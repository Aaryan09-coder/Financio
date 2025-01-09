package com.finpro.FinancePro.repository;

import com.finpro.FinancePro.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserId(Long userId);
    Optional<Budget> findByUserIdAndPeriod(Long userId, String period);
}
