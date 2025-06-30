package com.khu.acc.newsfeed.config;

import com.khu.acc.newsfeed.comment.comment.domain.CommentRepository;
import com.khu.acc.newsfeed.comment.commentlike.domain.CommentLikeRepository;
import com.khu.acc.newsfeed.follow.FollowRepository;
import com.khu.acc.newsfeed.newsfeed.domain.NewsFeedRepository;
import com.khu.acc.newsfeed.notification.NotificationRepository;
import com.khu.acc.newsfeed.post.post.domain.PostRepository;
import com.khu.acc.newsfeed.post.postlike.domain.LikeRepository;
import com.khu.acc.newsfeed.user.domain.UserRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class TestRepositoryConfig {

    @Bean
    @Primary
    public CommentRepository commentRepository() {
        return mock(CommentRepository.class);
    }
    
    @Bean
    @Primary
    public PostRepository postRepository() {
        return mock(PostRepository.class);
    }
    
    @Bean
    @Primary
    public UserRepository userRepository() {
        return mock(UserRepository.class);
    }
    
    @Bean
    @Primary
    public LikeRepository likeRepository() {
        return mock(LikeRepository.class);
    }
    
    @Bean
    @Primary
    public FollowRepository followRepository() {
        return mock(FollowRepository.class);
    }
    
    @Bean
    @Primary
    public NewsFeedRepository newsFeedRepository() {
        return mock(NewsFeedRepository.class);
    }
    
    @Bean
    @Primary
    public CommentLikeRepository commentLikeRepository() {
        return mock(CommentLikeRepository.class);
    }
    
    @Bean
    @Primary
    public NotificationRepository notificationRepository() {
        return mock(NotificationRepository.class);
    }
}