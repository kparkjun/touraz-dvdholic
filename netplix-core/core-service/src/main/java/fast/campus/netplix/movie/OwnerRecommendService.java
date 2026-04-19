package fast.campus.netplix.movie;

import fast.campus.netplix.movie.response.MovieWithRecommendReason;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * DVD방 주인장 추천 서비스.
 * TMDB 트렌딩 데이터를 RAG 컨텍스트로 활용하고,
 * DB 카탈로그 + OpenAI를 결합하여 사장님 페르소나로 추천한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OwnerRecommendService implements OwnerRecommendUseCase {

    private static final int MAX_CATALOG = 500;
    private static final int MAX_INPUT_LENGTH = 300;
    private static final String LINE_SEP = "\n";
    private static final String TITLE_REASON_DELIM = " - ";

    private final OpenAiClientPort openAiClientPort;
    private final PersistenceMoviePort persistenceMoviePort;
    private final TmdbTrendingPort tmdbTrendingPort;

    private final AtomicReference<String> trendingContext = new AtomicReference<>("");

    @Override
    public void refreshTrendingContext() {
        try {
            List<String> trending = tmdbTrendingPort.fetchWeeklyTrending(3);
            List<String> popular = tmdbTrendingPort.fetchPopular(2);

            StringBuilder ctx = new StringBuilder();
            if (!trending.isEmpty()) {
                ctx.append("[이번 주 트렌딩 영화]\n");
                for (int i = 0; i < Math.min(trending.size(), 40); i++) {
                    ctx.append((i + 1)).append(". ").append(trending.get(i)).append("\n");
                }
                ctx.append("\n");
            }
            if (!popular.isEmpty()) {
                ctx.append("[인기 영화]\n");
                for (int i = 0; i < Math.min(popular.size(), 30); i++) {
                    ctx.append((i + 1)).append(". ").append(popular.get(i)).append("\n");
                }
            }
            String result = ctx.toString().trim();
            trendingContext.set(result);
            log.info("[주인장추천] 트렌딩 컨텍스트 갱신 완료 (trending: {}편, popular: {}편, 총 {}자)",
                    trending.size(), popular.size(), result.length());
        } catch (Exception e) {
            log.warn("[주인장추천] 트렌딩 컨텍스트 갱신 실패: {}", e.getMessage());
        }
    }

    @Override
    public boolean hasTrendingContext() {
        String ctx = trendingContext.get();
        return ctx != null && !ctx.isBlank();
    }

    @Override
    public List<MovieWithRecommendReason> recommendByOwner(String customerRequest, String contentType, int limit) {
        String safeRequest = truncate(customerRequest, MAX_INPUT_LENGTH);
        List<String> catalogNames = collectMovieNames(contentType, MAX_CATALOG);
        if (catalogNames.isEmpty()) {
            log.warn("[주인장추천] 추천할 영화 목록이 비어 있음");
            return List.of();
        }

        String systemPrompt = buildOwnerSystemPrompt();
        String userPrompt = buildOwnerUserPrompt(catalogNames, safeRequest, limit);

        String response = openAiClientPort.chat(systemPrompt, userPrompt);
        if (response == null || response.isBlank()) {
            log.warn("[주인장추천] OpenAI 응답 없음");
            return List.of();
        }

        Map<String, String> titleToReason = parseResponseLines(response);
        List<String> orderedTitles = new ArrayList<>(titleToReason.keySet());
        if (orderedTitles.isEmpty()) return List.of();

        List<NetplixMovie> allCatalog = loadAllCatalog(contentType);
        List<MovieWithRecommendReason> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String title : orderedTitles) {
            if (result.size() >= limit) break;
            String normalizedTitle = normalize(title);
            NetplixMovie movie = findMovie(allCatalog, normalizedTitle);
            if (movie == null || !seen.add(movie.getMovieName())) continue;

            String reason = titleToReason.getOrDefault(title, "사장님이 추천하는 영화입니다!");
            result.add(new MovieWithRecommendReason(movie, reason));
        }
        log.info("[주인장추천] {}편 반환 (요청: '{}')", result.size(), safeRequest);
        return result;
    }

    private String buildOwnerSystemPrompt() {
        return "당신은 20년 경력의 동네 DVD방 사장님입니다.\n"
                + "매일 다양한 손님을 상대하며 취향을 읽는 눈이 탁월합니다.\n"
                + "말투는 편안한 '~요체'를 쓰고, 추천 이유는 짧지만 감 잡히게 설명합니다.\n\n"
                + "손님은 정해진 형식 없이 자유롭게 말합니다. 어떤 말이든 의도와 기분을 읽어서 영화를 골라주세요.\n"
                + "장르를 말할 수도, 기분을 말할 수도, 상황을 말할 수도, 그냥 '추천해주세요' 한마디일 수도 있습니다.\n"
                + "사장님은 손님 말 속에서 원하는 분위기·장르·상황을 자연스럽게 파악해서 딱 맞는 영화를 골라줍니다.\n\n"
                + "규칙:\n"
                + "1. 반드시 주어진 '보유 영화 목록'에 있는 영화만 추천하세요.\n"
                + "2. '트렌딩/인기 정보'가 있으면 참고하여 요즘 화제작을 우선 추천하세요.\n"
                + "3. 손님 말에서 읽히는 장르·분위기·상황에 맞는 영화를 추천하세요.\n"
                + "4. 한 줄에 하나씩, 형식: \"정확한 영화 제목 - 추천 이유\" 로 작성하세요.\n"
                + "5. 번호를 붙이지 마세요.\n"
                + "6. 제목은 목록에 있는 것과 한 글자도 다르지 않게 그대로 복사하세요.\n"
                + "7. 추천 이유는 사장님 말투로 자연스럽게 작성하세요.";
    }

    private String buildOwnerUserPrompt(List<String> catalogNames, String request, int limit) {
        StringBuilder sb = new StringBuilder();

        String ctx = trendingContext.get();
        if (ctx != null && !ctx.isBlank()) {
            sb.append("=== 최신 트렌딩/인기 정보 (참고용) ===\n");
            sb.append(ctx).append("\n\n");
        }

        sb.append("=== 우리 매장 보유 영화 목록 (이 목록에서만 추천) ===\n");
        sb.append(String.join(", ", catalogNames)).append("\n\n");

        sb.append("=== 손님 요청 ===\n");
        sb.append(request).append("\n\n");

        sb.append("위 손님의 요청에 맞는 영화를 최소 ").append(limit).append("편 추천해주세요.\n");
        sb.append("트렌딩/인기 목록에 있으면서 우리 매장에도 있는 영화를 우선 추천하세요.\n");
        sb.append("형식: \"정확한 영화 제목 - 사장님 말투로 추천 이유\"");
        return sb.toString();
    }

    private List<String> collectMovieNames(String contentType, int max) {
        List<String> names = new ArrayList<>();
        int page = 0;
        while (names.size() < max) {
            List<NetplixMovie> batch = persistenceMoviePort.fetchByContentType(contentType, page, 50);
            if (batch.isEmpty()) break;
            for (NetplixMovie m : batch) {
                if (m.getMovieName() != null && !m.getMovieName().isBlank()) {
                    names.add(m.getMovieName());
                    if (names.size() >= max) break;
                }
            }
            page++;
            if (batch.size() < 50) break;
        }
        return names;
    }

    private List<NetplixMovie> loadAllCatalog(String contentType) {
        List<NetplixMovie> all = new ArrayList<>();
        int p = 0;
        while (true) {
            List<NetplixMovie> batch = persistenceMoviePort.fetchByContentType(contentType, p, 50);
            if (batch.isEmpty()) break;
            all.addAll(batch);
            if (batch.size() < 50) break;
            p++;
        }
        return all;
    }

    private NetplixMovie findMovie(List<NetplixMovie> catalog, String normalizedTitle) {
        return catalog.stream()
                .filter(m -> normalize(m.getMovieName()).equals(normalizedTitle))
                .findFirst()
                .orElse(catalog.stream()
                        .filter(m -> normalize(m.getMovieName()).contains(normalizedTitle)
                                || normalizedTitle.contains(normalize(m.getMovieName())))
                        .findFirst()
                        .orElse(null));
    }

    private Map<String, String> parseResponseLines(String response) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String line : response.split(LINE_SEP)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            line = line.replaceFirst("^\\d+[.)]\\s*", "");
            int idx = line.indexOf(TITLE_REASON_DELIM);
            String title = idx >= 0 ? line.substring(0, idx).trim() : line;
            String reason = idx >= 0 ? line.substring(idx + TITLE_REASON_DELIM.length()).trim()
                    : "사장님이 강력 추천하는 영화예요!";
            if (!title.isEmpty()) map.put(title, reason);
        }
        return map;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\s\\p{Punct}]", "").toLowerCase();
    }

    private static String truncate(String value, int maxLen) {
        if (value == null) return "";
        value = value.trim();
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }
}
