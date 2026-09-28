package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByCategory(String category);

    List<Expense> findByDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT DISTINCT FUNCTION('to_char', e.date, 'YYYY-MM') FROM Expense e ORDER BY 1 DESC")
    List<String> findDistinctMonths();
}