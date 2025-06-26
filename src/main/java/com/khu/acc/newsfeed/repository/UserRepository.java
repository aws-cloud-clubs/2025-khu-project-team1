package com.khu.acc.newsfeed.repository;

import com.khu.acc.newsfeed.model.User;
import java.util.List;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@EnableScan
public interface UserRepository extends DynamoDBPagingAndSortingRepository<User, String> {
    public User save(User user);

    public Optional<User> findById(String userId);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Long countByIsActiveTrue();

    List<User> findAllByUsername(String username);

    Page<User> findByIsActive(String isActive, Pageable pageable);
}