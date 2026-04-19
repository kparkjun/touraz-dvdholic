package fast.campus.netplix.controller.support;

import fast.campus.netplix.controller.NetplixApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/support")
public class SupportController {

    private final String telegramBotToken;
    private final String telegramChatId;
    private final RestClient restClient;

    public SupportController(
            @Value("${telegram.bot-token:}") String telegramBotToken,
            @Value("${telegram.chat-id:}") String telegramChatId) {
        this.telegramBotToken = telegramBotToken;
        this.telegramChatId = telegramChatId;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.telegram.org")
                .build();
    }

    @PostMapping("/contact")
    public NetplixApiResponse<Map<String, String>> sendContactMessage(@RequestBody ContactRequest request) {
        if (request.message() == null || request.message().isBlank()) {
            return NetplixApiResponse.ok(Map.of("status", "INVALID_INPUT"));
        }

        if (telegramBotToken.isBlank() || telegramChatId.isBlank()) {
            log.warn("Telegram bot token or chat ID not configured");
            return NetplixApiResponse.ok(Map.of("status", "NOT_CONFIGURED"));
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String text = """
                📬 Touraz Holic 새 문의
                ─────────────────
                🕐 시간: %s
                ─────────────────
                💬 내용:
                %s
                """.formatted(timestamp, request.message());

        try {
            restClient.post()
                    .uri("/bot{token}/sendMessage", telegramBotToken)
                    .body(Map.of(
                            "chat_id", telegramChatId,
                            "text", text,
                            "parse_mode", "HTML"
                    ))
                    .retrieve()
                    .body(String.class);

            log.info("Telegram support message sent successfully");
            return NetplixApiResponse.ok(Map.of("status", "SENT"));
        } catch (Exception e) {
            log.error("Failed to send Telegram message: {}", e.getMessage());
            return NetplixApiResponse.ok(Map.of("status", "FAILED"));
        }
    }

    public record ContactRequest(String email, String message) {}
}
