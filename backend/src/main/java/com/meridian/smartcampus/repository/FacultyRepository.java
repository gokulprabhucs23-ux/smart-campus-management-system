package com.meridian.smartcampus.repository;

import com.meridian.smartcampus.model.Faculty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends MongoRepository<Faculty, String> {
    Optional<Faculty> findById(Long id);
    Optional<Faculty> findByEmail(String email);
    List<Faculty> findByNameContainingIgnoreCase(String search);
    List<Faculty> findByDepartment(String department);
}
