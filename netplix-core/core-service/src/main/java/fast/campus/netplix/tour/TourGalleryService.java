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

    // PhotoGalleryService1 의 galleryList1 totalCount ≈ 6,100 장. 10,000 상한이면 넉넉.
    private static final int MAX_LIMIT = 10_000;

    private final TourGalleryPort tourGalleryPort;

    @Override
    public List<TourGallery> byKeyword(String keyword, int limit) {
        return tourGalleryPort.fetchByKeyword(keyword, sanitize(limit));
    }

    @Override
    public List<TourGallery> all(int limit) {
        return tourGalleryPort.fetchAll(sanitize(limit));
    }

    /**
     * limit <= 0 → "전체 반환" 의미로 해석 (포트 레벨에서도 limit<=0 이면 자르지 않음).
     * 그 외에는 MAX_LIMIT 으로 상한만 맞춰준다.
     */
    private int sanitize(int limit) {
        if (limit <= 0) return 0; // 전체 반환
        return Math.min(limit, MAX_LIMIT);
    }
}
