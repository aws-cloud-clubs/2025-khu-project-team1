package com.khu.acc.newsfeed.common.service.pagination;

import com.khu.acc.newsfeed.common.dto.ScrollRequest;
import com.khu.acc.newsfeed.common.dto.ScrollResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;

/**
 * 쿼리 기반 페이징 실행기
 * DB 쿼리를 직접 실행하여 최적화된 페이징 제공
 */
@Slf4j
public class QueryBasedPaginationExecutor<T> {
    
    private final Function<ScrollRequest, List<T>> queryExecutor;
    private final CursorStrategy<T> cursorStrategy;
    
    public QueryBasedPaginationExecutor(Function<ScrollRequest, List<T>> queryExecutor, CursorStrategy<T> cursorStrategy) {
        this.queryExecutor = queryExecutor;
        this.cursorStrategy = cursorStrategy;
    }
    
    /**
     * 쿼리 기반 페이징 실행
     */
    public ScrollResponse<T> execute(ScrollRequest request) {
        try {
            // limit + 1개를 요청해서 hasNext 판단
            int effectiveLimit = request.getLimit() != null ? request.getLimit() : 20;
            ScrollRequest queryRequest = ScrollRequest.of(request.getCursor(), effectiveLimit + 1);
            
            List<T> items = queryExecutor.apply(queryRequest);
            
            if (items.isEmpty()) {
                return ScrollResponse.empty();
            }
            
            return buildOptimizedResponse(items, request, effectiveLimit);
            
        } catch (Exception e) {
            log.error("Failed to execute paginated query", e);
            throw new RuntimeException("Pagination query failed", e);
        }
    }
    
    private ScrollResponse<T> buildOptimizedResponse(List<T> items, ScrollRequest request, int originalLimit) {
        // hasNext 판단
        boolean hasNext = items.size() > originalLimit;
        
        // 실제 반환할 데이터 조정
        List<T> resultItems = hasNext ? items.subList(0, originalLimit) : items;
        
        // 커서 설정
        String nextCursor = null;
        if (hasNext && !resultItems.isEmpty()) {
            nextCursor = cursorStrategy.extractCursor(resultItems.get(resultItems.size() - 1));
        }
        
        // 이전 커서는 현재 첫 번째 항목의 커서 (단순화)
        boolean hasPrevious = cursorStrategy.isValidCursor(request.getCursor());
        String previousCursor = null;
        if (hasPrevious && !resultItems.isEmpty()) {
            previousCursor = cursorStrategy.extractCursor(resultItems.get(0));
        }
        
        return ScrollResponse.of(resultItems, nextCursor, previousCursor, hasNext, hasPrevious);
    }
}