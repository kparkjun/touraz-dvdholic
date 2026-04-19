# assetlinks.json 설정 가이드 (TWA Digital Asset Links)

TWA 앱이 풀스크린 앱 모드로 동작하려면 `assetlinks.json`이 올바르게 설정되어야 합니다.

**※ sha256_cert_fingerprints가 잘못되면 앱 상단에 브라우저 내비바가 뜨고, Chrome으로 실행되어 앱 실행 집계가 되지 않으며, 프로덕션 신청 시 리젝될 수 있습니다.**

## 1. package_name 확인

TWA/Android 앱의 패키지명을 확인하세요.
- Android 프로젝트의 `build.gradle` → `applicationId`
- 또는 `AndroidManifest.xml`의 `package` 속성

현재 `assetlinks.json`에는 `com.dvdholic.app`가 기본값으로 들어가 있습니다. 실제 패키지명과 다르면 수정하세요.

## 2. SHA256 인증서 fingerprint 확인 (필수)

**Play App Signing 사용 시** 반드시 Play Console의 **앱 서명 키 인증서**를 사용해야 합니다. (업로드 키가 아님)

1. [Google Play Console](https://play.google.com/console) 접속
2. 앱 선택 → 왼쪽 메뉴 **테스트 및 릴리스** → **설정** → **앱 무결성** (또는 **앱 서명**)
3. **앱 서명 키 인증서** 섹션의 **SHA-256 인증서 지문** 복사
4. 콜론(`:`) 구분 형식 그대로 사용 (예: `14:6D:E9:83:C5:73:06:50:D8:EE:B9:95:2F:34:FC:64:16:A0:83:42:E6:1D:BE:A8:8A:04:96:B2:3F:CF:44:E5`)

[Asset Links Tool](https://play.google.com/store/apps/details?id=dev.conn.assetlinkstool) 앱으로 자동 생성도 가능합니다.

## 3. assetlinks.json 수정

`netplix-frontend/public/.well-known/assetlinks.json`에서:

| 필드 | 현재값 | 조치 |
|------|--------|------|
| `package_name` | `com.dvdholic.app` | TWA 앱 applicationId와 일치하는지 확인 |
| `sha256_cert_fingerprints` | `00:00:00:...` (플레이스홀더) | **반드시 Play Console SHA-256으로 교체** |

## 4. 배포 후 검증

1. 프론트엔드 빌드: `cd netplix-frontend && npm run build`
2. 루트에서 API 빌드: `./gradlew bootJar` (또는 `stage`)
3. Heroku 배포: `git push heroku main`
4. 아래 URL에서 **JSON이 정상 반환되는지** 확인 (index.html/HTML이면 실패):
   ```
   https://dvdholic-01a66e19fbd3.herokuapp.com/.well-known/assetlinks.json
   ```
5. [Statement List Generator and Tester](https://developers.google.com/digital-asset-links/tools/generator)로 검증

## 문제 해결

- **상단에 브라우저 내비바가 보임** → assetlinks.json 미설정 또는 SHA256 불일치. Play Console **앱 서명 키** 지문으로 교체 후 재배포.
