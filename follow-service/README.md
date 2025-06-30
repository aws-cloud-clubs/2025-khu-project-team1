# follow-service
`news-feed`(Spring Boot) 서비스와 연동, AWS Lambda, API Gateway, DynamoDB, Redis를 활용.

---

## 프로젝트 구조

```bash
follow-service/
├── handlers/               # Lambda 함수 핸들러 (follow 기능 API)
├── utils/                  # 공통 유틸 (auth, db, cache)
├── generate-jwt.js         # JWT 토큰 생성 스크립트 (로컬 테스트용)
├── serverless.yml         
├── .env.example           
├── package.json
