package com.meridian.smartcampus.repository;

import com.meridian.smartcampus.model.FeeInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeeRepository extends MongoRepository<FeeInfo, String> {
    Optional<FeeInfo> findByStudentId(Long studentId);
}
