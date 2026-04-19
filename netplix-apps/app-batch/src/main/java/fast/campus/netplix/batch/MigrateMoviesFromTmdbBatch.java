package fast.campus.netplix.batch;

import fast.campus.netplix.movie.InsertMovieUseCase;
import fast.campus.netplix.movie.TmdbMoviePort;
import fast.campus.netplix.movie.NetplixMovie;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MigrateMoviesFromTmdbBatch {

    private final static String BATCH_NAME = "MigrateMoviesFromTmdbBatch";

    private final TmdbMoviePort tmdbMoviePort;
    private final InsertMovieUseCase insertMovieUseCase;

    @Bean(name = BATCH_NAME)
    public Job job(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new JobBuilder(BATCH_NAME, jobRepository)
                .start(step(jobRepository, platformTransactionManager))
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean(name = "MigrateMoviesFromTmdbBatchTaskletStep")
    public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("MigrateMoviesFromTmdbBatchTaskletStep", jobRepository)
                .chunk(5, platformTransactionManager)
                .reader(new HttpPageItemReaderForDvd(40, tmdbMoviePort))  // 40페이지 = 800편 (안정권)
                .writer(chunk -> {
                    List<NetplixMovie> items = (List<NetplixMovie>) chunk.getItems();
                    log.info("========== Processing {} movies ==========", items.size());
                    
                    // Filter out movies with corrupted Korean text
                    List<NetplixMovie> validItems = items.stream()
                            .filter(this::isValidOverview)
                            .collect(Collectors.toList());
                    
                    log.info("Filtered {} movies out of {}", items.size() - validItems.size(), items.size());
                    
                    // Insert each movie with logging
                    for (NetplixMovie movie : validItems) {
                        log.info("Inserting movie: {}", movie.getMovieName());
                        insertMovieUseCase.insert(List.of(movie));
                        log.info("✓ Completed movie: {}", movie.getMovieName());
                    }
                    
                    log.info("========== Chunk completed ==========");
                })
                .build();
    }
    
    private boolean isValidOverview(NetplixMovie movie) {
        String overview = movie.getOverview();
        
        // Log posterPath and backdropPath for debugging
        log.info("Movie: {}, PosterPath: {}, BackdropPath: {}", movie.getMovieName(), movie.getPosterPath(), movie.getBackdropPath());
        
        // Allow empty or null overview
        if (overview == null || overview.trim().isEmpty()) {
            return true;
        }
        
        // Filter out movies with corrupted Korean text
        // Corrupted Korean contains CJK Chinese characters (蹂, 議, etc.)
        // instead of proper Korean characters
        for (char c : overview.toCharArray()) {
            // Check if character is CJK Chinese ideograph (not Korean)
            if (c >= '\u4E00' && c <= '\u9FFF') {
                log.info("Filtering movie with corrupted Korean: {} - {}", 
                    movie.getMovieName(), overview.substring(0, Math.min(50, overview.length())));
                return false;
            }
        }
        
        return true;
    }
}
