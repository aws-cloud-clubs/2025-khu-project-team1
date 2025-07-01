package com.khu.acc.newsfeed.repository;

import com.khu.acc.newsfeed.model.Notification;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

@EnableScan
public interface NotificationRepository extends DynamoDBPagingAndSortingRepository<Notification, String> {

    List<Notification> findTop10ByUserIdOrderByCreatedAtDesc(String userId);
}