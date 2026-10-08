package com.meridian.smartcampus.repository;

import com.meridian.smartcampus.model.Mark;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarkRepository extends MongoRepository<Mark, String> {
    Optional<Mark> findById(Long id);
    List<Mark> findByStudentId(Long studentId);
    List<Mark> findByStudentIdAndSemester(Long studentId, Integer semester);
    List<Mark> findBySubjectId(Long subjectId);
}
