package com.khu.acc.newsfeed.repository;

import com.khu.acc.newsfeed.model.Like;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@EnableScan
public interface LikeRepository extends DynamoDBPagingAndSortingRepository<Like, String> {

}
