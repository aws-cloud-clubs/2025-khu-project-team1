package com.khu.acc.newsfeed.follow;

import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@EnableScan
public interface FollowRepository extends DynamoDBPagingAndSortingRepository<Follow, String> {
    
    Follow save(Follow follow);
    
    Optional<Follow> findById(String followId);
    
    // 팔로워 목록 조회 (특정 사용자를 팔로우하는 사람들)
    List<Follow> findByFolloweeId(@Param("followeeId") String followeeId);
    
    Page<Follow> findByFolloweeId(@Param("followeeId") String followeeId, Pageable pageable);
    
    List<Follow> findByFolloweeIdOrderByCreatedAtDesc(@Param("followeeId") String followeeId);
    
    // 팔로잉 목록 조회 (특정 사용자가 팔로우하는 사람들)
    List<Follow> findByFollowerId(@Param("followerId") String followerId);
    
    Page<Follow> findByFollowerId(@Param("followerId") String followerId, Pageable pageable);
    
    List<Follow> findByFollowerIdOrderByCreatedAtDesc(@Param("followerId") String followerId);
    
    // 팔로우 관계 확인
    boolean existsByFollowerIdAndFolloweeId(
            @Param("followerId") String followerId, 
            @Param("followeeId") String followeeId);
    
    Optional<Follow> findByFollowerIdAndFolloweeId(
            @Param("followerId") String followerId, 
            @Param("followeeId") String followeeId);
    
    // 팔로우 관계 삭제
    void deleteByFollowerIdAndFolloweeId(
            @Param("followerId") String followerId, 
            @Param("followeeId") String followeeId);
    
    // 팔로워 수 카운트
    Long countByFolloweeId(@Param("followeeId") String followeeId);
    
    // 팔로잉 수 카운트
    Long countByFollowerId(@Param("followerId") String followerId);
    
    // 커서 기반 페이징 - 팔로워 목록
    List<Follow> findByFolloweeIdAndCreatedAtLessThanOrderByCreatedAtDesc(
            @Param("followeeId") String followeeId, 
            @Param("createdAt") Instant cursor, 
            Pageable pageable);
    
    List<Follow> findTopByFolloweeIdOrderByCreatedAtDesc(
            @Param("followeeId") String followeeId, 
            Pageable pageable);
    
    // 커서 기반 페이징 - 팔로잉 목록
    List<Follow> findByFollowerIdAndCreatedAtLessThanOrderByCreatedAtDesc(
            @Param("followerId") String followerId, 
            @Param("createdAt") Instant cursor, 
            Pageable pageable);
    
    List<Follow> findTopByFollowerIdOrderByCreatedAtDesc(
            @Param("followerId") String followerId, 
            Pageable pageable);
}