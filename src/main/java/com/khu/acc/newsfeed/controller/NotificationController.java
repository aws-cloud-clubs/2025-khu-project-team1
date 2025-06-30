package com.khu.acc.newsfeed.controller;

import com.khu.acc.newsfeed.dto.ApiResponse;
import com.khu.acc.newsfeed.dto.NotificationResponse;
import com.khu.acc.newsfeed.dto.PostCreateRequest;
import com.khu.acc.newsfeed.dto.PostResponse;
import com.khu.acc.newsfeed.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "알림 관리 API")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "알림 조회", description = "사용자의 알림을 10개까지 조회합니다")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("알림 조회 성공", notificationService.getNotifications(userDetails)));
    }
}
