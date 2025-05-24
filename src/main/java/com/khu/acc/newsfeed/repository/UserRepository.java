package com.khu.acc.newsfeed.repository;

import com.khu.acc.newsfeed.model.User;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;

import java.util.Optional;

@EnableScan
public interface UserRepository extends DynamoDBPagingAndSortingRepository<User, String> {
    public User save(User user);

    public Optional<User> findById(String userId);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Long countByIsActiveTrue();
}