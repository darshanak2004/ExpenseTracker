package com.expensetracker.controller;

import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/insights")
@CrossOrigin(origins = "*")
public class InsightsController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${python.service.url:http://localhost:8000}")
    private String pythonServiceUrl;

    @GetMapping
    public Map<String, Object> getInsights() {
        List<Expense> expenses = expenseRepository.findAll();
        String url = pythonServiceUrl + "/analyze";
        Map<String, Object> response = restTemplate.postForObject(url, expenses, Map.class);
        return response;
    }
}
