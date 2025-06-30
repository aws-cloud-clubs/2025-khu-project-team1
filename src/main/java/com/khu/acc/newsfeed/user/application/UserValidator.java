package com.khu.acc.newsfeed.user.application;

import com.khu.acc.newsfeed.common.exception.user.UserInactiveException;
import com.khu.acc.newsfeed.common.exception.user.UserNotFoundException;
import com.khu.acc.newsfeed.user.domain.User;
import com.khu.acc.newsfeed.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 다른 도메인에서 사용자 유효성 검증을 위한 서비스
 * Post 도메인이 User 도메인의 내부 구현(Repository)에 직접 의존하지 않고
 * 이 검증 서비스를 통해 사용자 유효성을 확인할 수 있습니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserValidator {
    
    private final UserRepository userRepository;
    
    /**
     * 주어진 사용자 ID가 존재하는 유효한 사용자인지 검증합니다.
     * 
     * @param userId 검증할 사용자 ID
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     */
    public void validateUserExists(String userId) {
        log.debug("Validating user existence: userId={}", userId);
        
        if (!userRepository.existsByUserId(userId)) {
            log.warn("User not found: userId={}", userId);
            throw new UserNotFoundException(userId);
        }
        
        log.debug("User validation successful: userId={}", userId);
    }
    
    /**
     * 사용자가 활성 상태인지 검증합니다.
     * 
     * @param userId 검증할 사용자 ID
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     * @throws UserInactiveException 사용자가 비활성 상태인 경우
     */
    public void validateUserActive(String userId) {
        log.debug("Validating user active status: userId={}", userId);
        
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("User not found during active validation: userId={}", userId);
                    return new UserNotFoundException(userId);
                });
        
        if (!"true".equals(user.getIsActive())) {
            log.warn("User is inactive: userId={}", userId);
            throw new UserInactiveException(userId);
        }
        
        log.debug("User active validation successful: userId={}", userId);
    }
    
    /**
     * 사용자를 조회하고 존재하지 않으면 예외를 발생시킵니다.
     * 포스트 카운트 증가 등에 필요한 User 객체를 반환합니다.
     * 
     * @param userId 조회할 사용자 ID
     * @return User 객체
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     */
    public User getValidatedUser(String userId) {
        log.debug("Getting validated user: userId={}", userId);
        
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: userId={}", userId);
                    return new UserNotFoundException(userId);
                });
    }
    
    /**
     * 사용자 정보를 저장합니다.
     * 포스트 카운트 변경 등으로 인한 사용자 정보 업데이트에 사용됩니다.
     * 
     * @param user 저장할 사용자 객체
     * @return 저장된 사용자 객체
     */
    public User saveUser(User user) {
        log.debug("Saving user: userId={}", user.getUserId());
        return userRepository.save(user);
    }
}