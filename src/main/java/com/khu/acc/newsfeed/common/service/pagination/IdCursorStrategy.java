package com.khu.acc.newsfeed.common.service.pagination;

import java.util.List;
import java.util.function.Function;

/**
 * ID 기반 단순 커서 전략
 */
public class IdCursorStrategy<T> implements CursorStrategy<T> {
    private final Function<T, String> idExtractor;
    
    public IdCursorStrategy(Function<T, String> idExtractor) {
        this.idExtractor = idExtractor;
    }
    
    @Override
    public String extractCursor(T item) {
        return idExtractor.apply(item);
    }
    
    @Override
    public int findStartIndex(List<T> items, String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return 0;
        }
        
        for (int i = 0; i < items.size(); i++) {
            if (extractCursor(items.get(i)).equals(cursor)) {
                return i + 1; // 커서 다음부터 시작
            }
        }
        return 0;
    }
    
    @Override
    public boolean isValidCursor(String cursor) {
        return cursor != null && !cursor.trim().isEmpty();
    }
    
    @Override
    public String createPreviousCursor(List<T> allItems, int startIndex, int limit) {
        if (startIndex <= 0) {
            return null;
        }
        
        int prevStartIndex = Math.max(0, startIndex - limit);
        return prevStartIndex < allItems.size() ? 
                extractCursor(allItems.get(prevStartIndex)) : null;
    }
}