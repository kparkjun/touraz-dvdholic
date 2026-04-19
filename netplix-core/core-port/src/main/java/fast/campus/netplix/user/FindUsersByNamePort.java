package fast.campus.netplix.user;

import java.util.List;

public interface FindUsersByNamePort {
    List<AdminUserSearchResult> findByUsername(String username);
}
