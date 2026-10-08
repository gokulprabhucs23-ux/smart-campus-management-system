package com.meridian.smartcampus.repository;

import com.meridian.smartcampus.model.Complaint;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComplaintRepository extends MongoRepository<Complaint, String> {
    Optional<Complaint> findById(Long id);
}
