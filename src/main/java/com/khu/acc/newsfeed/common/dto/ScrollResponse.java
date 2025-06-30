package com.khu.acc.newsfeed.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrollResponse<T> {

    private List<T> content; // 실제 데이터 목록
    
    private String nextCursor; // 다음 페이지를 위한 커서
    
    private String previousCursor; // 이전 페이지를 위한 커서 (optional)
    
    private boolean hasNext; // 다음 페이지 존재 여부
    
    private boolean hasPrevious; // 이전 페이지 존재 여부
    
    private int size; // 현재 페이지의 항목 수

    // Static factory methods
    public static <T> ScrollResponse<T> of(List<T> content, String nextCursor, String previousCursor, 
                                          boolean hasNext, boolean hasPrevious) {
        ScrollResponse<T> response = new ScrollResponse<>();
        response.content = content;
        response.nextCursor = nextCursor;
        response.previousCursor = previousCursor;
        response.hasNext = hasNext;
        response.hasPrevious = hasPrevious;
        response.size = content != null ? content.size() : 0;
        return response;
    }

    public static <T> ScrollResponse<T> empty() {
        return of(new java.util.ArrayList<>(), null, null, false, false);
    }

    public static <T> ScrollResponse<T> singlePage(List<T> content) {
        return of(content, null, null, false, false);
    }
}