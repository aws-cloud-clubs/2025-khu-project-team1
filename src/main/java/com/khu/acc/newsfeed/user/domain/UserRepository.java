package com.khu.acc.newsfeed.user.domain;

import java.util.List;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@EnableScan
public interface UserRepository extends DynamoDBPagingAndSortingRepository<User, String> {
    public User save(User user);

    public Optional<User> findByUserId(String userId);

    public List<User> findAllByUserIds(Set<String> userIds);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findAllByUsername(String username);

    Page<User> findByIsActive(String isActive, Pageable pageable);

    boolean existsByUserId(String userId);
}