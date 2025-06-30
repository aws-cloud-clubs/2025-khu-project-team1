package com.khu.acc.newsfeed.common.service;

import com.khu.acc.newsfeed.common.dto.ScrollRequest;
import com.khu.acc.newsfeed.common.dto.ScrollResponse;
import com.khu.acc.newsfeed.common.service.pagination.CursorStrategy;
import com.khu.acc.newsfeed.common.service.pagination.IdCursorStrategy;
import com.khu.acc.newsfeed.common.service.pagination.PaginationContext;
import com.khu.acc.newsfeed.common.service.pagination.QueryBasedPaginationExecutor;
// Removed unused import
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

/**
 * 통합된 커서 페이징 서비스
 * Generic을 활용한 일관된 페이징 인터페이스 제공
 */
@Slf4j
@Service
public class CursorPaginationService {

    /**
     * Generic 커서 페이징 - 가장 일반적인 방식
     * ID 기반 단순 커서 사용
     */
    public <T> ScrollResponse<T> paginate(
            List<T> items, 
            ScrollRequest request,
            Function<T, String> idExtractor) {
        
        PaginationContext<T> context = PaginationContext.of(items, request, idExtractor);
        return executeMemoryBasedPagination(context);
    }

    /**
     * 쿼리 기반 최적화된 페이징
     * DB 쿼리를 직접 실행하여 성능 최적화
     */
    public <T> ScrollResponse<T> paginateWithQuery(
            Function<ScrollRequest, List<T>> queryExecutor,
            ScrollRequest request,
            Function<T, String> idExtractor) {
        
        CursorStrategy<T> strategy = new IdCursorStrategy<>(idExtractor);
        QueryBasedPaginationExecutor<T> executor = new QueryBasedPaginationExecutor<>(queryExecutor, strategy);
        return executor.execute(request);
    }

    /**
     * 복합 정렬 키 기반 페이징
     * sortKey#id 형태의 커서 사용
     */
    public <T> ScrollResponse<T> paginateWithSort(
            List<T> items,
            ScrollRequest request,
            Function<T, String> idExtractor,
            Function<T, String> sortKeyExtractor) {
        
        PaginationContext<T> context = PaginationContext.withCompositeKey(
                items, request, idExtractor, sortKeyExtractor);
        return executeMemoryBasedPagination(context);
    }
    
    /**
     * 사용자 정의 커서 전략 사용
     */
    public <T> ScrollResponse<T> paginateWithCustomStrategy(
            List<T> items,
            ScrollRequest request,
            CursorStrategy<T> strategy) {
        
        PaginationContext<T> context = PaginationContext.withStrategy(items, request, strategy);
        return executeMemoryBasedPagination(context);
    }
    
    /**
     * 쿼리 기반 + 커스텀 전략 조합
     */
    public <T> ScrollResponse<T> paginateWithQueryAndStrategy(
            Function<ScrollRequest, List<T>> queryExecutor,
            ScrollRequest request,
            CursorStrategy<T> strategy) {
        
        QueryBasedPaginationExecutor<T> executor = new QueryBasedPaginationExecutor<>(queryExecutor, strategy);
        return executor.execute(request);
    }
    
    /**
     * 메모리 기반 페이징 실행
     */
    private <T> ScrollResponse<T> executeMemoryBasedPagination(PaginationContext<T> context) {
        List<T> items = context.getItems();
        if (items.isEmpty()) {
            return ScrollResponse.empty();
        }
        
        ScrollRequest request = context.getRequest();
        CursorStrategy<T> strategy = context.getCursorStrategy();
        
        // 커서 기반으로 시작 인덱스 찾기
        int startIndex = strategy.findStartIndex(items, request.getCursor());
        
        // 페이징 처리
        int limit = context.getEffectiveLimit();
        int endIndex = Math.min(startIndex + limit + 1, items.size());
        
        List<T> resultItems = items.subList(startIndex, endIndex);
        
        // hasNext 판단 및 데이터 조정
        boolean hasNext = resultItems.size() > limit;
        if (hasNext) {
            resultItems = resultItems.subList(0, limit);
        }
        
        // 커서 설정
        String nextCursor = null;
        if (hasNext && !resultItems.isEmpty()) {
            nextCursor = strategy.extractCursor(resultItems.get(resultItems.size() - 1));
        }
        
        String previousCursor = strategy.createPreviousCursor(items, startIndex, limit);
        boolean hasPrevious = startIndex > 0;
        
        return ScrollResponse.of(resultItems, nextCursor, previousCursor, hasNext, hasPrevious);
    }
    
    // Utility methods for backward compatibility
    
    /**
     * 커서 문자열에서 ID 추출 (복합 커서 지원)
     */
    public String extractIdFromCursor(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }
        
        // 복합 커서인 경우 (sortKey#id)
        String[] parts = cursor.split("#");
        return parts.length > 1 ? parts[1] : cursor;
    }

    /**
     * 커서 문자열에서 정렬 키 추출
     */
    public String extractSortKeyFromCursor(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }
        
        String[] parts = cursor.split("#");
        return parts[0];
    }

    /**
     * 복합 커서 생성
     */
    public String createCompositeCursor(String sortKey, String id) {
        return sortKey + "#" + id;
    }
}