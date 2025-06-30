package com.khu.acc.newsfeed.common.service.pagination;

import com.khu.acc.newsfeed.common.dto.ScrollRequest;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

/**
 * 페이징 컨텍스트 - 페이징에 필요한 모든 정보를 담는 컨테이너
 */
@Getter
@Builder
public class PaginationContext<T> {
    
    /**
     * 페이징할 데이터 목록
     */
    private final List<T> items;
    
    /**
     * 스크롤 요청 정보
     */
    private final ScrollRequest request;
    
    /**
     * 커서 전략
     */
    private final CursorStrategy<T> cursorStrategy;
    
    /**
     * 기본 페이지 크기
     */
    @Builder.Default
    private final int defaultLimit = 20;
    
    /**
     * 최대 페이지 크기
     */
    @Builder.Default
    private final int maxLimit = 100;
    
    /**
     * 실제 사용할 limit 계산
     */
    public int getEffectiveLimit() {
        Integer requestLimit = request.getLimit();
        if (requestLimit == null) {
            return defaultLimit;
        }
        return Math.min(requestLimit, maxLimit);
    }
    
    /**
     * 빠른 생성 메서드 - ID 기반 단순 페이징
     */
    public static <T> PaginationContext<T> of(List<T> items, ScrollRequest request, Function<T, String> idExtractor) {
        return PaginationContext.<T>builder()
                .items(items)
                .request(request)
                .cursorStrategy(new IdCursorStrategy<>(idExtractor))
                .build();
    }
    
    /**
     * 빠른 생성 메서드 - 복합 키 기반 페이징
     */
    public static <T> PaginationContext<T> withCompositeKey(
            List<T> items, 
            ScrollRequest request, 
            Function<T, String> idExtractor,
            Function<T, String> sortKeyExtractor) {
        return PaginationContext.<T>builder()
                .items(items)
                .request(request)
                .cursorStrategy(new CompositeCursorStrategy<>(idExtractor, sortKeyExtractor))
                .build();
    }
    
    /**
     * 커스텀 전략 사용
     */
    public static <T> PaginationContext<T> withStrategy(
            List<T> items, 
            ScrollRequest request, 
            CursorStrategy<T> strategy) {
        return PaginationContext.<T>builder()
                .items(items)
                .request(request)
                .cursorStrategy(strategy)
                .build();
    }
}