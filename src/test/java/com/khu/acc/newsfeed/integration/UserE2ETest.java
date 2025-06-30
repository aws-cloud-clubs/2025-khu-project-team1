package com.khu.acc.newsfeed.integration;

import com.khu.acc.newsfeed.common.dto.ApiResponse;
import com.khu.acc.newsfeed.user.interfaces.dto.UserCreateRequest;
import com.khu.acc.newsfeed.user.interfaces.dto.UserResponse;
import com.khu.acc.newsfeed.user.interfaces.dto.UserUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("사용자 API E2E 테스트")
class UserE2ETest extends E2ETestBase {

    @Nested
    @DisplayName("사용자 생성")
    class UserCreation {

        @Test
        @DisplayName("유효한 사용자 정보가 주어졌을 때 성공적으로 사용자를 생성한다")
        void createsUserSuccessfullyWhenValidDataProvided() throws Exception {
            // Given
            String username = generateRandomString("testuser");
            String email = generateRandomString("test") + "@example.com";
            String displayName = generateRandomString("TestUser");

            UserCreateRequest request = UserCreateRequest.builder()
                    .username(username)
                    .email(email)
                    .displayName(displayName)
                    .build();

            // When & Then
            MvcResult result = mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.username").value(username))
                    .andExpect(jsonPath("$.data.email").value(email))
                    .andExpect(jsonPath("$.data.displayName").value(displayName))
                    .andReturn();

            ApiResponse<UserResponse> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, UserResponse.class));

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getUsername()).isEqualTo(username);
            assertThat(response.getData().getEmail()).isEqualTo(email);
            assertThat(response.getData().getDisplayName()).isEqualTo(displayName);
        }

        @Test
        @DisplayName("이미 존재하는 사용자명으로 생성을 시도할 때 실패한다")
        void failsWhenUsernameAlreadyExists() throws Exception {
            // Given
            String username = generateRandomString("testuser");
            String email1 = generateRandomString("test1") + "@example.com";
            String email2 = generateRandomString("test2") + "@example.com";
            String displayName = generateRandomString("TestUser");

            UserCreateRequest firstRequest = UserCreateRequest.builder()
                    .username(username)
                    .email(email1)
                    .displayName(displayName)
                    .build();

            // 첫 번째 사용자 생성
            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isCreated());

            UserCreateRequest duplicateRequest = UserCreateRequest.builder()
                    .username(username) // 동일한 사용자명
                    .email(email2)
                    .displayName(displayName)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("유효하지 않은 이메일 형식일 때 실패한다")
        void failsWhenInvalidEmailFormat() throws Exception {
            // Given
            UserCreateRequest request = UserCreateRequest.builder()
                    .username(generateRandomString("testuser"))
                    .email("invalid-email")
                    .displayName(generateRandomString("TestUser"))
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("사용자 조회")
    class UserRetrieval {

        @Test
        @DisplayName("존재하는 사용자 ID가 주어졌을 때 사용자를 성공적으로 조회한다")
        void retrievesUserSuccessfullyWhenValidUserIdProvided() throws Exception {
            // Given
            String username = generateRandomString("testuser");
            String email = generateRandomString("test") + "@example.com";
            String displayName = generateRandomString("TestUser");

            UserCreateRequest createRequest = UserCreateRequest.builder()
                    .username(username)
                    .email(email)
                    .displayName(displayName)
                    .build();

            MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            ApiResponse<UserResponse> createResponse = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, UserResponse.class));

            String userId = createResponse.getData().getUserId();

            // When & Then
            mockMvc.perform(get("/api/v1/users/" + userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.userId").value(userId))
                    .andExpect(jsonPath("$.data.username").value(username))
                    .andExpect(jsonPath("$.data.email").value(email))
                    .andExpect(jsonPath("$.data.displayName").value(displayName));
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 조회를 시도할 때 실패한다")
        void failsWhenUserNotFound() throws Exception {
            // Given
            String nonExistentUserId = "non-existent-user-id";

            // When & Then
            mockMvc.perform(get("/api/v1/users/" + nonExistentUserId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("User not found"));
        }

        @Test
        @DisplayName("사용자 검색이 올바르게 동작한다")
        void searchesUsersCorrectly() throws Exception {
            // Given
            String searchTerm = generateRandomString("search");
            String username1 = searchTerm + "_user1";
            String username2 = searchTerm + "_user2";
            String username3 = "other_user";

            // 검색 대상 사용자들 생성
            UserCreateRequest user1 = UserCreateRequest.builder()
                    .username(username1)
                    .email(generateRandomString("test1") + "@example.com")
                    .displayName("User 1")
                    .build();

            UserCreateRequest user2 = UserCreateRequest.builder()
                    .username(username2)
                    .email(generateRandomString("test2") + "@example.com")
                    .displayName("User 2")
                    .build();

            UserCreateRequest user3 = UserCreateRequest.builder()
                    .username(username3)
                    .email(generateRandomString("test3") + "@example.com")
                    .displayName("User 3")
                    .build();

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user1)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user2)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user3)))
                    .andExpect(status().isCreated());

            // When & Then
            mockMvc.perform(get("/api/v1/users/search")
                            .param("username", searchTerm))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2)); // user1과 user2만 포함되어야 함
        }
    }

    @Nested
    @DisplayName("사용자 정보 수정")
    class UserUpdate {

        @Test
        @DisplayName("유효한 수정 데이터로 사용자 정보를 수정할 때 성공한다")
        void updatesUserSuccessfullyWhenValidDataProvided() throws Exception {
            // Given
            String username = generateRandomString("testuser");
            String email = generateRandomString("test") + "@example.com";
            String originalDisplayName = generateRandomString("OriginalName");

            UserCreateRequest createRequest = UserCreateRequest.builder()
                    .username(username)
                    .email(email)
                    .displayName(originalDisplayName)
                    .build();

            MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            ApiResponse<UserResponse> createResponse = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, UserResponse.class));

            String userId = createResponse.getData().getUserId();

            String newDisplayName = generateRandomString("UpdatedName");
            String newBio = "Updated bio";
            UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                    .displayName(newDisplayName)
                    .bio(newBio)
                    .build();

            // When & Then
            mockMvc.perform(put("/api/v1/users/" + userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.userId").value(userId))
                    .andExpect(jsonPath("$.data.displayName").value(newDisplayName))
                    .andExpect(jsonPath("$.data.bio").value(newBio));
        }
    }

    @Nested
    @DisplayName("활성 사용자 목록 조회")
    class ActiveUsersList {

        @Test
        @DisplayName("활성 사용자 목록을 페이징과 함께 올바르게 조회한다")
        void retrievesActiveUsersWithPaginationCorrectly() throws Exception {
            // Given
            // 여러 사용자 생성
            for (int i = 1; i <= 5; i++) {
                UserCreateRequest userRequest = UserCreateRequest.builder()
                        .username(generateRandomString("activeuser" + i))
                        .email(generateRandomString("active" + i) + "@example.com")
                        .displayName("Active User " + i)
                        .build();

                mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userRequest)))
                        .andExpect(status().isCreated());
            }

            // When & Then
            mockMvc.perform(get("/api/v1/users")
                            .param("page", "0")
                            .param("size", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(3))
                    .andExpect(jsonPath("$.data.totalElements").exists())
                    .andExpect(jsonPath("$.data.totalPages").exists())
                    .andExpect(jsonPath("$.data.first").value(true))
                    .andExpect(jsonPath("$.data.last").exists());
        }
    }
}