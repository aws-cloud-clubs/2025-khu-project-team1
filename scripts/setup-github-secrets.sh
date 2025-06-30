#!/bin/bash

# GitHub Actions Secrets 설정 스크립트
# 이 스크립트는 대화형으로 secrets를 입력받아 GitHub Repository에 등록합니다.

set -e  # 오류 시 스크립트 종료

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Repository 정보
REPO="aws-cloud-clubs/2025-khu-project-team1"

echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}  GitHub Actions Secrets 설정${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""

# GitHub CLI 설치 확인
if ! command -v gh &> /dev/null; then
    echo -e "${RED}❌ GitHub CLI (gh)가 설치되지 않았습니다.${NC}"
    echo -e "${YELLOW}다음 명령어로 설치해주세요:${NC}"
    echo "  macOS: brew install gh"
    echo "  Ubuntu/Debian: sudo apt install gh"
    echo "  CentOS/RHEL: sudo yum install gh"
    exit 1
fi

# GitHub CLI 인증 확인
if ! gh auth status &> /dev/null; then
    echo -e "${RED}❌ GitHub CLI 인증이 필요합니다.${NC}"
    echo -e "${YELLOW}다음 명령어로 인증해주세요:${NC}"
    echo "  gh auth login"
    exit 1
fi

echo -e "${GREEN}✅ GitHub CLI 인증 완료${NC}"
echo ""

# Repository 접근 권한 확인
if ! gh repo view $REPO &> /dev/null; then
    echo -e "${RED}❌ Repository '$REPO'에 접근할 수 없습니다.${NC}"
    echo -e "${YELLOW}Repository 이름을 확인하거나 권한을 확인해주세요.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Repository 접근 권한 확인 완료${NC}"
echo ""

echo -e "${YELLOW}다음 secrets를 설정합니다:${NC}"
echo "1. AWS_ACCESS_KEY_ID - AWS 액세스 키"
echo "2. AWS_SECRET_ACCESS_KEY - AWS 시크릿 키"
echo "3. JWT_SECRET - JWT 서명용 시크릿 (256비트 이상)"
echo "4. REDIS_HOST - Redis 클러스터 엔드포인트"
echo "5. REDIS_PORT - Redis 포트 (기본값: 6379)"
echo "6. JWT_EXPIRATION - JWT 만료 시간 (기본값: 86400000ms = 24시간)"
echo ""

# AWS 자격증명 입력
echo -e "${BLUE}AWS 자격증명을 입력해주세요:${NC}"
read -p "AWS Access Key ID: " AWS_ACCESS_KEY_ID
if [[ -z "$AWS_ACCESS_KEY_ID" ]]; then
    echo -e "${RED}❌ AWS Access Key ID는 필수입니다.${NC}"
    exit 1
fi

read -s -p "AWS Secret Access Key: " AWS_SECRET_ACCESS_KEY
echo ""
if [[ -z "$AWS_SECRET_ACCESS_KEY" ]]; then
    echo -e "${RED}❌ AWS Secret Access Key는 필수입니다.${NC}"
    exit 1
fi

# JWT Secret 입력
echo ""
echo -e "${BLUE}JWT 설정을 입력해주세요:${NC}"
read -s -p "JWT Secret (256비트 이상, 최소 32자): " JWT_SECRET
echo ""
if [[ ${#JWT_SECRET} -lt 32 ]]; then
    echo -e "${RED}❌ JWT Secret은 최소 32자 이상이어야 합니다.${NC}"
    exit 1
fi

# JWT 만료 시간 설정
read -p "JWT 만료 시간(ms) [기본값: 86400000 = 24시간]: " JWT_EXPIRATION_INPUT
JWT_EXPIRATION=${JWT_EXPIRATION_INPUT:-86400000}

# Redis 설정 입력
echo ""
echo -e "${BLUE}Redis 설정을 입력해주세요:${NC}"
read -p "Redis Host (ElastiCache 엔드포인트): " REDIS_HOST
if [[ -z "$REDIS_HOST" ]]; then
    echo -e "${RED}❌ Redis Host는 필수입니다.${NC}"
    exit 1
fi

read -p "Redis Port [기본값: 6379]: " REDIS_PORT_INPUT
REDIS_PORT=${REDIS_PORT_INPUT:-6379}

# DynamoDB 엔드포인트 (운영환경에서는 비워둠)
echo ""
echo -e "${BLUE}DynamoDB 설정:${NC}"
echo "운영환경에서는 DynamoDB 엔드포인트를 비워둡니다 (AWS 기본값 사용)"
DYNAMODB_ENDPOINT=""

echo ""
echo -e "${YELLOW}입력된 정보를 확인해주세요:${NC}"
echo "Repository: $REPO"
echo "AWS Access Key ID: ${AWS_ACCESS_KEY_ID:0:8}..."
echo "AWS Secret Access Key: ********"
echo "JWT Secret: ********"
echo "JWT Expiration: $JWT_EXPIRATION ms"
echo "Redis Host: $REDIS_HOST"
echo "Redis Port: $REDIS_PORT"
echo "DynamoDB Endpoint: (비어있음 - AWS 기본값 사용)"
echo ""

read -p "이 정보로 secrets를 설정하시겠습니까? (y/N): " CONFIRM
if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}❌ 설정이 취소되었습니다.${NC}"
    exit 0
fi

echo ""
echo -e "${BLUE}GitHub Secrets를 설정 중입니다...${NC}"

# Secrets 설정 함수
set_secret() {
    local key=$1
    local value=$2
    local display_value=$3
    
    if gh secret set "$key" -b "$value" -R $REPO &> /dev/null; then
        echo -e "${GREEN}✅ $key 설정 완료${NC} ($display_value)"
    else
        echo -e "${RED}❌ $key 설정 실패${NC}"
        return 1
    fi
}

# Secrets 일괄 설정
set_secret "AWS_ACCESS_KEY_ID" "$AWS_ACCESS_KEY_ID" "${AWS_ACCESS_KEY_ID:0:8}..."
set_secret "AWS_SECRET_ACCESS_KEY" "$AWS_SECRET_ACCESS_KEY" "********"
set_secret "JWT_SECRET" "$JWT_SECRET" "********"
set_secret "JWT_EXPIRATION" "$JWT_EXPIRATION" "$JWT_EXPIRATION ms"
set_secret "REDIS_HOST" "$REDIS_HOST" "$REDIS_HOST"
set_secret "REDIS_PORT" "$REDIS_PORT" "$REDIS_PORT"
set_secret "DYNAMODB_ENDPOINT" "$DYNAMODB_ENDPOINT" "(비어있음)"

echo ""
echo -e "${GREEN}🎉 모든 GitHub Secrets가 성공적으로 설정되었습니다!${NC}"
echo ""
echo -e "${BLUE}다음 단계:${NC}"
echo "1. Redis 클러스터가 준비되었는지 확인"
echo "2. GitHub Actions를 통한 배포 실행 또는"
echo "3. 로컬에서 'sls deploy' 명령어로 배포"
echo ""
echo -e "${YELLOW}참고: Repository의 Settings > Secrets and variables > Actions에서 설정된 secrets를 확인할 수 있습니다.${NC}"