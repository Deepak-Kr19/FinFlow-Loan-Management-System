package com.finflow.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.finflow.application.entity.LoanApplication;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByUserId(Long userId);
}