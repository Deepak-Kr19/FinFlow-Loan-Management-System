package com.capg.adminservice.repository;

import com.capg.adminservice.entity.Decision;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DecisionRepository extends JpaRepository<Decision, Long> {
}
