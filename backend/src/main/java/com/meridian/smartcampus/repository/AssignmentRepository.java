package com.meridian.smartcampus.repository;

import com.meridian.smartcampus.model.Assignment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends MongoRepository<Assignment, String> {
    Optional<Assignment> findById(Long id);
    List<Assignment> findBySubjectId(Long subjectId);
}
