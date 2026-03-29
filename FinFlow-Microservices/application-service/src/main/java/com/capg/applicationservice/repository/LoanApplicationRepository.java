package com.capg.applicationservice.repository;

import com.capg.applicationservice.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for LoanApplication entity.
 * Provides CRUD operations and custom query methods for the loan_applications table.
 */
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    /**
     * Find all loan applications belonging to a specific user.
     * Used by applicants to view their own applications.
     *
     * @param userId the user's ID
     * @return list of loan applications created by the user
     */
    List<LoanApplication> findByUserId(Long userId);
}
