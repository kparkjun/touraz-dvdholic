# 비밀/키 설정 (GitHub 업로드 후)

이 프로젝트는 **비밀/키를 코드에 넣지 않고** 환경 변수로 주입합니다.

## 1. 필요한 환경 변수

| 변수명 | 설명 | 예시 위치 |
|--------|------|-----------|
| `DB_PASSWORD` | MySQL 비밀번호 | 필수 |
| `DB_USERNAME` | MySQL 사용자 (기본값: root) | 선택 |
| `TMDB_API_KEY` | TMDB API 키 | [TMDB 설정](https://www.themoviedb.org/settings/api) |
| `TMDB_ACCESS_TOKEN` | TMDB Bearer 토큰 (일부 API용) | 선택, 비워둬도 됨 |
| `KAKAO_CLIENT_ID` | Kakao OAuth 클라이언트 ID | [Kakao 개발자 콘솔](https://developers.kakao.com) |
| `KAKAO_CLIENT_SECRET` | Kakao OAuth 시크릿 | 위와 동일 |
| `JWT_SECRET` | JWT 서명용 시크릿 (32자 이상) | 직접 생성 |
| `AES_SECRET` | AES 암호화 시크릿 (app-api) | 직접 생성 |

자세한 예시는 **`env.example`** 파일을 참고하세요.

### JWT_SECRET / AES_SECRET 값을 모를 때 (새로 만들기)

이 값들은 **코드/저장소에 없고**, 예전에 `.env`나 `application-local.yml`, IDE 환경 변수에 넣었을 수 있습니다.  
**못 찾겠으면 새로 생성**해서 로컬과 Heroku 둘 다 같은 값으로 맞추거나, 로컬용·Heroku용 각각 새로 만들어 쓰면 됩니다.

**1) 터미널에서 랜덤 시크릿 생성**

Windows PowerShell (프로젝트 루트에서):

```powershell
# JWT_SECRET용 (32자 이상 권장)
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 40 | ForEach-Object { [char]$_ })

# 한 번 더 실행해서 AES_SECRET용으로 하나 더 복사
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 40 | ForEach-Object { [char]$_ })
```

macOS/Linux (또는 Git Bash):

```bash
# JWT_SECRET용
openssl rand -base64 32

# AES_SECRET용 (한 번 더 실행)
openssl rand -base64 32
```

**2) 생성한 값 넣는 곳**

- **로컬**: `netplix-apps/app-api/src/main/resources/application-local.yml` 에 아래처럼 추가 (파일 없으면 생성).  
  또는 프로젝트 루트에 `.env` 만들고 `JWT_SECRET=생성한값`, `AES_SECRET=생성한값` 넣은 뒤, IDE Run Configuration에서 EnvFile로 로드.

```yaml
# application-local.yml 예시 (나머지 DB, TMDB, Kakao 등은 본인 값으로)
JWT_SECRET: 여기에_위에서_생성한_첫번째_문자열
AES_SECRET: 여기에_위에서_생성한_두번째_문자열
```

- **Heroku**: Config Vars에 **APP_JWT_SECRET**, **APP_AES_SECRET** 키로 위에서 만든 값(또는 새로 생성한 값)을 **그대로** 넣기. (`"${JWT_SECRET}"` 같은 글자는 넣지 말 것.)

로컬과 Heroku에서 **같은 JWT_SECRET 값**을 쓰면, 로컬에서 발급한 토큰을 Heroku API에 그대로 쓸 수 있습니다. 따로 쓰려면 각각 다른 값으로 만들어도 됩니다.

## 2. 설정 방법 (택 1)

### A) 시스템 환경 변수

Windows (PowerShell, 한 번만 실행):

```powershell
$env:DB_PASSWORD="실제비밀번호"
$env:TMDB_API_KEY="실제키"
# ... 나머지도 동일
```

macOS/Linux:

```bash
export DB_PASSWORD=실제비밀번호
export TMDB_API_KEY=실제키
# ... 나머지도 동일
```

### B) 로컬 설정 파일 (Git에 안 올라감)

- **app-api**: `netplix-apps/app-api/src/main/resources/application-local.yml`
- **app-batch**: `netplix-apps/app-batch/src/main/resources/application-local.yml`

예시 (`application-local.yml`). **환경 변수 이름을 그대로 키로 쓰면** 됩니다:

```yaml
# 환경 변수와 같은 이름으로 넣으면 application.yml 의 ${DB_PASSWORD} 등이 여기 값으로 채워짐
DB_PASSWORD: 실제DB비밀번호
DB_USERNAME: root
TMDB_API_KEY: 실제TMDB키
TMDB_ACCESS_TOKEN: ""   # 비워두려면 빈 문자열
KAKAO_CLIENT_ID: 실제카카오클라이언트ID
KAKAO_CLIENT_SECRET: 실제카카오시크릿
JWT_SECRET: 실제JWT시크릿32자이상
AES_SECRET: 실제AES시크릿   # app-api만 사용
```

- **app-batch**는 이미 `spring.profiles.active: local` 이 있어서, 같은 폴더에 `application-local.yml` 두면 자동 로드됩니다.
- **app-api**는 `application-local.yml`을 쓰려면 `spring.config.import: optional:file:./application-local.yml` 를 application.yml 맨 위에 추가하거나, Run 설정에서 `spring.profiles.active=local` 로 실행한 뒤 `application-local.yml`을 resources에 두면 됩니다.

### C) IDE Run Configuration

- IntelliJ: Run → Edit Configurations → 해당 앱 선택 → **Environment variables**에  
  `DB_PASSWORD=값;TMDB_API_KEY=값;...` 형식으로 입력 (여러 개는 `;` 구분)

## 3. 확인

- 값을 넣지 않으면 앱 기동 시 `Could not resolve placeholder 'DB_PASSWORD'` 같은 오류가 납니다.
- `.env` 파일을 쓰려면 IDE나 실행 스크립트에서 **env 파일 로드**를 설정해야 합니다. (Spring은 기본으로 .env를 읽지 않음)
