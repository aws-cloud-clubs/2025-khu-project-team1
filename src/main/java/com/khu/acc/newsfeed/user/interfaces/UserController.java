package com.khu.acc.newsfeed.user.interfaces;

import com.khu.acc.newsfeed.common.dto.ApiResponse;
import com.khu.acc.newsfeed.common.util.SecurityContextUtil;
import com.khu.acc.newsfeed.user.domain.User;
import com.khu.acc.newsfeed.user.application.UserService;
import com.khu.acc.newsfeed.user.interfaces.dto.UserCreateRequest;
import com.khu.acc.newsfeed.user.interfaces.dto.UserResponse;
import com.khu.acc.newsfeed.user.interfaces.dto.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "사용자 관리 API")
public class UserController {

    private final UserService userService;
    private final SecurityContextUtil securityContextUtil;

    @PostMapping
    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        User user = userService.createUser(request.getEmail(), request.getUsername(), request.getDisplayName());
        UserResponse response = UserResponse.from(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", response));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "사용자 조회", description = "사용자 ID로 사용자 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable String userId) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found", null));
        }

        UserResponse response = UserResponse.from(userOpt.get());
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인된 사용자의 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        String userId = securityContextUtil.getCurrentUserId();
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found", null));
        }

        UserResponse response = UserResponse.from(userOpt.get());
        return ResponseEntity.ok(ApiResponse.success("Current user retrieved successfully", response));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserUpdateRequest request) {

        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found", null));
        }

        User user = userOpt.get();
        User updatedUser = userService.updateUserProfile(user, request.getDisplayName(),
                request.getBio(), request.getProfileImageUrl(), request.getInterests());

        UserResponse response = UserResponse.from(updatedUser);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    @GetMapping("/search")
    @Operation(summary = "사용자 검색", description = "사용자명으로 사용자를 검색합니다.")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(@RequestParam String username) {
        List<User> users = userService.searchUsersByUsername(username);
        List<UserResponse> responses = users.stream()
                .map(UserResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", responses));
    }

    @GetMapping
    @Operation(summary = "활성 사용자 목록", description = "활성 사용자 목록을 페이징으로 조회합니다.")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getActiveUsers(
            @PageableDefault(size = 20) Pageable pageable) {

        Page<User> users = userService.findActiveUsers(pageable);
        Page<UserResponse> responses = users.map(UserResponse::from);

        return ResponseEntity.ok(ApiResponse.success("Active users retrieved successfully", responses));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "사용자 비활성화", description = "사용자를 비활성화합니다.")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(
            @PathVariable String userId) {

        userService.deactivateUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
    }
}