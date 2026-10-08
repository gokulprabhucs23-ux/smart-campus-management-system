package com.meridian.smartcampus.repository;

import com.meridian.smartcampus.model.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends MongoRepository<Department, String> {
    Optional<Department> findById(Long id);
    Optional<Department> findByCode(String code);
}
