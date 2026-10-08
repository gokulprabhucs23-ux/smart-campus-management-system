package com.meridian.smartcampus.repository;

import com.meridian.smartcampus.model.TimetableEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimetableRepository extends MongoRepository<TimetableEntry, String> {
    List<TimetableEntry> findByDepartmentAndSemester(String department, Integer semester);
    List<TimetableEntry> findByDepartmentAndSemesterOrderByPeriodAsc(String department, Integer semester);
    List<TimetableEntry> findByFacultyUsername(String facultyUsername);
    List<TimetableEntry> findByFacultyName(String facultyName);
    Optional<TimetableEntry> findByDepartmentAndSemesterAndDayAndPeriod(String department, Integer semester, String day, Integer period);
    void deleteByDepartmentAndSemesterAndDayAndPeriod(String department, Integer semester, String day, Integer period);
}
