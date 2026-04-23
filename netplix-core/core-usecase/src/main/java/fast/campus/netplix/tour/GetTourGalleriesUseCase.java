package fast.campus.netplix.tour;

import java.util.List;

/**
 * 관광사진갤러리 조회 유스케이스.
 * 컨트롤러 → 서비스 → 포트 계층 분리를 유지하기 위한 얇은 래퍼.
 */
public interface GetTourGalleriesUseCase {

    List<TourGallery> byKeyword(String keyword, int limit);

    List<TourGallery> all(int limit);
}
