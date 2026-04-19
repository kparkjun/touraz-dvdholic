package fast.campus.netplix.admin;

public interface InsertAccessLogPort {
    void insertAccessLog(String userId, String platform, String reqIp);
}
