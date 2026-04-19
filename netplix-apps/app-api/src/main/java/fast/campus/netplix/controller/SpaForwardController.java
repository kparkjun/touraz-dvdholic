package fast.campus.netplix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

/**
 * SPA 클라이언트 라우트 요청 시 index.html로 포워드.
 * 뷰 이름 "forward:/index.html"은 ViewResolver 미설정 시 500을 유발하므로 RequestDispatcher로 직접 포워드.
 */
@Controller
public class SpaForwardController {

    private final ResourceLoader resourceLoader;

    public SpaForwardController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping({
            "",
            "/",
            "/login",
            "/signup",
            "/dashboard",
            "/dashboard/images",
            "/main",
            "/account",
            "/support",
            "/login/oauth2/code/kakao",
            "/login/oauth2/code/apple",
            "/dvd-stores"
    })
    public Object forwardToIndex(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Resource index = resourceLoader.getResource("classpath:/static/index.html");
        if (index.exists() && index.isReadable()) {
            request.getRequestDispatcher("/index.html").forward(request, response);
            return null;
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                .body("<!DOCTYPE html><html><body><h1>404 Not Found</h1><p>Frontend not built. Deploy with Node buildpack + Gradle stage.</p></body></html>");
    }
}
