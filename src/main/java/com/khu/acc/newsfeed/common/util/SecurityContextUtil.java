package com.khu.acc.newsfeed.common.util;

import com.khu.acc.newsfeed.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * JWT 토큰에서 현재 사용자 정보를 추출하는 유틸리티
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityContextUtil {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 현재 인증된 사용자의 userId를 반환
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        // JWT 토큰에서 userId 추출 시도
        String token = getCurrentJwtToken();
        if (token != null) {
            String userId = jwtTokenProvider.getUserIdFromToken(token);
            if (userId != null) {
                return userId;
            }
        }

        // 백업: principal에서 username 사용 (username이 userId인 경우)
        return authentication.getName();
    }

    /**
     * 현재 인증된 사용자의 username을 반환
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return authentication.getName();
    }

    /**
     * 사용자가 인증되었는지 확인
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * 현재 요청에서 JWT 토큰을 추출 (RequestContextHolder 사용)
     */
    private String getCurrentJwtToken() {
        try {
            // Spring의 RequestContextHolder를 통해 현재 요청의 JWT 토큰 추출
            // 실제 구현에서는 ThreadLocal이나 다른 방식으로 토큰을 저장/추출
            return JwtTokenHolder.getCurrentToken();
        } catch (Exception e) {
            log.debug("Failed to extract JWT token from current request", e);
            return null;
        }
    }

    /**
     * JWT 토큰을 ThreadLocal에 저장하는 홀더 클래스
     */
    public static class JwtTokenHolder {
        private static final ThreadLocal<String> tokenHolder = new ThreadLocal<>();

        public static void setCurrentToken(String token) {
            tokenHolder.set(token);
        }

        public static String getCurrentToken() {
            return tokenHolder.get();
        }

        public static void clear() {
            tokenHolder.remove();
        }
    }
}