package com.finpro.FinancePro.repository;

import com.finpro.FinancePro.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find transactions for a user within a date range
    List<Transaction> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // Find transactions by type (INCOME or EXPENSE) for a user
    List<Transaction> findByUserIdAndType(Long userId, String type);

    // Find all transactions by user ID
    List<Transaction> findByUserId(Long userId);

}
