package com.khu.acc.newsfeed.service;

import com.khu.acc.newsfeed.dto.NotificationResponse;
import com.khu.acc.newsfeed.model.Notification;
import com.khu.acc.newsfeed.model.User;
import com.khu.acc.newsfeed.repository.NotificationRepository;
import com.khu.acc.newsfeed.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> getNotifications(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        List<Notification> notifications = notificationRepository.findTop10ByUserIdOrderByCreatedAtDesc(
            user.getUserId());
        List<NotificationResponse> notificationResponses = new ArrayList<>();
        for (final Notification notification : notifications) {
            User fromUser = userRepository.findById(notification.getFromUserId()).orElseThrow();
            notificationResponses.add(NotificationResponse.fromNotification(notification, fromUser.getUsername()));
        }
        return notificationResponses;
    }

}
