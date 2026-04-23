package fast.campus.netplix.tour;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 관광사진갤러리 조회 서비스.
 * 캐시는 어댑터(VisitKoreaGalleryHttpClient) 에서 관리.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TourGalleryService implements GetTourGalleriesUseCase {

    private static final int MAX_LIMIT = 200;

    private final TourGalleryPort tourGalleryPort;

    @Override
    public List<TourGallery> byKeyword(String keyword, int limit) {
        return tourGalleryPort.fetchByKeyword(keyword, sanitize(limit));
    }

    @Override
    public List<TourGallery> all(int limit) {
        return tourGalleryPort.fetchAll(sanitize(limit));
    }

    private int sanitize(int limit) {
        if (limit <= 0) return MAX_LIMIT;
        return Math.min(limit, MAX_LIMIT);
    }
}
