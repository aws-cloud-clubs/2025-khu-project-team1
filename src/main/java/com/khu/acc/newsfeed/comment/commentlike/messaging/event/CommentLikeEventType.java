package com.khu.acc.newsfeed.comment.commentlike.messaging.event;

public enum CommentLikeEventType {
    /**
     * 포스트 생성 이벤트
     */
    COMMENT_LIKE_CREATED("post.created"),

    /**
     * 포스트 삭제 이벤트 (소프트 삭제 포함)
     */
    COMMENT_LIKE_DELETED("post.deleted");

    private final String eventName;

    CommentLikeEventType(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }

    /**
     * 이벤트 이름으로부터 CommentEventType을 찾습니다.
     *
     * @param eventName 이벤트 이름
     * @return 해당하는 CommentEventType, 찾지 못하면 null
     */
    public static CommentLikeEventType fromEventName(String eventName) {
        for (CommentLikeEventType type : values()) {
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
