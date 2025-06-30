#!/bin/bash

# Redis 클러스터 설정 스크립트
# AWS ElastiCache Redis 클러스터를 생성하거나 기존 클러스터 정보를 확인합니다.

set -e  # 오류 시 스크립트 종료

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 기본 변수
REGION="ap-northeast-2"
CLUSTER_ID="newsfeed-redis"
NODE_TYPE="cache.t3.micro"  # 개발용
SUBNET_GROUP_NAME="newsfeed-redis-subnet-group"

echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}  Redis 클러스터 설정${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""

# AWS CLI 확인
if ! command -v aws &> /dev/null; then
    echo -e "${RED}❌ AWS CLI가 설치되지 않았습니다.${NC}"
    echo -e "${YELLOW}설치 명령어: brew install awscli${NC}"
    exit 1
fi

# AWS 자격증명 확인
echo -e "${BLUE}AWS 자격증명 확인 중...${NC}"
if aws sts get-caller-identity &> /dev/null; then
    ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
    echo -e "${GREEN}✅ AWS 자격증명 확인 완료${NC}"
    echo "Account ID: $ACCOUNT_ID"
else
    echo -e "${RED}❌ AWS 자격증명이 설정되지 않았습니다.${NC}"
    exit 1
fi

echo ""

# 기존 Redis 클러스터 확인
echo -e "${BLUE}기존 Redis 클러스터 확인 중...${NC}"
EXISTING_CLUSTERS=$(aws elasticache describe-cache-clusters --region $REGION --query 'CacheClusters[?contains(CacheClusterId, `redis`)].CacheClusterId' --output text 2>/dev/null || true)

if [[ -n "$EXISTING_CLUSTERS" ]]; then
    echo -e "${GREEN}✅ 기존 Redis 클러스터 발견:${NC}"
    echo "$EXISTING_CLUSTERS"
    echo ""
    
    # 각 클러스터의 상세 정보 출력
    for cluster in $EXISTING_CLUSTERS; do
        echo -e "${BLUE}클러스터: $cluster${NC}"
        CLUSTER_INFO=$(aws elasticache describe-cache-clusters --cache-cluster-id "$cluster" --region $REGION --show-cache-node-info --query 'CacheClusters[0]' 2>/dev/null || true)
        
        if [[ -n "$CLUSTER_INFO" ]]; then
            STATUS=$(echo "$CLUSTER_INFO" | jq -r '.CacheClusterStatus')
            ENDPOINT=$(echo "$CLUSTER_INFO" | jq -r '.RedisConfiguration.PrimaryEndpoint.Address // .CacheNodes[0].Endpoint.Address')
            PORT=$(echo "$CLUSTER_INFO" | jq -r '.RedisConfiguration.PrimaryEndpoint.Port // .CacheNodes[0].Endpoint.Port')
            NODE_TYPE=$(echo "$CLUSTER_INFO" | jq -r '.CacheNodeType')
            
            echo "  상태: $STATUS"
            echo "  엔드포인트: $ENDPOINT"
            echo "  포트: $PORT"
            echo "  노드 타입: $NODE_TYPE"
            
            if [[ "$STATUS" == "available" ]]; then
                echo -e "  ${GREEN}✅ 사용 가능${NC}"
                echo ""
                echo -e "${YELLOW}이 엔드포인트를 GitHub Secrets의 REDIS_HOST로 설정하세요:${NC}"
                echo -e "${GREEN}$ENDPOINT${NC}"
                echo ""
                
                read -p "이 클러스터를 사용하시겠습니까? (y/N): " USE_EXISTING
                if [[ "$USE_EXISTING" =~ ^[Yy]$ ]]; then
                    echo -e "${GREEN}기존 클러스터를 사용합니다.${NC}"
                    echo "REDIS_HOST: $ENDPOINT"
                    echo "REDIS_PORT: $PORT"
                    exit 0
                fi
            else
                echo -e "  ${YELLOW}⚠️ 상태: $STATUS${NC}"
            fi
        fi
        echo ""
    done
fi

echo -e "${BLUE}새 Redis 클러스터 생성 옵션:${NC}"
echo "1. 개발용 (t3.micro) - 무료 티어 적용 가능"
echo "2. 운영용 (r6g.large) - 고성능"
echo "3. 사용자 정의"
echo "4. 취소"
echo ""

read -p "선택하세요 (1-4): " CHOICE

case $CHOICE in
    1)
        NODE_TYPE="cache.t3.micro"
        CLUSTER_ID="newsfeed-redis-dev"
        echo -e "${GREEN}개발용 설정 선택: $NODE_TYPE${NC}"
        ;;
    2)
        NODE_TYPE="cache.r6g.large"
        CLUSTER_ID="newsfeed-redis-prod"
        echo -e "${GREEN}운영용 설정 선택: $NODE_TYPE${NC}"
        ;;
    3)
        read -p "클러스터 ID: " CLUSTER_ID
        read -p "노드 타입 (예: cache.t3.micro): " NODE_TYPE
        echo -e "${GREEN}사용자 정의 설정: $NODE_TYPE${NC}"
        ;;
    4)
        echo -e "${YELLOW}취소되었습니다.${NC}"
        exit 0
        ;;
    *)
        echo -e "${RED}잘못된 선택입니다.${NC}"
        exit 1
        ;;
