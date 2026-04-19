package fast.campus.netplix.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fast.campus.netplix.logging.CreateAuditLog;
import fast.campus.netplix.logging.LogUserAuditHistoryUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserHistoryLoggingFilter extends OncePerRequestFilter {

    private final LogUserAuditHistoryUseCase logUserAuditHistoryUseCase;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (shouldLog(authentication)) {
            CompletableFuture.runAsync(() -> {
                try {
                    log(authentication, request);
                } catch (Exception e) {
                    // 감사 로그 실패가 요청 실패로 이어지지 않도록 무시
                }
            });
        }
        filterChain.doFilter(request, response);
    }

    private boolean shouldLog(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() != null
                && !"anonymousUser".equals(String.valueOf(authentication.getPrincipal()));
    }

    public void log(Authentication authentication, HttpServletRequest httpServletRequest) {
        if (!shouldLog(authentication)) {
            return;
        }
        logUserAuditHistoryUseCase.log(
                new CreateAuditLog(
                        authentication.getName(),
                        authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")),
                        httpServletRequest.getRemoteAddr(),
                        httpServletRequest.getMethod(),
                        httpServletRequest.getRequestURI(),
                        getHeaders(httpServletRequest),
                        "payload"
                )
        );
    }

    private String getHeaders(HttpServletRequest request) {
        // 헤더를 저장할 Map 생성
        Map<String, String> headersMap = new HashMap<>();

        // 모든 헤더 추출
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                headersMap.put(headerName, headerValue);
            }
        }

        // Map을 JSON 문자열로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(headersMap);
        } catch (JsonProcessingException e) {
            return "{}"; // 변환 실패 시 빈 JSON 반환
        }
    }
}
