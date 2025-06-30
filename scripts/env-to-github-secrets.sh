#!/bin/bash

# .env 파일에서 GitHub Secrets로 일괄 업로드하는 스크립트

set -e  # 오류 시 스크립트 종료

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Repository 정보
REPO="aws-cloud-clubs/2025-khu-project-team1"
ENV_FILE=".env"

echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}  .env → GitHub Secrets 업로드${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""

# .env 파일 존재 확인
if [[ ! -f "$ENV_FILE" ]]; then
    echo -e "${RED}❌ .env 파일을 찾을 수 없습니다.${NC}"
    echo -e "${YELLOW}다음 명령어로 .env.example을 복사해서 시작하세요:${NC}"
    echo "  cp .env.example .env"
    echo "  # .env 파일을 편집해서 실제 값들을 입력하세요"
    exit 1
fi

# GitHub CLI 설치 확인
if ! command -v gh &> /dev/null; then
    echo -e "${RED}❌ GitHub CLI (gh)가 설치되지 않았습니다.${NC}"
    echo -e "${YELLOW}설치 명령어: brew install gh${NC}"
    exit 1
fi

# GitHub CLI 인증 확인
if ! gh auth status &> /dev/null; then
    echo -e "${RED}❌ GitHub CLI 인증이 필요합니다.${NC}"
    echo -e "${YELLOW}다음 명령어로 인증해주세요: gh auth login${NC}"
    exit 1
fi

echo -e "${GREEN}✅ GitHub CLI 인증 완료${NC}"

# Repository 접근 권한 확인
if ! gh repo view $REPO &> /dev/null; then
    echo -e "${RED}❌ Repository '$REPO'에 접근할 수 없습니다.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Repository 접근 권한 확인 완료${NC}"
echo ""

# .env 파일 내용 미리보기
echo -e "${BLUE}.env 파일 내용 (값은 숨김):${NC}"
while IFS='=' read -r key value; do
    # 빈 줄이나 주석 제외
    if [[ ! -z "$key" && ! "$key" =~ ^# ]]; then
        # 값의 길이에 따라 마스킹
        if [[ ${#value} -gt 0 ]]; then
            masked_value=$(echo "$value" | sed 's/./*/g' | cut -c1-8)
            echo "  $key = ${masked_value}..."
        fi
    fi
done < "$ENV_FILE"

echo ""
read -p "이 설정들을 GitHub Secrets로 업로드하시겠습니까? (y/N): " CONFIRM
if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}❌ 업로드가 취소되었습니다.${NC}"
    exit 0
fi

echo ""
echo -e "${BLUE}GitHub Secrets 업로드 중...${NC}"

# Secrets 설정 함수
set_secret() {
    local key=$1
    local value=$2
    
    if [[ -z "$value" ]]; then
        echo -e "${YELLOW}⚠️ $key 값이 비어있습니다. 건너뜁니다.${NC}"
        return 0
    fi
    
    if gh secret set "$key" -b "$value" -R $REPO &> /dev/null; then
        # 값의 일부만 표시 (보안)
        display_value=""
        if [[ ${#value} -gt 8 ]]; then
            display_value="${value:0:4}...${value: -4}"
        else
            display_value="****"
        fi
        echo -e "${GREEN}✅ $key${NC} ($display_value)"
    else
        echo -e "${RED}❌ $key 설정 실패${NC}"
        return 1
    fi
}

# .env 파일에서 secrets 업로드
SUCCESS_COUNT=0
TOTAL_COUNT=0

while IFS='=' read -r key value; do
    # 빈 줄이나 주석 제외
    if [[ ! -z "$key" && ! "$key" =~ ^# ]]; then
        # 앞뒤 공백 제거
        key=$(echo "$key" | xargs)
        value=$(echo "$value" | xargs)
        
        ((TOTAL_COUNT++))
        
        if set_secret "$key" "$value"; then
            ((SUCCESS_COUNT++))
        fi
    fi
done < "$ENV_FILE"

echo ""
if [[ $SUCCESS_COUNT -eq $TOTAL_COUNT ]]; then
    echo -e "${GREEN}🎉 모든 GitHub Secrets가 성공적으로 설정되었습니다!${NC}"
    echo "업로드된 secrets: $SUCCESS_COUNT/$TOTAL_COUNT"
else
    echo -e "${YELLOW}⚠️ 일부 secrets 설정에 실패했습니다.${NC}"
    echo "업로드된 secrets: $SUCCESS_COUNT/$TOTAL_COUNT"
fi

echo ""
echo -e "${BLUE}설정된 secrets 목록:${NC}"
gh secret list -R $REPO

echo ""
echo -e "${BLUE}다음 단계:${NC}"
echo "1. GitHub Actions 탭에서 워크플로우 실행 확인"
echo "2. 또는 main 브랜치에 푸시해서 자동 배포 실행"
echo "3. 로컬에서 직접 배포: ./scripts/deploy.sh dev"
echo ""
echo -e "${YELLOW}참고: .env 파일은 절대로 Git에 커밋하지 마세요!${NC}"