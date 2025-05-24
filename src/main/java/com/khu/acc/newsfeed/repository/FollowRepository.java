package com.khu.acc.newsfeed.repository;

import com.khu.acc.newsfeed.model.Follow;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;

@EnableScan
public interface FollowRepository extends DynamoDBPagingAndSortingRepository<Follow, String> {

}