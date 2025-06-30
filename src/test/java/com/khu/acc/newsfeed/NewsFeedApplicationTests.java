package com.khu.acc.newsfeed;

import com.khu.acc.newsfeed.config.TestDynamoDBConfig;
import com.khu.acc.newsfeed.config.TestRepositoryConfig;
import com.khu.acc.newsfeed.config.TestS3Config;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {NewsFeedApplication.class}, 
    webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import({TestDynamoDBConfig.class, TestRepositoryConfig.class, TestS3Config.class})
class NewsFeedApplicationTests {

	@Test
	void contextLoads() {
		// Basic context loading test without external dependencies
	}

}
