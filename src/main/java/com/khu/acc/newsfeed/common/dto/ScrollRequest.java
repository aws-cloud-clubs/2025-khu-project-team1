package com.khu.acc.newsfeed.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrollRequest {

    private String cursor; // 마지막으로 본 포스트의 ID 또는 타임스탬프

    @Min(1)
    @Max(50)
    private Integer limit = 20; // 한 번에 가져올 포스트 수

    private String direction; // "next" or "previous" (optional)

    // Static factory methods
    public static ScrollRequest of(String cursor, Integer limit) {
        ScrollRequest request = new ScrollRequest();
        request.cursor = cursor;
        request.limit = limit != null ? limit : 20;
        return request;
    }

    public static ScrollRequest withCursor(String cursor) {
        return of(cursor, 20);
    }

    public static ScrollRequest withLimit(Integer limit) {
        return of(null, limit);
    }

    public static ScrollRequest defaultRequest() {
        return of(null, 20);
    }
}