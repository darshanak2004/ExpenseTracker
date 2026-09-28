package com.expensetracker.controller;

import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @GetMapping
    public List<Expense> listExpenses() {
        return expenseRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpense(@PathVariable Long id) {
        return expenseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Expense> addExpense(@Valid @RequestBody Expense expense) {
        Expense saved = expenseRepository.save(expense);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable Long id, @Valid @RequestBody Expense updated) {
        return expenseRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(updated.getTitle());
                    existing.setAmount(updated.getAmount());
                    existing.setCategory(updated.getCategory());
                    existing.setDate(updated.getDate());
                    return ResponseEntity.ok(expenseRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        if (!expenseRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        expenseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public Map<String, Double> categorySummary() {
        return expenseRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }
        // GET /api/expenses/months - list all year-months that have data, e.g. ["2026-08", "2026-07"]
    @GetMapping("/months")
    public List<String> availableMonths() {
        return expenseRepository.findDistinctMonths();
    }

    // GET /api/expenses/by-month?year=2026&month=7 - expenses for one specific month
    @GetMapping("/by-month")
    public List<Expense> expensesByMonth(@RequestParam int year, @RequestParam int month) {
        java.time.LocalDate start = java.time.LocalDate.of(year, month, 1);
        java.time.LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return expenseRepository.findByDateBetween(start, end);
    }
}
