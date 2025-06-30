# 뉴스피드 애플리케이션 배포 가이드

이 문서는 뉴스피드 애플리케이션을 AWS에 배포하는 방법을 설명합니다.

## 📋 사전 요구사항

### 필수 도구
- **Java 17** - OpenJDK 17 이상
- **Node.js** - v16 이상 (Serverless Framework용)
- **AWS CLI** - v2 이상
- **GitHub CLI** - secrets 설정용 (선택사항)

### AWS 권한
다음 AWS 서비스에 대한 권한이 필요합니다:
- Lambda (함수 생성, 실행)
- DynamoDB (테이블 생성, 읽기/쓰기)
- S3 (버킷 생성, 객체 관리)
- CloudFront (CDN 배포)
- SQS (큐 생성, 메시지 처리)
- SNS (토픽 생성, 발행)
- ElastiCache (Redis 클러스터)
- IAM (역할 생성)
- CloudWatch (로깅, 모니터링)

## 🚀 빠른 시작

### 1단계: Repository 클론
```bash
git clone https://github.com/aws-cloud-clubs/2025-khu-project-team1.git
cd 2025-khu-project-team1
```

### 2단계: 필수 도구 설치
```bash
# macOS
brew install openjdk@17 node awscli gh

# Serverless Framework 설치
npm install -g serverless
```

### 3단계: AWS 자격증명 설정
```bash
aws configure
```

### 4단계: Redis 클러스터 설정
```bash
./scripts/setup-redis.sh
```

### 5단계: GitHub Secrets 설정
```bash
./scripts/setup-github-secrets.sh
```

### 6단계: 배포 실행
```bash
# 개발 환경 배포
./scripts/deploy.sh dev

# 운영 환경 배포
./scripts/deploy.sh prod
```

## 📂 프로젝트 구조

```
├── src/main/java/com/khu/acc/newsfeed/
│   ├── controller/          # REST API 컨트롤러
│   ├── service/            # 비즈니스 로직
│   ├── repository/         # 데이터 접근 계층
│   ├── model/              # 도메인 모델
│   ├── dto/                # 데이터 전송 객체
│   ├── exception/          # 예외 처리
│   ├── config/             # 설정 클래스
│   ├── lambda/             # Lambda 함수들
│   └── stream/             # DynamoDB 스트림 처리
├── scripts/                # 배포 스크립트들
├── serverless.yml          # Serverless Framework 설정
└── build.gradle           # Gradle 빌드 설정
```

## 🔧 상세 설정

### GitHub Secrets 설정

다음 secrets를 GitHub Repository에 설정해야 합니다:

| Secret 이름 | 설명 | 예시 |
|------------|------|------|
| `AWS_ACCESS_KEY_ID` | AWS 액세스 키 ID | `AKIAIOSFODNN7EXAMPLE` |
| `AWS_SECRET_ACCESS_KEY` | AWS 시크릿 액세스 키 | `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY` |
| `JWT_SECRET` | JWT 서명용 시크릿 | `mySecretKey123456789...` (256비트) |
| `REDIS_HOST` | Redis 클러스터 엔드포인트 | `newsfeed-redis.xxxxx.cache.amazonaws.com` |
| `REDIS_PORT` | Redis 포트 | `6379` |
| `JWT_EXPIRATION` | JWT 만료 시간 (ms) | `86400000` (24시간) |

### 환경별 설정

#### 개발 환경 (dev)
- 작은 Lambda 메모리 (256MB)
- t3.micro Redis 인스턴스
- DynamoDB On-Demand 요금제

#### 운영 환경 (prod)
- 큰 Lambda 메모리 (512MB+)
- r6g.large Redis 인스턴스
- DynamoDB Provisioned 요금제
- CloudWatch 알림 설정

## 🏗️ 인프라 구성

### AWS 서비스 맵
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   CloudFront    │    │   API Gateway   │    │     Lambda      │
│      (CDN)      │────│   (REST API)    │────│   (Functions)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│       S3        │    │   ElastiCache   │    │   DynamoDB      │
│   (Images)      │    │    (Redis)      │    │   (Database)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                       ┌─────────────────┐    ┌─────────────────┐
                       │       SQS       │    │      SNS        │
                       │   (Queues)      │    │   (Topics)      │
                       └─────────────────┘    └─────────────────┘
