package com.meridian.smartcampus.repository;

import com.meridian.smartcampus.model.Subject;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends MongoRepository<Subject, String> {
    Optional<Subject> findById(Long id);
    Optional<Subject> findByCode(String code);
    List<Subject> findByDepartment(String department);
    List<Subject> findByDepartmentAndSemester(String department, Integer semester);
    List<Subject> findBySemester(Integer semester);
}
