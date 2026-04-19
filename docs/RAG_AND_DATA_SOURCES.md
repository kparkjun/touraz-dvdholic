# 추천 시스템 및 데이터 소스 가이드

공모전용 Netplix 앱의 **추천 시스템**과 **영화/DVD 관련 데이터 소스**를 정리한 문서입니다.

---

## 1. 현재 활용 중인 데이터

### TMDB API (이미 적용됨)
- **용도**: 영화 메타데이터, 장르, 비슷한 영화 추천(recommendations), 예고편, OTT 정보
- **적용**: 배치로 수집 → DB 저장, 추천 시 `recommendations`·장르 기반 로직에 활용
- **공식**: https://www.themoviedb.org/documentation/api

---

## 2. 영화·박스오피스·유통 데이터 (추가 활용 권장)

### 2.1 영화진흥위원회(KOBIS) 오픈API
- **용도**: **한국 영화관 박스오피스** (일별/주간/주말), 관객수, 매출액, 스크린수
- **데이터**: 영화명, 순위, 매출액, 관객수, 상영횟수 등
- **적용 예**: "흥행/관객수 기반 추천", "요즘 잘 나가는 영화" 섹션
- **가입·키**: https://www.kobis.or.kr/kobisopenapi/homepg/main/main.do  
- **REST 예시**:  
  `http://kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json?key={KEY}&targetDt={YYYYMMDD}`

### 2.2 한국영상자료원(KMDb) 영화정보DB
- **용도**: 국내 개봉·수입 영화 상세 정보 (제목, 감독, 제작사, 개봉일, 배우, 장르, 줄거리)
- **형식**: JSON/XML
- **적용 예**: RAG용 문서 보강, 한국 영화 설명 품질 향상
- **공공데이터포털**: 영화진흥위원회_영화박스오피스 DB, 한국영상자료원_영화정보DB 검색

### 2.3 공공데이터포털 (data.go.kr)
- **영화진흥위원회_영화박스오피스 DB**: 박스오피스 통계
- **한국영상자료원_영화정보DB**: 영화 메타데이터
- **문화체육관광부_비디오물 유통산업 조회**: 비디오(영상물) 유통·판매 업체 정보 (직접적인 판매량은 아님)

### 2.4 DVD·판매·대여 데이터 (참고)
- **실제 DVD 판매/대여 거래 데이터**는 상용 DB나 제휴 없이는 공개 API로 구하기 어렵습니다.
- **대안**:
  - **KOBIS**: 극장 매출·관객수 → "흥행" 지표로 활용
  - **TMDB**: `revenue`, `vote_count` 등 → 인기·흥행 보조 지표
  - **Kaggle 등**: “Movies Box Office Dataset”, “DVD Rental” 등 샘플 데이터로 시연·프로토타입용

---

## 3. Spring AI + RAG 기반 추천 (구성)

### 3.1 RAG가 하는 일
- **Retrieval**: 사용자 질의(예: "심리 thriller 추천해줘")와 **유사한 영화 문서**를 벡터 DB에서 검색
- **Augmented Generation**: 검색된 문서(제목, 장르, 줄거리 등)를 **컨텍스트**로 넣어, LLM이 추천 문장/목록 생성 (선택)

즉, **DB/API에 있는 영화 정보를 “지식”으로 두고**, 질의에 맞는 영화를 찾아서 추천하는 구조입니다.

### 3.2 기술 스택 (구색)
- **Spring AI**: 벡터 저장소 연동, RAG 패턴(검색 → 프롬프트 보강 → 생성)
- **Vector Store**: 인메모리(SimpleVectorStore) 또는 ChromaDB 등 — 영화 메타/줄거리를 임베딩해 저장
- **Embedding**: OpenAI 등 (API 키 필요 시 문서에 명시)
- **데이터**: 기존 **movies 테이블**(TMDB 기반) + 필요 시 KOBIS/KMDb 요약 텍스트를 문서로 추가

