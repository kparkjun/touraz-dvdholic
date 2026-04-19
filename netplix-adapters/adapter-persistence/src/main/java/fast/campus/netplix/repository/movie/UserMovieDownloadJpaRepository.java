package fast.campus.netplix.repository.movie;

import fast.campus.netplix.entity.movie.UserMovieDownloadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserMovieDownloadJpaRepository extends JpaRepository<UserMovieDownloadEntity, String>, UserMovieDownloadCustomRepository {

    @Modifying
    @Query("DELETE FROM UserMovieDownloadEntity u WHERE u.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}
