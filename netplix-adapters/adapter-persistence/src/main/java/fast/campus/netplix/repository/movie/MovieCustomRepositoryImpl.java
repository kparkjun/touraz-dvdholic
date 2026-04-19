package fast.campus.netplix.repository.movie;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fast.campus.netplix.entity.movie.MovieEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static fast.campus.netplix.entity.movie.QMovieEntity.movieEntity;

@Repository
@RequiredArgsConstructor
public class MovieCustomRepositoryImpl implements MovieCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<MovieEntity> findByMovieName(String name) {
        return jpaQueryFactory.selectFrom(movieEntity)
                .where(movieEntity.movieName.eq(name))
                .fetch()
                .stream()
                .findFirst();
    }

    @Override
    public Page<MovieEntity> searchByKeyword(String keyword, Pageable pageable) {
        String k = keyword.trim();
        if (k.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        String pattern = "%" + k.toLowerCase().replace("%", "\\%").replace("_", "\\_") + "%";
        List<MovieEntity> fetch = jpaQueryFactory.selectFrom(movieEntity)
                .where(movieEntity.movieName.lower().like(pattern)
                        .or(movieEntity.genre.lower().like(pattern))
                        .or(movieEntity.overview.lower().like(pattern)))
                .orderBy(movieEntity.releaseDate.desc().nullsLast(), movieEntity.movieId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        long count = jpaQueryFactory.selectFrom(movieEntity)
                .where(movieEntity.movieName.lower().like(pattern)
                        .or(movieEntity.genre.lower().like(pattern))
                        .or(movieEntity.overview.lower().like(pattern)))
                .fetchCount();
        return new PageImpl<>(fetch, pageable, count);
    }

    @Override
    public Page<MovieEntity> search(Pageable pageable) {
        List<MovieEntity> fetch = jpaQueryFactory.selectFrom(movieEntity)
                .orderBy(movieEntity.releaseDate.desc().nullsLast(), movieEntity.movieId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long count = jpaQueryFactory.selectFrom(movieEntity)
                .fetch()
                .size();

        return new PageImpl<>(fetch, pageable, count);
    }

    @Override
    public Page<MovieEntity> searchByContentType(String contentType, Pageable pageable) {
        List<MovieEntity> fetch = jpaQueryFactory.selectFrom(movieEntity)
                .where(movieEntity.contentType.eq(contentType))
                .orderBy(movieEntity.releaseDate.desc().nullsLast(), movieEntity.movieId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long count = jpaQueryFactory.selectFrom(movieEntity)
                .where(movieEntity.contentType.eq(contentType))
                .fetch()
                .size();

        return new PageImpl<>(fetch, pageable, count);
    }

    @Override
    public Page<MovieEntity> searchByContentTypeAndGenre(String contentType, String genre, Pageable pageable) {
        if (genre == null || genre.isBlank()) {
            // 최신순 정렬
            List<MovieEntity> fetch = jpaQueryFactory.selectFrom(movieEntity)
                    .where(movieEntity.contentType.eq(contentType))
                    .orderBy(movieEntity.releaseDate.desc().nullsLast(), movieEntity.movieId.asc())
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
            long count = jpaQueryFactory.selectFrom(movieEntity)
                    .where(movieEntity.contentType.eq(contentType))
                    .fetch().size();
            return new PageImpl<>(fetch, pageable, count);
        }
        String genrePattern = "%" + genre.trim() + "%";
        List<MovieEntity> fetch = jpaQueryFactory.selectFrom(movieEntity)
                .where(movieEntity.contentType.eq(contentType)
                        .and(movieEntity.genre.likeIgnoreCase(genrePattern)))
                .orderBy(movieEntity.releaseDate.desc().nullsLast(), movieEntity.movieId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long count = jpaQueryFactory.selectFrom(movieEntity)
                .where(movieEntity.contentType.eq(contentType)
                        .and(movieEntity.genre.likeIgnoreCase(genrePattern)))
                .fetch()
                .size();

        return new PageImpl<>(fetch, pageable, count);
    }

    @Override
    public List<MovieEntity> findByMovieNameIn(List<String> movieNames) {
        if (movieNames == null || movieNames.isEmpty()) return List.of();
        return jpaQueryFactory.selectFrom(movieEntity)
                .where(movieEntity.movieName.in(movieNames))
                .orderBy(movieEntity.releaseDate.desc().nullsLast())
                .fetch();
    }

    @Override
    public List<MovieEntity> findByGenresExcludingMovieNames(String contentType, List<String> genres, List<String> excludeMovieNames, int limit) {
        if (genres == null || genres.isEmpty()) return List.of();
        
        var query = jpaQueryFactory.selectFrom(movieEntity)
                .where(movieEntity.contentType.eq(contentType));
        
        var genreCondition = genres.stream()
                .map(genre -> movieEntity.genre.containsIgnoreCase(genre))
                .reduce((a, b) -> a.or(b))
                .orElse(null);
        
        if (genreCondition != null) {
            query = query.where(genreCondition);
        }
        
        if (excludeMovieNames != null && !excludeMovieNames.isEmpty()) {
            query = query.where(movieEntity.movieName.notIn(excludeMovieNames));
        }
        
        return query
                .orderBy(movieEntity.voteAverage.desc().nullsLast(), movieEntity.releaseDate.desc().nullsLast())
                .limit(limit)
                .fetch();
    }

    @Override
    public long countByContentTypeAndGenre(String contentType, String genre) {
        if (genre == null || genre.isBlank()) {
            return jpaQueryFactory.selectFrom(movieEntity)
                    .where(movieEntity.contentType.eq(contentType))
                    .fetchCount();
        }
        String genrePattern = "%" + genre.trim() + "%";
        return jpaQueryFactory.selectFrom(movieEntity)
                .where(movieEntity.contentType.eq(contentType)
                        .and(movieEntity.genre.likeIgnoreCase(genrePattern)))
                .fetchCount();
    }

    @Override
    public Page<MovieEntity> searchAdvanced(String contentType, String genre, String filter, Pageable pageable) {
        BooleanBuilder where = buildAdvancedWhere(contentType, genre, filter);
        var orderBy = resolveOrder(filter);

        List<MovieEntity> fetch = jpaQueryFactory.selectFrom(movieEntity)
                .where(where)
                .orderBy(orderBy, movieEntity.releaseDate.desc().nullsLast(), movieEntity.movieId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long count = jpaQueryFactory.selectFrom(movieEntity)
                .where(where)
                .fetchCount();

        return new PageImpl<>(fetch, pageable, count);
    }

    @Override
    public long countAdvanced(String contentType, String genre, String filter) {
        BooleanBuilder where = buildAdvancedWhere(contentType, genre, filter);
        return jpaQueryFactory.selectFrom(movieEntity)
                .where(where)
                .fetchCount();
    }

    private BooleanBuilder buildAdvancedWhere(String contentType, String genre, String filter) {
        BooleanBuilder where = new BooleanBuilder();

        if (contentType != null && !contentType.isBlank() && !"all".equalsIgnoreCase(contentType)) {
            where.and(movieEntity.contentType.eq(contentType));
        }

        if (genre != null && !genre.isBlank()) {
            if (genre.contains("|")) {
                BooleanBuilder genreOr = new BooleanBuilder();
                for (String g : genre.split("\\|")) {
                    genreOr.or(movieEntity.genre.likeIgnoreCase("%" + g.trim() + "%"));
                }
                where.and(genreOr);
            } else {
                where.and(movieEntity.genre.likeIgnoreCase("%" + genre.trim() + "%"));
            }
        }

        if (filter != null && !filter.isBlank()) {
            switch (filter) {
                case "korean":
                    where.and(movieEntity.originalLanguage.eq("ko"));
                    break;
                case "classics":
                    where.and(movieEntity.voteAverage.goe(8.0));
                    where.and(movieEntity.voteCount.goe(500));
                    break;
                case "new":
                    String threeMonthsAgo = LocalDate.now().minusMonths(3)
                            .format(DateTimeFormatter.ISO_LOCAL_DATE);
                    where.and(movieEntity.releaseDate.goe(threeMonthsAgo));
                    break;
                case "blockbuster":
                    where.and(movieEntity.revenue.goe(100_000_000L));
                    break;
                case "hidden":
                    where.and(movieEntity.voteAverage.goe(7.0));
                    where.and(movieEntity.voteCount.lt(200));
                    break;
                case "japanese":
                    where.and(movieEntity.originalLanguage.eq("ja"));
                    break;
                case "collection":
                    where.and(movieEntity.collectionName.isNotNull());
                    where.and(movieEntity.collectionName.ne(""));
                    break;
                default:
                    break;
            }
        }

        return where;
    }

    private com.querydsl.core.types.OrderSpecifier<?> resolveOrder(String filter) {
        if (filter == null) return movieEntity.releaseDate.desc().nullsLast();
        return switch (filter) {
            case "classics", "hidden" -> movieEntity.voteAverage.desc().nullsLast();
            case "blockbuster" -> movieEntity.revenue.desc().nullsLast();
            case "collection" -> movieEntity.collectionName.asc().nullsLast();
            default -> movieEntity.releaseDate.desc().nullsLast();
        };
    }
}
