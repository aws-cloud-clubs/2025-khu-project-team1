#!/bin/bash

# 뉴스피드 애플리케이션 배포 스크립트
# 로컬에서 Serverless Framework를 사용하여 직접 배포하는 스크립트

set -e  # 오류 시 스크립트 종료

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 기본 변수
STAGE=${1:-dev}
REGION="ap-northeast-2"

echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}  뉴스피드 애플리케이션 배포${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""
echo "Stage: $STAGE"
echo "Region: $REGION"
echo ""

# 필수 도구 설치 확인
check_tool() {
    local tool=$1
    local install_cmd=$2
    
    if ! command -v $tool &> /dev/null; then
        echo -e "${RED}❌ $tool이 설치되지 않았습니다.${NC}"
        echo -e "${YELLOW}설치 명령어: $install_cmd${NC}"
        return 1
    else
        echo -e "${GREEN}✅ $tool 설치 확인${NC}"
        return 0
    fi
}

echo -e "${BLUE}필수 도구 확인 중...${NC}"
check_tool "java" "brew install openjdk@17"
check_tool "node" "brew install node"
check_tool "npm" "node.js와 함께 설치됨"

# Gradle 확인
if [[ -f "./gradlew" ]]; then
    echo -e "${GREEN}✅ Gradle Wrapper 확인${NC}"
else
    echo -e "${RED}❌ Gradle Wrapper를 찾을 수 없습니다.${NC}"
    exit 1
fi

echo ""

# Serverless Framework 설치 확인
if ! command -v sls &> /dev/null; then
    echo -e "${YELLOW}⚠️ Serverless Framework가 설치되지 않았습니다. 설치 중...${NC}"
    npm install -g serverless
    echo -e "${GREEN}✅ Serverless Framework 설치 완료${NC}"
else
    echo -e "${GREEN}✅ Serverless Framework 설치 확인${NC}"
fi

# AWS CLI 설치 확인
if ! command -v aws &> /dev/null; then
    echo -e "${RED}❌ AWS CLI가 설치되지 않았습니다.${NC}"
    echo -e "${YELLOW}설치 명령어: brew install awscli${NC}"
    exit 1
else
    echo -e "${GREEN}✅ AWS CLI 설치 확인${NC}"
fi

echo ""

# AWS 자격증명 확인
echo -e "${BLUE}AWS 자격증명 확인 중...${NC}"
if aws sts get-caller-identity &> /dev/null; then
    ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
    USER_ARN=$(aws sts get-caller-identity --query Arn --output text)
    echo -e "${GREEN}✅ AWS 자격증명 확인 완료${NC}"
    echo "Account ID: $ACCOUNT_ID"
    echo "User/Role: $USER_ARN"
else
    echo -e "${RED}❌ AWS 자격증명이 설정되지 않았습니다.${NC}"
    echo -e "${YELLOW}다음 명령어로 설정해주세요:${NC}"
    echo "  aws configure"
    exit 1
fi

echo ""

# .env 파일 로드 (있는 경우)
if [[ -f ".env" ]]; then
    echo -e "${BLUE}.env 파일을 발견했습니다. 환경 변수 로드 중...${NC}"
    set -a  # 모든 변수를 자동으로 export
    source .env
    set +a
    echo -e "${GREEN}✅ .env 파일 로드 완료${NC}"
else
    echo -e "${YELLOW}⚠️ .env 파일이 없습니다. 시스템 환경 변수를 사용합니다.${NC}"
fi

echo ""

# 환경 변수 확인
echo -e "${BLUE}환경 변수 확인 중...${NC}"
check_env_var() {
    local var_name=$1
    local default_value=$2
    
    if [[ -z "${!var_name}" ]]; then
        if [[ -n "$default_value" ]]; then
            export $var_name="$default_value"
            echo -e "${YELLOW}⚠️ $var_name 기본값 사용: $default_value${NC}"
        else
            echo -e "${RED}❌ 환경 변수 $var_name이 설정되지 않았습니다.${NC}"
            return 1
        fi
    else
        # 값의 일부만 표시 (보안)
        display_value="${!var_name}"
        if [[ ${#display_value} -gt 8 ]]; then
            display_value="${display_value:0:4}...${display_value: -4}"
        else
            display_value="****"
        fi
        echo -e "${GREEN}✅ $var_name ($display_value)${NC}"
    fi
}

# 필수 환경 변수들 (운영환경용)
if [[ "$STAGE" == "prod" ]]; then
    check_env_var "JWT_SECRET" || echo -e "${YELLOW}JWT_SECRET을 설정해주세요${NC}"
    check_env_var "REDIS_HOST" || echo -e "${YELLOW}REDIS_HOST를 설정해주세요${NC}"
fi

# 개발환경용 기본값 설정
check_env_var "JWT_EXPIRATION" "86400000"
check_env_var "REDIS_PORT" "6379"

echo ""

# 빌드 시작
echo -e "${BLUE}애플리케이션 빌드 중...${NC}"
echo "Gradle 빌드를 시작합니다..."

if ./gradlew clean buildZip; then
    echo -e "${GREEN}✅ 빌드 완료${NC}"
else
    echo -e "${RED}❌ 빌드 실패${NC}"
    exit 1
fi

# 빌드 결과 확인
BUILD_ZIP="build/distributions/news-feed-0.0.1-SNAPSHOT.zip"
if [[ -f "$BUILD_ZIP" ]]; then
    ZIP_SIZE=$(du -h "$BUILD_ZIP" | cut -f1)
    echo -e "${GREEN}✅ 배포 아티팩트 생성 완료: $BUILD_ZIP ($ZIP_SIZE)${NC}"
else
    echo -e "${RED}❌ 배포 아티팩트를 찾을 수 없습니다: $BUILD_ZIP${NC}"
    exit 1
fi

echo ""

# Serverless 배포
echo -e "${BLUE}Serverless Framework 배포 중...${NC}"
echo "Stage: $STAGE"
echo "Region: $REGION"
echo ""

# Serverless 플러그인 설치 (필요한 경우)
if [[ -f "package.json" ]]; then
    echo "Serverless 플러그인 설치 중..."
    npm install
fi

# 배포 실행
echo "배포를 시작합니다..."
if sls deploy --stage $STAGE --region $REGION --verbose; then
    echo ""
    echo -e "${GREEN}🎉 배포가 성공적으로 완료되었습니다!${NC}"
else
    echo ""
    echo -e "${RED}❌ 배포가 실패했습니다.${NC}"
    echo -e "${YELLOW}문제 해결 방법:${NC}"
    echo "1. AWS 권한 확인"
    echo "2. 환경 변수 설정 확인"
    echo "3. serverless.yml 설정 확인"
    echo "4. 로그 확인: sls logs -f api --stage $STAGE"
    exit 1
fi

echo ""

# 배포 정보 출력
echo -e "${BLUE}배포 정보:${NC}"
echo "Service: news-feed"
echo "Stage: $STAGE"
echo "Region: $REGION"
echo ""

# API 엔드포인트 확인
echo -e "${BLUE}API 엔드포인트 정보:${NC}"
if sls info --stage $STAGE --region $REGION | grep -E "ServiceEndpoint|GET|POST"; then
    echo ""
else
    echo "API 엔드포인트 정보를 가져올 수 없습니다."
fi

# DynamoDB 테이블 확인
echo -e "${BLUE}생성된 DynamoDB 테이블 확인:${NC}"
aws dynamodb list-tables --region $REGION | jq -r '.TableNames[] | select(test("Users|Posts|Comments|Likes|Follows|NewsFeed|Notifications"))'

echo ""

# 기본 헬스체크
echo -e "${BLUE}기본 헬스체크 수행:${NC}"
API_URL=$(sls info --stage $STAGE --region $REGION | grep "ServiceEndpoint" | cut -d' ' -f2)
if [[ -n "$API_URL" ]]; then
    echo "API URL: $API_URL"
    if curl -s -o /dev/null -w "%{http_code}" "$API_URL/actuator/health" | grep -q "200"; then
        echo -e "${GREEN}✅ 헬스체크 성공${NC}"
    else
        echo -e "${YELLOW}⚠️ 헬스체크 실패 - API가 아직 준비되지 않았을 수 있습니다${NC}"
    fi
else
    echo -e "${YELLOW}⚠️ API URL을 찾을 수 없습니다${NC}"
fi

echo ""
echo -e "${GREEN}🚀 배포 스크립트 실행 완료!${NC}"
echo ""
echo -e "${BLUE}다음 단계:${NC}"
echo "1. API 문서: $API_URL/swagger-ui.html"
echo "2. 헬스체크: $API_URL/actuator/health"
echo "3. 메트릭: $API_URL/actuator/metrics"
echo "4. 로그 확인: sls logs -f api --stage $STAGE --tail"
echo "5. CloudWatch 대시보드에서 모니터링"
echo ""
echo -e "${YELLOW}참고: 첫 배포 후 Lambda 함수들이 완전히 초기화되는데 몇 분이 걸릴 수 있습니다.${NC}"