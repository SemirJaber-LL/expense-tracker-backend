package com.personal.project.repository;

import com.personal.project.model.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends MongoRepository<Expense, String> {
    List<Expense> findByCategory(String category);

    List<Expense> findByUserIdAndDateBetween(String userId, LocalDate start, LocalDate end);
}
