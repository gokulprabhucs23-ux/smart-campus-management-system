package com.meridian.smartcampus.repository;

import com.meridian.smartcampus.model.StudentDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentDetailsRepository extends MongoRepository<StudentDetails, String> {
    Optional<StudentDetails> findByStudentId(Long studentId);
}
