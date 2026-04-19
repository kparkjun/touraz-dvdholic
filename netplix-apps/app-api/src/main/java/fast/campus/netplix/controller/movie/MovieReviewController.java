package fast.campus.netplix.controller.movie;

import fast.campus.netplix.authentication.token.JwtTokenProvider;
import fast.campus.netplix.controller.NetplixApiResponse;
import fast.campus.netplix.movie.MovieReview;
import fast.campus.netplix.movie.MovieReviewUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/movie")
@RequiredArgsConstructor
public class MovieReviewController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MovieReviewUseCase movieReviewUseCase;

    @PostMapping("/{movieName}/review")
    public NetplixApiResponse<MovieReview> saveReview(
            @PathVariable String movieName,
            @RequestParam(defaultValue = "movie") String contentType,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String userId = getUserIdOrAnonymous(request);
        String oneLiner = (String) body.get("oneLiner");
        String fullReview = (String) body.get("fullReview");
        Boolean isSpoiler = body.get("isSpoiler") instanceof Boolean b ? b : false;

        MovieReview saved = movieReviewUseCase.saveReview(userId, movieName, contentType,
                oneLiner, fullReview, isSpoiler);
        return NetplixApiResponse.ok(saved);
    }

    @GetMapping("/{movieName}/reviews")
    public NetplixApiResponse<List<MovieReview>> getReviews(
            @PathVariable String movieName,
            @RequestParam(defaultValue = "movie") String contentType,
            @RequestParam(defaultValue = "newest") String sort) {
        return NetplixApiResponse.ok(movieReviewUseCase.getReviews(movieName, contentType, sort));
    }

    @PostMapping("/{movieName}/review/{reviewId}/helpful")
    public NetplixApiResponse<Boolean> markHelpful(
            @PathVariable String movieName,
            @PathVariable String reviewId) {
        movieReviewUseCase.markHelpful(reviewId);
        return NetplixApiResponse.ok(true);
    }

    @DeleteMapping("/{movieName}/review/{reviewId}")
    public NetplixApiResponse<Boolean> deleteReview(
            @PathVariable String movieName,
            @PathVariable String reviewId,
            HttpServletRequest request) {
        String userId = getUserIdOrAnonymous(request);
        movieReviewUseCase.deleteReview(reviewId, userId);
        return NetplixApiResponse.ok(true);
    }

    private String getUserIdOrAnonymous(HttpServletRequest request) {
        try {
            String userId = jwtTokenProvider.getUserId();
            if (userId != null && !userId.isEmpty()) {
                return userId;
            }
        } catch (Exception ignored) {}
        String ip = request.getRemoteAddr();
        return "anon_" + ip;
    }
}
