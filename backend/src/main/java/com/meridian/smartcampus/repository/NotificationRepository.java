package com.meridian.smartcampus.repository;

import com.meridian.smartcampus.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    Optional<Notification> findById(Long id);
    List<Notification> findByTargetRoleIsNullOrTargetRole(String targetRole);
    List<Notification> findByTargetUsername(String targetUsername);
}
