package com.khu.acc.newsfeed.common.service.pagination;

import java.util.List;
import java.util.function.Function;

/**
 * 복합 정렬 키 기반 커서 전략 (sortKey#id)
 */
public class CompositeCursorStrategy<T> implements CursorStrategy<T> {
    private final Function<T, String> idExtractor;
    private final Function<T, String> sortKeyExtractor;
    
    public CompositeCursorStrategy(Function<T, String> idExtractor, Function<T, String> sortKeyExtractor) {
        this.idExtractor = idExtractor;
        this.sortKeyExtractor = sortKeyExtractor;
    }
    
    @Override
    public String extractCursor(T item) {
        return sortKeyExtractor.apply(item) + "#" + idExtractor.apply(item);
    }
    
    @Override
    public int findStartIndex(List<T> items, String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return 0;
        }
        
        String[] parts = cursor.split("#");
        if (parts.length != 2) {
            return 0;
        }
        
        String sortKey = parts[0];
        String id = parts[1];
        
        for (int i = 0; i < items.size(); i++) {
            T item = items.get(i);
            if (sortKeyExtractor.apply(item).equals(sortKey) && 
                idExtractor.apply(item).equals(id)) {
                return i + 1;
            }
        }
        return 0;
    }
    
    @Override
    public boolean isValidCursor(String cursor) {
        if (cursor == null || cursor.trim().isEmpty()) {
            return false;
        }
        String[] parts = cursor.split("#");
        return parts.length == 2 && !parts[0].isEmpty() && !parts[1].isEmpty();
    }
    
    @Override
    public String createPreviousCursor(List<T> allItems, int startIndex, int limit) {
        if (startIndex <= 0) {
            return null;
        }
        
        int prevStartIndex = Math.max(0, startIndex - limit);
        if (prevStartIndex < allItems.size()) {
            T item = allItems.get(prevStartIndex);
            return extractCursor(item);
        }
        return null;
    }
}