package com.meridian.smartcampus.repository;

import com.meridian.smartcampus.model.AttendanceItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends MongoRepository<AttendanceItem, String> {
    Optional<AttendanceItem> findById(Long id);
    List<AttendanceItem> findByStudentId(Long studentId);
    List<AttendanceItem> findByStudentIdAndSemester(Long studentId, Integer semester);
    Optional<AttendanceItem> findBySubjectId(Long subjectId);
}
