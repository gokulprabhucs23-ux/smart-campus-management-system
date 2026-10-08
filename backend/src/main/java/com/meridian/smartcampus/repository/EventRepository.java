package com.meridian.smartcampus.repository;

import com.meridian.smartcampus.model.CampusEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends MongoRepository<CampusEvent, String> {
    Optional<CampusEvent> findById(Long id);
}
