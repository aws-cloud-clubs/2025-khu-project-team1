package com.khu.acc.newsfeed.comment.comment.messaging.event;

import lombok.Getter;

/**
 * 포스트 관련 이벤트 타입을 정의하는 enum
 * 이벤트 수신 측에서 어떤 타입의 이벤트인지 식별하는데 사용됩니다.
 */
@Getter
public enum CommentEventType {
    /**
     * 포스트 생성 이벤트
     */
    POST_CREATED("comment.created"),

    /**
     * 포스트 수정 이벤트
     */
    POST_UPDATED("comment.updated"),

    /**
     * 포스트 삭제 이벤트 (소프트 삭제 포함)
     */
    POST_DELETED("comment.deleted");

    private final String eventName;

    CommentEventType(String eventName) {
        this.eventName = eventName;
    }

    /**
     * 이벤트 이름으로부터 CommentEventType을 찾습니다.
     *
     * @param eventName 이벤트 이름
     * @return 해당하는 CommentEventType, 찾지 못하면 null
     */
    public static CommentEventType fromEventName(String eventName) {
        for (CommentEventType type : values()) {
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