package com.meridian.smartcampus.repository;

import com.meridian.smartcampus.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends MongoRepository<Student, String> {
    Optional<Student> findById(Long id);
    Optional<Student> findByEmail(String email);
    Optional<Student> findByStudentId(String studentId);
    List<Student> findByMentorId(Long mentorId);
    List<Student> findByDepartment(String department);
    List<Student> findByNameContainingIgnoreCase(String search);
}
