package com.calorietracker.controller;

import com.calorietracker.model.DailySummary;
import com.calorietracker.model.GoalComparison;
import com.calorietracker.model.WeeklySummary;
import com.calorietracker.repository.SummaryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    private final SummaryRepository summaryRepository;

    public SummaryController(SummaryRepository summaryRepository) {
        this.summaryRepository = summaryRepository;
    }

    private Integer getSessionUserId(HttpSession session) {
        return (Integer) session.getAttribute("userId");
    }

    // GET /api/summary/daily?date=2025-04-26
    // Dynamic query 2: JOIN meal + meal_entry + food, GROUP BY date, filter by user + date
    @GetMapping("/daily")
    public ResponseEntity<?> getDailySummary(@RequestParam String date, HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        LocalDate localDate = LocalDate.parse(date);
        Optional<DailySummary> summary = summaryRepository.getDailySummary(userId, localDate);

        if (summary.isEmpty()) {
            // No meals logged yet — return zeroed summary rather than 404
            DailySummary empty = new DailySummary();
            empty.setDate(localDate);
            empty.setTotalCalories(BigDecimal.ZERO);
            empty.setTotalProtein(BigDecimal.ZERO);
            empty.setTotalCarbs(BigDecimal.ZERO);
            empty.setTotalFat(BigDecimal.ZERO);
            return ResponseEntity.ok(empty);
        }
        return ResponseEntity.ok(summary.get());
    }

    // GET /api/summary/weekly?start=2025-04-20&end=2025-04-26
    // Dynamic query 3: JOIN + GROUP BY over a date range
    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklySummary(
            @RequestParam String start,
            @RequestParam String end,
            HttpSession session) {

        Integer userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate   = LocalDate.parse(end);

        List<WeeklySummary> weekly = summaryRepository.getWeeklySummary(userId, startDate, endDate);
        return ResponseEntity.ok(weekly);
    }

    // GET /api/summary/goal-comparison?date=2025-04-26
    // Dynamic query 4: JOIN meal + meal_entry + food + daily_goal, filter by user + date
    @GetMapping("/goal-comparison")
    public ResponseEntity<?> getGoalComparison(@RequestParam String date, HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        LocalDate localDate = LocalDate.parse(date);
        Optional<GoalComparison> comparison = summaryRepository.getGoalComparison(userId, localDate);

        return comparison.<ResponseEntity<?>>map(ResponseEntity::ok)
                         .orElseGet(() -> ResponseEntity.status(404)
                                 .body(Map.of("error", "No goal set for " + date)));
    }
}
