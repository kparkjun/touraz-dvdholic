package fast.campus.netplix.filter;

import fast.campus.netplix.auth.FetchTokenUseCase;
import fast.campus.netplix.authentication.token.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final FetchTokenUseCase fetchTokenUseCase;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        String token = resolveToken(request);

        System.out.println("========== JWT 필터 디버깅 ==========");
        System.out.println("요청 URI: " + requestURI);
        System.out.println("토큰 존재: " + (token != null));
        
        if (token != null) {
            System.out.println("토큰 앞 20자: " + token.substring(0, Math.min(20, token.length())) + "...");
            boolean isValid = fetchTokenUseCase.validateToken(token);
            System.out.println("토큰 유효성: " + isValid);
            
            if (isValid) {
                try {
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("인증 성공! 권한: " + authentication.getAuthorities());
                } catch (Exception e) {
                    System.out.println("인증 실패! 에러: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        System.out.println("======================================");

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            // 토큰이 최소한의 형식을 갖추었는지 확인 (JWT는 최소 2개의 점을 포함)
            if (StringUtils.hasText(token) && token.contains(".")) {
                return token;
            }
        }

        return null;
    }
}
