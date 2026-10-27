package com.meridian.smartcampus.repository;

import com.meridian.smartcampus.model.LeaveRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRequestRepository extends MongoRepository<LeaveRequest, String> {
    Optional<LeaveRequest> findById(Long id);
    List<LeaveRequest> findByApplicantType(String applicantType);
    List<LeaveRequest> findByApplicantUsername(String applicantUsername);
    List<LeaveRequest> findByAssignedApproverRole(String role);
    List<LeaveRequest> findByAssignedApproverUsername(String username);
    List<LeaveRequest> findByStatus(String status);
}
