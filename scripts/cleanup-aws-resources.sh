#!/bin/bash

# AWS Resource Cleanup Script
# This script cleans up resources that failed to delete via CloudFormation

set -e

REGION="ap-northeast-2"
STAGES=("dev" "user1")
PROJECT_PREFIXES=("news-feed" "newsfeed" "khu-newsfeed" "khu")

echo "🧹 Starting AWS Resource Cleanup..."

# Function to safely delete SQS queue
delete_sqs_queue() {
    local queue_url=$1
    local queue_name=$(basename "$queue_url")
    
    echo "  Deleting SQS Queue: $queue_name"
    if aws sqs delete-queue --queue-url "$queue_url" --region "$REGION" 2>/dev/null; then
        echo "    ✅ Successfully deleted: $queue_name"
    else
        echo "    ❌ Failed to delete or doesn't exist: $queue_name"
    fi
}

# Function to safely delete SNS topic
delete_sns_topic() {
    local topic_arn=$1
    local topic_name=$(basename "$topic_arn")
    
    echo "  Deleting SNS Topic: $topic_name"
    if aws sns delete-topic --topic-arn "$topic_arn" --region "$REGION" 2>/dev/null; then
        echo "    ✅ Successfully deleted: $topic_name"
    else
        echo "    ❌ Failed to delete or doesn't exist: $topic_name"
    fi
}

# Function to safely delete DynamoDB table
delete_dynamodb_table() {
    local table_name=$1
    
    echo "  Deleting DynamoDB Table: $table_name"
    if aws dynamodb delete-table --table-name "$table_name" --region "$REGION" 2>/dev/null; then
        echo "    ✅ Successfully deleted: $table_name"
    else
        echo "    ❌ Failed to delete or doesn't exist: $table_name"
    fi
}

# Function to safely delete S3 bucket (with contents)
delete_s3_bucket() {
    local bucket_name=$1
    
    echo "  Deleting S3 Bucket: $bucket_name"
    
    # First, delete all objects in the bucket
    if aws s3 ls "s3://$bucket_name" --region "$REGION" >/dev/null 2>&1; then
        echo "    Emptying bucket contents..."
        aws s3 rm "s3://$bucket_name" --recursive --region "$REGION" 2>/dev/null || true
        
        # Delete the bucket
        if aws s3 rb "s3://$bucket_name" --region "$REGION" 2>/dev/null; then
            echo "    ✅ Successfully deleted: $bucket_name"
        else
            echo "    ❌ Failed to delete: $bucket_name"
        fi
    else
        echo "    ❌ Bucket doesn't exist: $bucket_name"
    fi
}

# Function to safely delete CloudFormation stack
delete_cloudformation_stack() {
    local stack_name=$1
    
    echo "  Deleting CloudFormation Stack: $stack_name"
    
    # Check stack status
    local stack_status=$(aws cloudformation describe-stacks --stack-name "$stack_name" --region "$REGION" --query 'Stacks[0].StackStatus' --output text 2>/dev/null || echo "NOT_FOUND")
    
    if [[ "$stack_status" == "NOT_FOUND" ]]; then
        echo "    ❌ Stack doesn't exist: $stack_name"
        return
    fi
    
    if [[ "$stack_status" == "DELETE_FAILED" || "$stack_status" == "UPDATE_ROLLBACK_COMPLETE" ]]; then
        echo "    Stack is in $stack_status state, forcing deletion..."
        
        # Get failed resources
        local failed_resources=$(aws cloudformation list-stack-resources --stack-name "$stack_name" --region "$REGION" --query 'StackResourceSummaries[?ResourceStatus==`DELETE_FAILED`].LogicalResourceId' --output text 2>/dev/null || echo "")
        
        if [[ -n "$failed_resources" ]]; then
            echo "    Failed resources found, will try to delete manually first..."
            
            # Try to delete the stack anyway
            aws cloudformation delete-stack --stack-name "$stack_name" --region "$REGION" 2>/dev/null || true
        fi
    else
        # Normal deletion
        if aws cloudformation delete-stack --stack-name "$stack_name" --region "$REGION" 2>/dev/null; then
            echo "    ✅ Successfully initiated deletion: $stack_name"
        else
            echo "    ❌ Failed to delete: $stack_name"
        fi
    fi
}

echo "🔍 Step 1: Cleaning up SQS Queues..."
for stage in "${STAGES[@]}"; do
    for prefix in "${PROJECT_PREFIXES[@]}"; do
        # List and delete SQS queues
        queues=$(aws sqs list-queues --region "$REGION" --queue-name-prefix "${prefix}" 2>/dev/null | jq -r '.QueueUrls[]?' 2>/dev/null || echo "")
        
        if [[ -n "$queues" ]]; then
            while IFS= read -r queue_url; do
                if [[ "$queue_url" == *"$stage"* ]]; then
                    delete_sqs_queue "$queue_url"
                fi
            done <<< "$queues"
        fi
    done