esac

echo ""

# VPC 확인
echo -e "${BLUE}VPC 정보 확인 중...${NC}"
DEFAULT_VPC=$(aws ec2 describe-vpcs --region $REGION --filters "Name=is-default,Values=true" --query 'Vpcs[0].VpcId' --output text 2>/dev/null || echo "None")

if [[ "$DEFAULT_VPC" == "None" ]]; then
    echo -e "${RED}❌ 기본 VPC를 찾을 수 없습니다.${NC}"
    echo -e "${YELLOW}VPC를 수동으로 생성하거나 기존 VPC ID를 지정해야 합니다.${NC}"
    read -p "VPC ID를 입력하세요: " VPC_ID
else
    VPC_ID=$DEFAULT_VPC
    echo -e "${GREEN}✅ 기본 VPC 사용: $VPC_ID${NC}"
fi

# 서브넷 확인
echo -e "${BLUE}서브넷 정보 확인 중...${NC}"
SUBNETS=$(aws ec2 describe-subnets --region $REGION --filters "Name=vpc-id,Values=$VPC_ID" --query 'Subnets[].SubnetId' --output text)

if [[ -z "$SUBNETS" ]]; then
    echo -e "${RED}❌ VPC에서 서브넷을 찾을 수 없습니다.${NC}"
    exit 1
fi

SUBNET_LIST=($(echo $SUBNETS))
echo -e "${GREEN}✅ 사용 가능한 서브넷: ${#SUBNET_LIST[@]}개${NC}"

