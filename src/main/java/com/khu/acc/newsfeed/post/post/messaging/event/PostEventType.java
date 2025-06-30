package com.khu.acc.newsfeed.post.post.messaging.event;

/**
 * 포스트 관련 이벤트 타입을 정의하는 enum
 * 이벤트 수신 측에서 어떤 타입의 이벤트인지 식별하는데 사용됩니다.
 */
public enum PostEventType {
    /**
     * 포스트 생성 이벤트
     */
    POST_CREATED("post.created"),
    
    /**
     * 포스트 수정 이벤트
     */
    POST_UPDATED("post.updated"),
    
    /**
     * 포스트 삭제 이벤트 (소프트 삭제 포함)
     */
    POST_DELETED("post.deleted");
    
    private final String eventName;
    
    PostEventType(String eventName) {
        this.eventName = eventName;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    /**
     * 이벤트 이름으로부터 PostEventType을 찾습니다.
     * 
     * @param eventName 이벤트 이름
     * @return 해당하는 PostEventType, 찾지 못하면 null
     */
    public static PostEventType fromEventName(String eventName) {
        for (PostEventType type : values()) {
            if (type.eventName.equals(eventName)) {
                return type;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return eventName;
    }
}