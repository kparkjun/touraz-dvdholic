package fast.campus.netplix.controller.tour;

import fast.campus.netplix.tour.TourGallery;

/**
 * 프론트엔드 전달용 관광사진갤러리 DTO.
 */
public record TourGalleryResponse(
        String galContentId,
        String galContentTypeId,
        String title,
        String photographer,
        String photoMonth,
        String photoLocation,
        String searchKeyword,
        String thumbnailUrl,
        String imageUrl,
        String createdTime,
        String modifiedTime
) {
    public static TourGalleryResponse from(TourGallery g) {
        return new TourGalleryResponse(
                g.getGalContentId(),
                g.getGalContentTypeId(),
                g.getTitle(),
                g.getPhotographer(),
                g.getPhotoMonth(),
                g.getPhotoLocation(),
                g.getSearchKeyword(),
                g.getThumbnailUrl(),
                g.getImageUrl(),
                g.getCreatedTime(),
                g.getModifiedTime()
        );
    }
}
