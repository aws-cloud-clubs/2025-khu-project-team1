package com.khu.acc.newsfeed.repository;

import com.khu.acc.newsfeed.model.Post;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;

@EnableScan
public interface PostRepository extends DynamoDBPagingAndSortingRepository<Post, String> {
    public Post save(Post post);
}