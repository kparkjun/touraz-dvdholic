package fast.campus.netplix.controller.movie;

import fast.campus.netplix.authentication.token.JwtTokenProvider;
import fast.campus.netplix.controller.NetplixApiResponse;
import fast.campus.netplix.movie.LikeMovieUseCase;
import fast.campus.netplix.movie.NetplixMovie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movie")
@RequiredArgsConstructor
public class UserMovieController {

    private final JwtTokenProvider jwtTokenProvider;
    private final LikeMovieUseCase likeMovieUseCase;

    @PostMapping("/{movieId}/like")
    public NetplixApiResponse<Boolean> likeMovie(
            @PathVariable String movieId,
            @RequestParam(defaultValue = "movie") String contentType,
            HttpServletRequest request) {
        String userId = getUserIdOrAnonymous(request);
        return NetplixApiResponse.ok(likeMovieUseCase.like(userId, movieId, contentType));
    }

    @PostMapping("/{movieId}/unlike")
    public NetplixApiResponse<Boolean> unlikeMovie(
            @PathVariable String movieId,
            @RequestParam(defaultValue = "movie") String contentType,
            HttpServletRequest request) {
        String userId = getUserIdOrAnonymous(request);
        return NetplixApiResponse.ok(likeMovieUseCase.unlike(userId, movieId, contentType));
    }

    @PostMapping("/{movieId}/meh")
    public NetplixApiResponse<Boolean> mehMovie(
            @PathVariable String movieId,
            @RequestParam(defaultValue = "movie") String contentType,
            HttpServletRequest request) {
        String userId = getUserIdOrAnonymous(request);
        return NetplixApiResponse.ok(likeMovieUseCase.meh(userId, movieId, contentType));
    }

    private String getUserIdOrAnonymous(HttpServletRequest request) {
        try {
            String userId = jwtTokenProvider.getUserId();
            if (userId != null && !userId.isEmpty()) {
                return userId;
            }
        } catch (Exception e) {
            // 토큰이 없거나 유효하지 않음
        }
        String anonId = request.getHeader("X-Anon-Id");
        if (anonId != null && !anonId.isBlank()) {
            return "anon_" + anonId;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
        return "anon_" + ip;
    }
    
    @GetMapping("/{movieId}/like-count")
    public NetplixApiResponse<Long> getLikeCount(
            @PathVariable String movieId,
            @RequestParam(defaultValue = "movie") String contentType) {
        return NetplixApiResponse.ok(likeMovieUseCase.getLikeCount(movieId, contentType));
    }
    
    @GetMapping("/{movieId}/unlike-count")
    public NetplixApiResponse<Long> getUnlikeCount(
            @PathVariable String movieId,
            @RequestParam(defaultValue = "movie") String contentType) {
        return NetplixApiResponse.ok(likeMovieUseCase.getUnlikeCount(movieId, contentType));
    }

    @GetMapping("/{movieId}/meh-count")
    public NetplixApiResponse<Long> getMehCount(
            @PathVariable String movieId,
            @RequestParam(defaultValue = "movie") String contentType) {
        return NetplixApiResponse.ok(likeMovieUseCase.getMehCount(movieId, contentType));
    }

    @GetMapping("/today-popular")
    public NetplixApiResponse<List<NetplixMovie>> getTodayPopular(
            @RequestParam(defaultValue = "movie") String contentType,
            @RequestParam(defaultValue = "10") int limit) {
        return NetplixApiResponse.ok(likeMovieUseCase.getTodayPopular(contentType, Math.min(limit, 20)));
    }

    @GetMapping("/popular")
    public NetplixApiResponse<List<NetplixMovie>> getPopular(
            @RequestParam(defaultValue = "today") String period,
            @RequestParam(defaultValue = "movie") String contentType,
            @RequestParam(defaultValue = "10") int limit) {
        return NetplixApiResponse.ok(likeMovieUseCase.getPopular(period, contentType, Math.min(limit, 20)));
    }

    @GetMapping("/{movieId}/my-vote")
    public NetplixApiResponse<String> getMyVote(
            @PathVariable String movieId,
            @RequestParam(defaultValue = "movie") String contentType,
            HttpServletRequest request) {
        String userId = getUserIdOrAnonymous(request);
        return NetplixApiResponse.ok(likeMovieUseCase.getMyVote(userId, movieId, contentType));
    }
}
