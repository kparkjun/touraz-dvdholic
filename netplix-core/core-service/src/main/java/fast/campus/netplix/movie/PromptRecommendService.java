package fast.campus.netplix.movie;

import fast.campus.netplix.movie.response.MovieWithRecommendReason;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * OpenAI API로 프롬프트+기분+동행 기반 추천.
 * DB 영화 목록을 프롬프트에 넣어, 우리 보유 작품만 추천하도록 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptRecommendService implements PromptRecommendUseCase {

    private static final int MAX_CATALOG = 500;
    private static final int MAX_INPUT_LENGTH = 200;
    private static final String LINE_SEP = "\n";
    private static final String TITLE_REASON_DELIM = " - ";

    private final OpenAiClientPort openAiClientPort;
    private final PersistenceMoviePort persistenceMoviePort;

    @Override
    public List<MovieWithRecommendReason> recommendByPrompt(
            String q, String mood, String companion, String contentType, int limit) {
        String safeQ = truncate(q, MAX_INPUT_LENGTH);
        String safeMood = truncate(mood, MAX_INPUT_LENGTH);
        String safeCompanion = truncate(companion, MAX_INPUT_LENGTH);
        List<String> catalogNames = collectMovieNames(contentType, MAX_CATALOG);
        if (catalogNames.isEmpty()) {
            log.warn("추천할 영화 목록이 비어 있음");
            return List.of();
        }
        String systemPrompt = "You are an expert movie recommender. You must recommend ONLY from the given list of movie titles. "
                + "Reply in Korean. You MUST list at least " + limit + " movies (or every possible match). "
                + "Think broadly: consider genre, mood, atmosphere, theme, director style, actors, and storyline relevance. "
                + "Write ONE line per movie with format: \"Exact Movie Title - reason in one short sentence.\" "
                + "Do not number the lines. Copy the exact title character-by-character from the list. Do NOT modify the title at all.";
        String userPrompt = buildUserPrompt(catalogNames, safeQ, safeMood, safeCompanion, limit);
        String response = openAiClientPort.chat(systemPrompt, userPrompt);
        if (response == null || response.isBlank()) {
            return List.of();
        }
        Map<String, String> titleToReason = parseResponseLines(response);
        List<String> orderedTitles = new ArrayList<>(titleToReason.keySet());
        if (orderedTitles.isEmpty()) {
            return List.of();
        }
        List<NetplixMovie> allCatalog = new ArrayList<>();
        int p = 0;
        while (true) {
            List<NetplixMovie> batch = persistenceMoviePort.fetchByContentType(contentType, p, 50);
            if (batch.isEmpty()) break;
            allCatalog.addAll(batch);
            if (batch.size() < 50) break;
            p++;
        }
        List<MovieWithRecommendReason> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String title : orderedTitles) {
            if (result.size() >= limit) break;
            String normalizedTitle = normalize(title);
            NetplixMovie movie = allCatalog.stream()
                    .filter(m -> normalize(m.getMovieName()).equals(normalizedTitle))
                    .findFirst()
                    .orElse(allCatalog.stream()
                            .filter(m -> normalize(m.getMovieName()).contains(normalizedTitle)
                                    || normalizedTitle.contains(normalize(m.getMovieName())))
                            .findFirst()
                            .orElse(null));
            if (movie == null || !seen.add(movie.getMovieName())) {
                continue;
            }
            String reason = titleToReason.getOrDefault(title, "추천 영화입니다.");
            result.add(new MovieWithRecommendReason(movie, reason));
        }
        log.info("OpenAI 프롬프트 추천: {}편 반환", result.size());
        return result;
    }

    private List<String> collectMovieNames(String contentType, int max) {
        List<String> names = new ArrayList<>();
        int page = 0;
        int pageSize = 50;
        while (names.size() < max) {
            List<NetplixMovie> pageList = persistenceMoviePort.fetchByContentType(contentType, page, pageSize);
            if (pageList.isEmpty()) break;
            for (NetplixMovie m : pageList) {
                if (m.getMovieName() != null && !m.getMovieName().isBlank()) {
                    names.add(m.getMovieName());
                    if (names.size() >= max) break;
                }
            }
            page++;
            if (pageList.size() < pageSize) break;
        }
        return names;
    }

    private String buildUserPrompt(List<String> catalogNames, String q, String mood, String companion, int limit) {
        String catalog = String.join(", ", catalogNames);
        StringBuilder sb = new StringBuilder();
        sb.append("Available movies (recommend ONLY from this list): ").append(catalog).append("\n\n");
        if (q != null && !q.isBlank()) sb.append("User request: ").append(q).append("\n");
        if (mood != null && !mood.isBlank()) sb.append("Mood: ").append(mood).append("\n");
        if (companion != null && !companion.isBlank()) sb.append("Watching with: ").append(companion).append("\n");
        sb.append("Pick at least ").append(limit).append(" movies that match. Be generous — include any movie that could remotely fit the request. ")
          .append("Reply with one line per movie: \"Exact Movie Title - reason in Korean.\"");
        return sb.toString();
    }

    private Map<String, String> parseResponseLines(String response) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String line : response.split(LINE_SEP)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            // 번호 접두어 제거 (1. 2. 1) 등)
            line = line.replaceFirst("^\\d+[.)]\\s*", "");
            int idx = line.indexOf(TITLE_REASON_DELIM);
            String title = idx >= 0 ? line.substring(0, idx).trim() : line;
            String reason = idx >= 0 ? line.substring(idx + TITLE_REASON_DELIM.length()).trim() : "추천 영화입니다.";
            if (!title.isEmpty()) {
                map.put(title, reason);
            }
        }
        return map;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\s\\p{Punct}]", "").toLowerCase();
    }

    private static String truncate(String value, int maxLen) {
        if (value == null) return null;
        value = value.trim();
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }
}
