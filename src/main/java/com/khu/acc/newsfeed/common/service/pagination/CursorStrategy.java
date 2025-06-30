package com.khu.acc.newsfeed.common.service.pagination;

import java.util.List;

/**
 * 커서 페이징 전략 인터페이스
 * 다양한 커서 방식을 지원하기 위한 추상화
 */
public interface CursorStrategy<T> {
    
    /**
     * 아이템으로부터 커서 값을 추출
     */
    String extractCursor(T item);
    
    /**
     * 커서 기준으로 시작 인덱스를 찾음
     */
    int findStartIndex(List<T> items, String cursor);
    
    /**
     * 커서가 유효한지 검증
     */
    boolean isValidCursor(String cursor);
    
    /**
     * 이전 페이지 커서 생성
     */
    String createPreviousCursor(List<T> allItems, int startIndex, int limit);
}