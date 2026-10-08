package com.meridian.smartcampus.repository;

import com.meridian.smartcampus.model.Announcement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnouncementRepository extends MongoRepository<Announcement, String> {
    Optional<Announcement> findById(Long id);
}
