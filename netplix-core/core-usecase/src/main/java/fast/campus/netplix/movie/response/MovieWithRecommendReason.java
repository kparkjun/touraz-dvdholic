package fast.campus.netplix.movie.response;

import fast.campus.netplix.movie.NetplixMovie;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MovieWithRecommendReason {
    private final NetplixMovie movie;
    private final String reason;
}
