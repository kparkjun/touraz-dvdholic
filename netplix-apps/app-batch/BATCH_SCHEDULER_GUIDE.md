# Netflix Batch Scheduler Guide

## 개요

Netflix 배치 애플리케이션의 자동 스케줄링 기능에 대한 가이드입니다.

## 스케줄 설정

### 1. 영화 목록 업데이트 배치

- **실행 시간**: 매일 새벽 2시
- **Cron 표현식**: `0 0 2 * * *`
- **기능**: TMDB API에서 최신 영화 목록을 가져와 데이터베이스 업데이트
- **실행 이유**:
  - 새벽 시간대는 사용자 접속이 적음
  - TMDB의 "now playing" 목록은 매일 업데이트됨
  - 사용자가 항상 최신 영화를 볼 수 있도록 함

### 2. 구독 만료 처리 배치

- **실행 시간**: 매일 자정 (00:00)
- **Cron 표현식**: `0 0 0 * * *`
- **기능**: 만료된 구독을 처리하고 사용자 권한 업데이트

## 실행 방법

### 개발 환경에서 실행

```bash
cd netplix-apps/app-batch
./gradlew bootRun
```

애플리케이션이 시작되면 자동으로 스케줄러가 동작합니다.

### 프로덕션 환경에서 실행

#### 1. JAR 파일 빌드

```bash
cd netplix-apps/app-batch
./gradlew build
```

#### 2. 백그라운드 실행

```bash
nohup java -jar build/libs/app-batch.jar > batch.log 2>&1 &
```

#### 3. Docker로 실행 (권장)

```dockerfile
FROM openjdk:17-jdk-slim
COPY build/libs/app-batch.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
docker build -t netflix-batch .
docker run -d --name netflix-batch-scheduler netflix-batch
```

## Cron 표현식 설명

```
 ┌────────────── 초 (0-59)
 │ ┌──────────── 분 (0-59)
 │ │ ┌────────── 시 (0-23)
 │ │ │ ┌──────── 일 (1-31)
 │ │ │ │ ┌────── 월 (1-12)
 │ │ │ │ │ ┌──── 요일 (0-7) (0과 7은 일요일)
 │ │ │ │ │ │
 * * * * * *
```

### 예시

- `0 0 2 * * *` - 매일 새벽 2시
- `0 0 0 * * *` - 매일 자정
- `0 0 */6 * * *` - 6시간마다
- `0 0 0 * * MON` - 매주 월요일 자정

## 실제 앱 배포 시 고려사항

### 1. 클라우드 환경

각 클라우드 서비스의 스케줄링 기능을 활용할 수도 있습니다:

#### AWS

- **AWS EventBridge (CloudWatch Events)** + Lambda
- **ECS Scheduled Tasks**
- **Elastic Beanstalk Worker Environment**

#### Google Cloud

- **Cloud Scheduler** + Cloud Run
- **Cloud Functions** with Pub/Sub

#### Azure

- **Azure Functions** with Timer Trigger
- **Azure Batch** with scheduled jobs

### 2. Kubernetes 환경

**CronJob** 사용 예시:

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: netflix-movie-update
spec:
  schedule: "0 2 * * *" # 매일 새벽 2시
  jobTemplate:
    spec:
      template:
        spec:
          containers:
            - name: batch
              image: netflix-batch:latest
              args:
                - --job.name=MigrateMoviesFromTmdbBatch
          restartPolicy: OnFailure
```

### 3. 전통적인 서버 환경

**Linux Cron** 사용:

```bash
# crontab -e
0 2 * * * cd /app/netflix-batch && java -jar app-batch.jar --job.name=MigrateMoviesFromTmdbBatch >> /var/log/netflix-batch.log 2>&1
```

## 모니터링

### 로그 확인

```bash
# 실시간 로그 확인
tail -f batch.log

# 오늘 실행된 배치 로그만 확인
grep "$(date +%Y-%m-%d)" batch.log
```

### 데이터베이스에서 실행 이력 확인

```sql
-- 최근 배치 실행 이력
SELECT
    JOB_INSTANCE_ID,
    JOB_NAME,
    START_TIME,
    END_TIME,
    STATUS,
    EXIT_CODE
FROM BATCH_JOB_EXECUTION
ORDER BY START_TIME DESC
LIMIT 10;

-- 실패한 배치 확인
SELECT * FROM BATCH_JOB_EXECUTION
WHERE STATUS = 'FAILED'
ORDER BY START_TIME DESC;
```

## 스케줄 변경 방법

`BatchScheduler.java` 파일에서 `@Scheduled` 애노테이션의 cron 값을 수정:

```java
// 기존: 매일 새벽 2시
@Scheduled(cron = "0 0 2 * * *")

// 변경: 매일 새벽 3시
@Scheduled(cron = "0 0 3 * * *")

// 변경: 6시간마다
@Scheduled(cron = "0 0 */6 * * *")

// 변경: 매주 월요일 새벽 2시
@Scheduled(cron = "0 0 2 * * MON")
```

## 테스트

개발 중에는 `BatchScheduler.java`의 `runTestBatch()` 메서드 주석을 해제하여 5분마다 실행되도록 설정할 수 있습니다:

```java
@Scheduled(cron = "0 */5 * * * *")
public void runTestBatch() {
    log.info("=== 테스트 배치 실행 (5분마다) ===");
    runMigrateMoviesBatch();
}
```

⚠️ **주의**: 운영 환경에서는 반드시 주석 처리하거나 제거하세요!

## 권장 운영 전략

### 소규모 서비스

- Spring @Scheduled 사용 (현재 구현)
- 단일 서버에서 배치 애플리케이션 실행
- 비용 효율적이고 관리가 간단함

### 중대규모 서비스

- Kubernetes CronJob 사용
- 컨테이너 오케스트레이션으로 안정성 확보
- 자동 재시작 및 로그 관리

### 대규모 서비스

- 클라우드 관리형 서비스 사용 (AWS EventBridge, Cloud Scheduler 등)
- 배치 실행 실패 시 알림 설정 (SNS, Slack 연동)
- 여러 리전에 배포하여 가용성 확보

## 문제 해결

### 배치가 실행되지 않는 경우

1. 로그 확인: `@EnableScheduling` 애노테이션이 있는지 확인
2. 애플리케이션이 계속 실행 중인지 확인 (종료되지 않았는지)
3. Cron 표현식이 올바른지 확인
4. 시간대(Timezone) 설정 확인

### 배치가 중복 실행되는 경우

- JobParameters에 timestamp를 추가하여 매번 새로운 실행으로 인식되도록 함
- 이미 구현되어 있음: `.addLong("timestamp", System.currentTimeMillis())`

## 참고 자료

- [Spring Batch 공식 문서](https://spring.io/projects/spring-batch)
- [Spring Scheduling 공식 문서](https://spring.io/guides/gs/scheduling-tasks/)
- [Cron Expression Generator](https://crontab.guru/)
