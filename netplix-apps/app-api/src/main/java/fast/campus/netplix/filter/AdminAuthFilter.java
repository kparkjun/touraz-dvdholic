package fast.campus.netplix.filter;

import fast.campus.netplix.admin.AdminLoginUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminAuthFilter extends OncePerRequestFilter {

    private final AdminLoginUseCase adminLoginUseCase;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // /api/v1/cine-trip/auto-map (POST) 는 경로상 /admin 이 아니지만 실질적으로
        // 관리자 전용 기능(TMDB 자동 매핑). admin token 으로만 호출되므로
        // AdminAuthFilter 가 검증하도록 포함한다. (status 폴링 GET 은 permitAll 이라 제외)
        boolean isAdminPath = uri.startsWith("/api/v1/admin/");
        boolean isAutoMapAdminPath =
                "POST".equals(method) && "/api/v1/cine-trip/auto-map".equals(uri);

        if (!isAdminPath && !isAutoMapAdminPath) {
            filterChain.doFilter(request, response);
            return;
        }
        if ("/api/v1/admin/login".equals(uri) && "POST".equals(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        }
        if (token == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"code\":401,\"message\":\"관리자 인증이 필요합니다.\"}");
            return;
        }
        Optional<String> adminId = adminLoginUseCase.validateToken(token);
        if (adminId.isEmpty()) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"code\":401,\"message\":\"토큰이 만료되었거나 유효하지 않습니다.\"}");
            return;
        }
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                adminId.get(), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }
}