done

echo ""
echo "🔍 Step 2: Cleaning up SNS Topics..."
for stage in "${STAGES[@]}"; do
    # List and delete SNS topics
    topics=$(aws sns list-topics --region "$REGION" 2>/dev/null | jq -r '.Topics[]?.TopicArn' 2>/dev/null || echo "")
    
    if [[ -n "$topics" ]]; then
        while IFS= read -r topic_arn; do
            if [[ "$topic_arn" == *"post-events-topic"* && "$topic_arn" == *"$stage"* ]]; then
                delete_sns_topic "$topic_arn"
            fi
        done <<< "$topics"
    fi
done

echo ""
echo "🔍 Step 3: Cleaning up DynamoDB Tables..."
for stage in "${STAGES[@]}"; do
    # List and delete DynamoDB tables
    tables=$(aws dynamodb list-tables --region "$REGION" 2>/dev/null | jq -r '.TableNames[]?' 2>/dev/null || echo "")
    
    if [[ -n "$tables" ]]; then
        while IFS= read -r table_name; do
            if [[ "$table_name" == *"$stage"* ]] && [[ "$table_name" =~ (Comments|Posts|Users|Likes|Follows|NewsFeed|Notifications|LikeCountBuffer|CommentLikes) ]]; then
                delete_dynamodb_table "$table_name"
            fi
        done <<< "$tables"
    fi
done

echo ""
echo "🔍 Step 4: Cleaning up S3 Buckets..."
for stage in "${STAGES[@]}"; do
    for prefix in "${PROJECT_PREFIXES[@]}"; do
        # List and delete S3 buckets
        buckets=$(aws s3api list-buckets --region "$REGION" 2>/dev/null | jq -r '.Buckets[]?.Name' 2>/dev/null || echo "")
        
        if [[ -n "$buckets" ]]; then
            while IFS= read -r bucket_name; do
                if [[ "$bucket_name" == *"$prefix"* && "$bucket_name" == *"$stage"* ]]; then
                    delete_s3_bucket "$bucket_name"
                fi
            done <<< "$buckets"
        fi
    done
done

echo ""
echo "🔍 Step 5: Cleaning up CloudFormation Stacks..."
for prefix in "${PROJECT_PREFIXES[@]}"; do
    # List and delete CloudFormation stacks
    stacks=$(aws cloudformation list-stacks --region "$REGION" --stack-status-filter CREATE_COMPLETE UPDATE_COMPLETE ROLLBACK_COMPLETE UPDATE_ROLLBACK_COMPLETE DELETE_FAILED 2>/dev/null | jq -r '.StackSummaries[]?.StackName' 2>/dev/null || echo "")
    
    if [[ -n "$stacks" ]]; then
        while IFS= read -r stack_name; do
            if [[ "$stack_name" == *"$prefix"* ]]; then
                delete_cloudformation_stack "$stack_name"
            fi
        done <<< "$stacks"
    fi
done

echo ""
echo "⏳ Waiting for deletions to complete..."
sleep 10

echo ""
echo "🔍 Final verification - checking remaining resources..."

echo "Remaining SQS Queues:"
aws sqs list-queues --region "$REGION" 2>/dev/null | jq -r '.QueueUrls[]?' 2>/dev/null | grep -E "(news-feed|newsfeed|khu|like-count|notification)" || echo "  None found"

echo "Remaining SNS Topics:"
aws sns list-topics --region "$REGION" 2>/dev/null | jq -r '.Topics[]?.TopicArn' 2>/dev/null | grep -E "(post-events|news-feed|newsfeed|khu)" || echo "  None found"

echo "Remaining DynamoDB Tables:"
aws dynamodb list-tables --region "$REGION" 2>/dev/null | jq -r '.TableNames[]?' 2>/dev/null | grep -E "(Comments|Posts|Users|Likes|Follows|NewsFeed|Notifications|LikeCountBuffer|CommentLikes)" || echo "  None found"

echo "Remaining S3 Buckets:"
aws s3api list-buckets --region "$REGION" 2>/dev/null | jq -r '.Buckets[]?.Name' 2>/dev/null | grep -E "(news-feed|newsfeed|khu)" || echo "  None found"

echo "Remaining CloudFormation Stacks:"
aws cloudformation list-stacks --region "$REGION" --stack-status-filter CREATE_COMPLETE UPDATE_COMPLETE ROLLBACK_COMPLETE UPDATE_ROLLBACK_COMPLETE DELETE_FAILED 2>/dev/null | jq -r '.StackSummaries[]?.StackName' 2>/dev/null | grep -E "(news-feed|newsfeed|khu)" || echo "  None found"

echo ""
echo "✅ AWS Resource Cleanup Complete!"
echo "📝 Note: Some resources may take additional time to fully delete."
echo "📝 Re-run this script if you see any remaining resources that should be deleted."