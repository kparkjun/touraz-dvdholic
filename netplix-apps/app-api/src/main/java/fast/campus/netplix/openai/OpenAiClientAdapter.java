package fast.campus.netplix.openai;

import fast.campus.netplix.movie.OpenAiClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

/**
 * OpenAI Chat Completions API 호출.
 * Heroku: OPENAI_API_KEY 설정 시 동작, 미설정 시 chat()이 null 반환.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClientAdapter implements OpenAiClientPort {

    private static final String CHAT_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-3.5-turbo";

    @Value("${openai.api-key:}")
    private String apiKey;

    private static final int CONNECT_TIMEOUT_SEC = 5;
    private static final int READ_TIMEOUT_SEC = 30;

    private final RestTemplate restTemplate = createRestTemplate();

    private static RestTemplate createRestTemplate() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SEC));
        factory.setReadTimeout(Duration.ofSeconds(READ_TIMEOUT_SEC));
        return new RestTemplate(factory);
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("OpenAI API 키 미설정");
            return null;
        }
        try {
            // 10편×한글 이유까지 수용 (한 개만 나오는 현상 방지)
            ChatRequest req = new ChatRequest(MODEL, List.of(
                    new ChatMessage("system", systemPrompt),
                    new ChatMessage("user", userMessage)
            ), 1200);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            ResponseEntity<ChatResponse> res = restTemplate.exchange(
                    CHAT_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(req, headers),
                    ChatResponse.class
            );
            if (res.getBody() != null && res.getBody().choices != null && !res.getBody().choices.isEmpty()) {
                String content = res.getBody().choices.get(0).message.content;
                return content != null ? content.trim() : null;
            }
        } catch (Exception e) {
            log.warn("OpenAI API 호출 실패: {}", e.getMessage());
        }
        return null;
    }

    private static class ChatRequest {
        public final String model;
        public final List<ChatMessage> messages;
        public final int max_tokens;

        ChatRequest(String model, List<ChatMessage> messages, int max_tokens) {
            this.model = model;
            this.messages = messages;
            this.max_tokens = max_tokens;
        }
    }

    private static class ChatMessage {
        public final String role;
        public final String content;

        ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    private static class ChatResponse {
        public List<Choice> choices;

        static class Choice {
            public Message message;
        }

        static class Message {
            public String content;
        }
    }
}