```

### DynamoDB 테이블
- **Users** - 사용자 정보
- **Posts** - 게시글
- **Comments** - 댓글
- **Likes** - 좋아요
- **CommentLikes** - 댓글 좋아요
- **Follows** - 팔로우 관계
- **NewsFeed** - 뉴스피드 항목
- **Notifications** - 알림

### Lambda 함수들
- **api** - 메인 REST API 핸들러
- **postStreamProcessor** - 게시글 스트림 처리
- **commentStreamProcessor** - 댓글 스트림 처리
- **likeStreamProcessor** - 좋아요 스트림 처리
- **followStreamProcessor** - 팔로우 스트림 처리
- **newsFeedFanout** - 뉴스피드 팬아웃
- **imageProcessing** - 이미지 처리 파이프라인

## 📊 모니터링

### CloudWatch 메트릭
- Lambda 함수 실행 시간/오류율
- DynamoDB 읽기/쓰기 용량
- SQS 큐 메시지 수
- API Gateway 요청 수/응답 시간

### 로그 확인
```bash
# API 함수 로그
sls logs -f api --stage dev --tail

# 특정 함수 로그
sls logs -f postStreamProcessor --stage dev --tail

# CloudWatch에서 로그 검색
aws logs filter-log-events \
  --log-group-name "/aws/lambda/news-feed-dev-api" \
  --start-time $(date -d '1 hour ago' +%s)000
```

### 헬스체크
```bash
# API 상태 확인
curl https://your-api-url/actuator/health

# 특정 엔드포인트 테스트
curl https://your-api-url/api/v1/posts
```

## 🔒 보안 고려사항

### JWT 보안
- 256비트 이상의 강력한 시크릿 사용
- 적절한 만료 시간 설정
- 리프레시 토큰 구현

### AWS 보안
- IAM 역할 최소 권한 원칙
- VPC 보안 그룹 설정
- S3 버킷 퍼블릭 액세스 차단
- Redis 인증 설정

### 데이터 보안
- DynamoDB 암호화 활성화
- S3 서버 사이드 암호화
- HTTPS 강제 사용

## 🐛 문제 해결

### 자주 발생하는 문제

#### 1. Lambda 콜드 스타트 지연
```yaml
# serverless.yml에서 예약된 동시성 설정
functions:
  api:
    reservedConcurrency: 5
```

#### 2. DynamoDB 용량 부족
```bash
# 읽기/쓰기 용량 증가
aws dynamodb update-table \
  --table-name Posts \
  --provisioned-throughput ReadCapacityUnits=10,WriteCapacityUnits=10
```

#### 3. Redis 연결 오류
- 보안 그룹 설정 확인
- VPC 설정 확인
- 엔드포인트 주소 확인

#### 4. S3 권한 오류
```bash
# S3 버킷 정책 확인
aws s3api get-bucket-policy --bucket your-bucket-name
```

### 로그 분석
```bash
# 오류 로그 필터링
aws logs filter-log-events \
  --log-group-name "/aws/lambda/news-feed-dev-api" \
  --filter-pattern "ERROR"

# 특정 시간대 로그
aws logs filter-log-events \
  --log-group-name "/aws/lambda/news-feed-dev-api" \
  --start-time $(date -d '1 hour ago' +%s)000 \
  --end-time $(date +%s)000
```

## 📈 성능 최적화

### Lambda 최적화
- 메모리 설정 조정 (비용 vs 성능)
- 환경 변수 최소화
- 연결 풀링 설정

### DynamoDB 최적화
- GSI 설계 최적화
- 배치 작업 사용
- 캐싱 전략 구현

### 이미지 최적화
- WebP 형식 사용
- CloudFront 캐싱 설정
- 이미지 크기 제한

## 💰 비용 관리

### 예상 비용 (월간, USD)
- **개발 환경**: $50-100
- **운영 환경**: $200-500 (트래픽에 따라)

### 비용 절약 팁
- 사용하지 않는 환경 정리
- CloudWatch 로그 보존 기간 설정
- DynamoDB On-Demand vs Provisioned 비교
- Lambda 동시 실행 수 제한

## 🔄 CI/CD 파이프라인

GitHub Actions 워크플로우가 자동으로 다음을 수행합니다:

1. **코드 푸시** 감지
2. **Java 빌드** 및 테스트
3. **보안 스캔** 실행
4. **Serverless 배포** 실행
5. **헬스체크** 수행
6. **Slack 알림** (설정 시)

### 수동 배포
```bash
# 특정 브랜치 배포
git checkout feature/new-feature
./scripts/deploy.sh dev

# 운영 배포 (main 브랜치에서만)
git checkout main
./scripts/deploy.sh prod
```

## 📞 지원

문제가 발생하면 다음을 확인하세요:

1. **AWS 상태 페이지**: https://status.aws.amazon.com/
2. **Serverless 문서**: https://www.serverless.com/framework/docs/
3. **프로젝트 Issues**: GitHub Issues 탭
4. **AWS 지원**: AWS Support Center

---

**참고**: 이 가이드는 AWS 환경에서의 배포를 기준으로 작성되었습니다. 다른 클라우드 제공업체를 사용하는 경우 해당 업체의 문서를 참조하세요.