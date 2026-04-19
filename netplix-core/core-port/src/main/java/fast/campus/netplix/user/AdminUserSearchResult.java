package fast.campus.netplix.user;

/**
 * 관리자용: USER_NAME으로 검색한 결과 (users / social_users)
 */
public record AdminUserSearchResult(
        String userId,
        String userName,
        String email,
        String phone,
        String source,
        String provider
) {
}
