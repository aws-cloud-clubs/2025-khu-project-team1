package com.khu.acc.newsfeed.user.application;

import com.khu.acc.newsfeed.common.exception.user.UserNotFoundException;
import com.khu.acc.newsfeed.user.domain.User;
import com.khu.acc.newsfeed.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 통계 관리를 위한 서비스
 * 다른 도메인에서 사용자의 통계 정보(포스트 수, 팔로워 수 등)를 
 * 변경할 때 사용하는 전용 서비스입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatsService {
    
    private final UserRepository userRepository;
    
    /**
     * 사용자의 포스트 수를 1 증가시킵니다.
     * 
     * @param userId 포스트 수를 증가시킬 사용자 ID
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     */
    @Transactional
    public void incrementPostCount(String userId) {
        log.debug("Incrementing post count for user: userId={}", userId);
        
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for post count increment: userId={}", userId);
                    return new UserNotFoundException(userId);
                });
        
        user.incrementPostsCount();
        userRepository.save(user);
        
        log.debug("Post count incremented for user: userId={}, newCount={}", 
                userId, user.getPostsCount());
    }
    
    /**
     * 사용자의 포스트 수를 1 감소시킵니다.
     * 
     * @param userId 포스트 수를 감소시킬 사용자 ID
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     */
    @Transactional
    public void decrementPostCount(String userId) {
        log.debug("Decrementing post count for user: userId={}", userId);
        
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for post count decrement: userId={}", userId);
                    return new UserNotFoundException(userId);
                });
        
        user.decrementPostsCount();
        userRepository.save(user);
        
        log.debug("Post count decremented for user: userId={}, newCount={}", 
                userId, user.getPostsCount());
    }
}