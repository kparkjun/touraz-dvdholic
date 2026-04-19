package fast.campus.netplix.entity.movie;

import fast.campus.netplix.entity.audit.MutableBaseEntity;
import fast.campus.netplix.movie.UserMovieLike;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

@Getter
@Entity
@Table(name = "user_movie_likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserMovieLikeEntity extends MutableBaseEntity implements Persistable<String> {
    @Id
    @Column(name = "USER_MOVIE_LIKE_ID")
    private String userMovieLikeId;

    @Transient
    private boolean isNewEntity = false;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "MOVIE_ID")
    private String movieId;

    @Column(name = "CONTENT_TYPE")
    private String contentType; // "movie" 또는 "dvd"

    @Column(name = "LIKE_YN")
    private Boolean likeYn;

    @Column(name = "VOTE_TYPE")
    private String voteType; // "like" | "unlike" | "meh"

    @Override
    public String getId() {
        return userMovieLikeId;
    }

    @Override
    public boolean isNew() {
        return isNewEntity;
    }

    public UserMovieLike toDomain() {
        String vt = voteType != null ? voteType : (Boolean.TRUE.equals(likeYn) ? "like" : "unlike");
        Boolean ly = "like".equals(vt);
        return UserMovieLike.builder()
                .userMovieLikeId(userMovieLikeId)
                .userId(userId)
                .movieId(movieId)
                .contentType(contentType)
                .likeYn(ly)
                .voteType(vt)
                .build();
    }

    public static UserMovieLikeEntity toEntity(UserMovieLike domain) {
        return toEntity(domain, false);
    }

    public static UserMovieLikeEntity toEntity(UserMovieLike domain, boolean markNew) {
        String vt = domain.getVoteType();
        Boolean ly = "like".equals(vt);
        UserMovieLikeEntity entity = new UserMovieLikeEntity(
                domain.getUserMovieLikeId(),
                false,
                domain.getUserId(),
                domain.getMovieId(),
                domain.getContentType(),
                ly,
                vt
        );
        entity.isNewEntity = markNew;
        return entity;
    }
}