# 최소 2개 서브넷 필요 (Multi-AZ용)
if [[ ${#SUBNET_LIST[@]} -lt 2 ]]; then
    echo -e "${RED}❌ Redis 클러스터에는 최소 2개의 서브넷이 필요합니다.${NC}"
    exit 1
fi

# 서브넷 그룹 확인/생성
echo -e "${BLUE}서브넷 그룹 확인 중...${NC}"
if aws elasticache describe-cache-subnet-groups --cache-subnet-group-name $SUBNET_GROUP_NAME --region $REGION &> /dev/null; then
    echo -e "${GREEN}✅ 서브넷 그룹 '$SUBNET_GROUP_NAME' 이미 존재${NC}"
else
    echo -e "${YELLOW}⚠️ 서브넷 그룹 생성 중...${NC}"
    
    # 처음 2개 서브넷 사용
    SUBNET_IDS="${SUBNET_LIST[0]} ${SUBNET_LIST[1]}"
    
    if aws elasticache create-cache-subnet-group \
        --cache-subnet-group-name $SUBNET_GROUP_NAME \
        --cache-subnet-group-description "Subnet group for NewsFeed Redis cluster" \
        --subnet-ids $SUBNET_IDS \
        --region $REGION; then
        echo -e "${GREEN}✅ 서브넷 그룹 생성 완료${NC}"
    else
        echo -e "${RED}❌ 서브넷 그룹 생성 실패${NC}"
        exit 1
    fi
fi

echo ""

# Redis 클러스터 생성 확인
echo -e "${YELLOW}다음 설정으로 Redis 클러스터를 생성합니다:${NC}"
echo "클러스터 ID: $CLUSTER_ID"
echo "노드 타입: $NODE_TYPE"
echo "엔진: Redis 7.0"
echo "포트: 6379"
echo "서브넷 그룹: $SUBNET_GROUP_NAME"
echo "지역: $REGION"
echo ""

read -p "Redis 클러스터를 생성하시겠습니까? (y/N): " CREATE_CONFIRM
if [[ ! "$CREATE_CONFIRM" =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}클러스터 생성이 취소되었습니다.${NC}"
    exit 0
fi

echo ""
echo -e "${BLUE}Redis 클러스터 생성 중...${NC}"
echo "이 작업은 5-10분 정도 소요됩니다."

# Redis 클러스터 생성
if aws elasticache create-cache-cluster \
    --cache-cluster-id $CLUSTER_ID \
    --cache-node-type $NODE_TYPE \
    --engine redis \
    --engine-version "7.0" \
    --num-cache-nodes 1 \
    --port 6379 \
    --cache-subnet-group-name $SUBNET_GROUP_NAME \
    --region $REGION; then
    
    echo -e "${GREEN}✅ Redis 클러스터 생성 요청 완료${NC}"
    echo ""
    echo -e "${BLUE}클러스터 상태 확인 중...${NC}"
    
    # 클러스터가 준비될 때까지 대기
    echo "클러스터가 준비될 때까지 기다리는 중..."
    
    WAIT_COUNT=0
    MAX_WAIT=30  # 최대 15분 대기 (30 * 30초)
    
    while [[ $WAIT_COUNT -lt $MAX_WAIT ]]; do
        STATUS=$(aws elasticache describe-cache-clusters --cache-cluster-id $CLUSTER_ID --region $REGION --query 'CacheClusters[0].CacheClusterStatus' --output text 2>/dev/null || echo "unknown")
        
        echo "현재 상태: $STATUS (대기 시간: $((WAIT_COUNT * 30))초)"
        
        if [[ "$STATUS" == "available" ]]; then
            echo -e "${GREEN}✅ Redis 클러스터가 준비되었습니다!${NC}"
            break
        elif [[ "$STATUS" == "failed" ]]; then
            echo -e "${RED}❌ Redis 클러스터 생성에 실패했습니다.${NC}"
            exit 1
        fi
        
        sleep 30
        ((WAIT_COUNT++))
    done
    
    if [[ $WAIT_COUNT -ge $MAX_WAIT ]]; then
        echo -e "${YELLOW}⚠️ 클러스터 생성 대기 시간 초과${NC}"
        echo "AWS 콘솔에서 클러스터 상태를 확인해주세요."
    else
        # 엔드포인트 정보 가져오기
        ENDPOINT=$(aws elasticache describe-cache-clusters --cache-cluster-id $CLUSTER_ID --region $REGION --show-cache-node-info --query 'CacheClusters[0].CacheNodes[0].Endpoint.Address' --output text)
        PORT=$(aws elasticache describe-cache-clusters --cache-cluster-id $CLUSTER_ID --region $REGION --show-cache-node-info --query 'CacheClusters[0].CacheNodes[0].Endpoint.Port' --output text)
        
        echo ""
        echo -e "${GREEN}🎉 Redis 클러스터 생성 완료!${NC}"
        echo ""
        echo -e "${BLUE}연결 정보:${NC}"
        echo "엔드포인트: $ENDPOINT"
        echo "포트: $PORT"
        echo ""
        echo -e "${YELLOW}다음 값들을 GitHub Secrets에 설정하세요:${NC}"
        echo "REDIS_HOST: $ENDPOINT"
        echo "REDIS_PORT: $PORT"
        echo ""
        echo -e "${BLUE}또는 setup-github-secrets.sh 스크립트에서 이 값들을 사용하세요.${NC}"
    fi
    
else
    echo -e "${RED}❌ Redis 클러스터 생성 실패${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}🚀 Redis 설정 스크립트 완료!${NC}"
echo ""
echo -e "${BLUE}다음 단계:${NC}"
echo "1. GitHub Secrets에 Redis 정보 설정"
echo "2. 배포 스크립트 실행"
echo "3. CloudWatch에서 Redis 메트릭 모니터링"
echo ""
echo -e "${YELLOW}참고:${NC}"
echo "- Redis 클러스터는 시간당 요금이 부과됩니다"
echo "- 개발 완료 후 불필요한 클러스터는 삭제하세요"
echo "- 삭제 명령어: aws elasticache delete-cache-cluster --cache-cluster-id $CLUSTER_ID --region $REGION"