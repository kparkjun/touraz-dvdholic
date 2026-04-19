package fast.campus.netplix.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 404 발생 시 SPA 라우트로 보이는 요청만 index.html로 포워드 (Whitelabel 대신 SPA 로딩).
 */
@Controller
public class SpaErrorViewResolver implements ErrorController {

    @RequestMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        if (status != null && (Integer) status == HttpStatus.NOT_FOUND.value()) {
            String path = request.getRequestURI();
            // API·정적 리소스·에러 경로는 그대로 404, SPA 라우트만 index로 포워드
            if (path != null && !path.startsWith("/api") && !path.contains(".") && !"/index.html".equals(path)) {
                return new ModelAndView("forward:/index.html");
            }
        }
        return null; // 기본 에러 페이지 사용
    }
}
