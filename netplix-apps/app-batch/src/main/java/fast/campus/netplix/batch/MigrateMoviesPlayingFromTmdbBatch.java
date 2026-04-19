package fast.campus.netplix.batch;

import fast.campus.netplix.movie.FetchMovieUseCase;
import fast.campus.netplix.movie.InsertMovieUseCase;
import fast.campus.netplix.movie.NetplixMovie;
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
public class MigrateMoviesPlayingFromTmdbBatch {

    private final static String BATCH_NAME = "MigrateMoviesPlayingFromTmdbBatch";

    private final fast.campus.netplix.movie.TmdbMoviePlayingPort tmdbMoviePlayingPort;
    private final InsertMovieUseCase insertMovieUseCase;

    @Bean(name = BATCH_NAME)
    public Job job(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new JobBuilder(BATCH_NAME, jobRepository)
                .start(step(jobRepository, platformTransactionManager))
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean(name = "MigrateMoviesPlayingFromTmdbBatchTaskletStep")
    public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("MigrateMoviesPlayingFromTmdbBatchTaskletStep", jobRepository)
                .chunk(5, platformTransactionManager)
                .reader(new HttpPageItemReaderForMovies(80, tmdbMoviePlayingPort))  // 80페이지 = 1600편 (DB 용량 2배 업그레이드 반영)
                .writer(chunk -> {
                    List<NetplixMovie> items = (List<NetplixMovie>) chunk.getItems();
                    log.info("========== Processing {} movies (MOVIE) ==========", items.size());
                    
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
        for (char c : overview.toCharArray()) {
            if (c >= '\u4E00' && c <= '\u9FFF') {
                log.info("Filtering movie with corrupted Korean: {} - {}", 
                    movie.getMovieName(), overview.substring(0, Math.min(50, overview.length())));
                return false;
            }
        }
        
        return true;
    }
}
