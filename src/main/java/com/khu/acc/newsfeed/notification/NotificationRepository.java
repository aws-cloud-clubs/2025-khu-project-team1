package com.khu.acc.newsfeed.notification;

import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;

@EnableScan
public interface NotificationRepository extends DynamoDBPagingAndSortingRepository<Notification, String> {
}