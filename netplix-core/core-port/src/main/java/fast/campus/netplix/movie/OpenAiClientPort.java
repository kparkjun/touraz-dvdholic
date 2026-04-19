package fast.campus.netplix.movie;

/**
 * OpenAI Chat API 호출용 포트.
 * Heroku 등에서 API 키만 설정하면 동작한다.
 */
public interface OpenAiClientPort {

    /**
     * 시스템 프롬프트와 사용자 메시지로 채팅 완료 응답 본문을 반환한다.
     * API 키 미설정·호출 실패 시 null 또는 빈 문자열.
     */
    String chat(String systemPrompt, String userMessage);
}
