package com.khu.acc.newsfeed.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khu.acc.newsfeed.NewsFeedApplication;
import com.khu.acc.newsfeed.config.TestDynamoDBConfig;
import com.khu.acc.newsfeed.config.TestRepositoryConfig;
import com.khu.acc.newsfeed.config.TestS3Config;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

@SpringBootTest(classes = NewsFeedApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestDynamoDBConfig.class, TestRepositoryConfig.class, TestS3Config.class})
public abstract class E2ETestBase {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // MockMvc is already autowired with @AutoConfigureMockMvc
    }

    protected String generateRandomString(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}