package fast.campus.netplix.security;

import fast.campus.netplix.user.FetchUserUseCase;
import fast.campus.netplix.user.response.DetailUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NetplixUserDetailsService implements UserDetailsService {

    private final FetchUserUseCase fetchUserUseCase;

    @Override
    public NetplixAuthUser loadUserByUsername(String email) throws UsernameNotFoundException {
        DetailUserResponse user = fetchUserUseCase.findDetailUserByEmail(email);
        
        System.out.println("========== 사용자 로그인 디버깅 ==========");
        System.out.println("이메일: " + email);
        System.out.println("사용자 ID: " + user.userId());
        System.out.println("사용자 이름: " + user.username());
        System.out.println("사용자 Role: " + user.role());
        
        // Role이 있으면 권한 설정, 없으면 기본 ROLE_FREE 부여
        List<SimpleGrantedAuthority> authorities;
        if (StringUtils.hasText(user.role())) {
            authorities = List.of(new SimpleGrantedAuthority(user.role()));
            System.out.println("부여된 권한: " + user.role());
        } else {
            authorities = List.of(new SimpleGrantedAuthority("ROLE_FREE"));
            System.out.println("부여된 권한: ROLE_FREE (기본값)");
        }
        System.out.println("==========================================");
        
        return new NetplixAuthUser(
                user.userId(), user.username(), user.password(),
                user.email(), user.phone(), authorities
        );
    }
}