### 3.3 데이터 흐름
1. **수집**: TMDB(이미) + 선택적으로 KOBIS/KMDb에서 영화·박스오피스 정보 수집
2. **문서화**: 영화별로 “제목, 장르, overview, 박스오피스 요약” 등을 하나의 텍스트로 만들어 **Document**로 저장
3. **임베딩**: Spring AI Embedding 모델로 문서 벡터화 후 **VectorStore**에 저장
4. **추천 요청**: 사용자 질의 → 동일 임베딩 모델로 쿼리 벡터 생성 → 유사 문서 검색 → 영화 ID/제목 목록 반환 (및 선택 시 LLM으로 문장 생성)

### 3.4 API 구색 제안
- `GET /api/v1/movie/recommend`  
  - 기존: TMDB recommendations + 장르 기반 추천 (이미 구현)
- `GET /api/v1/movie/recommend/rag?q=...`  
  - RAG: 질의 `q`로 벡터 검색 → 유사 영화 목록 반환 (Spring AI VectorStore)
- (선택) `POST /api/v1/movie/recommend/rag/chat`  
  - RAG + LLM: 검색된 문서를 컨텍스트로 넣어 “이런 영화 추천해요” 식의 자연어 응답

---

## 4. 공모전 제출 시 강조 포인트

1. **다중 데이터 소스**: TMDB + (선택) KOBIS/KMDb로 “메타데이터 + 한국 흥행” 스토리
2. **추천 2축**:  
   - 규칙/통계 기반: TMDB recommendations, 장르, (KOBIS) 관객/매출  
   - AI 기반: Spring AI RAG(벡터 검색, 선택 시 생성)
3. **RAG 명시**: “영화/DVD 정보를 벡터 DB에 넣고, 사용자 질의로 유사 영화를 검색해 추천”으로 기술 스택과 차별점 설명
4. **DVD/유통**: 실제 판매·대여 수치는 없어도, “비디오물 유통 데이터(공공)” + “박스오피스”로 “영화·유통 기반 데이터 활용”이라고 정리 가능

---

## 5. Spring AI RAG 활성화 방법 (선택)

현재는 **구색용**으로 `GET /api/v1/movie/recommend/rag?q=...` API와 `MovieRagPort` 구조만 두었으며, 기본 구현은 빈 목록을 반환합니다.

**실제 벡터 검색을 쓰려면:**

1. **의존성 추가** (app-api `build.gradle.kts`)
   - Maven Central만 사용하는 프로젝트에서:
   - `implementation("org.springframework.ai:spring-ai-core:0.8.1")`
   - `implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter:0.8.1")`
2. **설정**
   - `spring.ai.openai.api-key` 에 OpenAI API 키 설정
3. **MovieRagPort 구현**
   - `EmbeddingClient`(OpenAI) + `SimpleVectorStore` 로 벡터 저장소 구성
   - 영화 목록을 `Document`(제목, 장르, overview)로 변환해 `vectorStore.add()` 로 적재
   - `similaritySearch(SearchRequest.builder().query(q).topK(limit).build())` 로 유사 영화 검색 후 메타데이터에서 `movieName` 추출
4. **기동 시 적재**
   - `ApplicationRunner` 에서 `GetMoviesForRagUseCase.getMovies(500)` 호출 후 위 구현의 `addMovieDocuments(movies)` 호출

자세한 코드는 Spring AI 0.8 문서의 VectorStore / RAG 예제를 참고하면 됩니다.

---

## 6. 참고 링크

- [Spring AI Reference - RAG](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html)
- [Spring AI Vector Stores (Chroma, Simple 등)](https://docs.spring.io/spring-ai/reference/api/vectordbs/chroma.html)
- [영화진흥위원회 오픈API](https://www.kobis.or.kr/kobisopenapi/homepg/main/main.do)
- [공공데이터포털](https://www.data.go.kr)
