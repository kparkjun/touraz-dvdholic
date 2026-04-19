package fast.campus.netplix.movie.response;

/**
 * DB 기준 영화/DVD 총 편수 (총 콘텐츠 수 표시용)
 */
public record ContentTotalsResponse(long movieTotal, long dvdTotal) {}
