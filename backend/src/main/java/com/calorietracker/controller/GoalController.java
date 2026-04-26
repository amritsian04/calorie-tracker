package com.calorietracker.controller;

import com.calorietracker.model.DailyGoal;
import com.calorietracker.repository.DailyGoalRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final DailyGoalRepository dailyGoalRepository;

    public GoalController(DailyGoalRepository dailyGoalRepository) {
        this.dailyGoalRepository = dailyGoalRepository;
    }

    private Integer getSessionUserId(HttpSession session) {
        return (Integer) session.getAttribute("userId");
    }

    // GET /api/goals?date=2025-04-26 — fetch goal for a specific date
    @GetMapping
    public ResponseEntity<?> getGoal(@RequestParam String date, HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        Optional<DailyGoal> goal = dailyGoalRepository.findByUserIdAndDate(userId, LocalDate.parse(date));
        return goal.<ResponseEntity<?>>map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "No goal set for this date")));
    }

    // GET /api/goals/all — fetch all goals for the current user
    @GetMapping("/all")
    public ResponseEntity<?> getAllGoals(HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        return ResponseEntity.ok(dailyGoalRepository.findByUserId(userId));
    }

    // POST /api/goals — create a goal (upsert: update if one already exists for that date)
    @PostMapping
    public ResponseEntity<?> createGoal(@RequestBody Map<String, Object> body, HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        String dateStr = (String) body.get("goalDate");
        Object calObj  = body.get("dailyCalorieGoal");

        if (dateStr == null || calObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "goalDate and dailyCalorieGoal are required"));
        }

        LocalDate goalDate = LocalDate.parse(dateStr);

        // Upsert: if a goal already exists for this user+date, update it instead
        Optional<DailyGoal> existing = dailyGoalRepository.findByUserIdAndDate(userId, goalDate);
        if (existing.isPresent()) {
            DailyGoal goal = existing.get();
            goal.setDailyCalorieGoal(Integer.parseInt(calObj.toString()));
            if (body.containsKey("proteinGoal")) goal.setProteinGoal(Integer.parseInt(body.get("proteinGoal").toString()));
            if (body.containsKey("carbGoal"))    goal.setCarbGoal(Integer.parseInt(body.get("carbGoal").toString()));
            if (body.containsKey("fatGoal"))     goal.setFatGoal(Integer.parseInt(body.get("fatGoal").toString()));
            dailyGoalRepository.update(goal);
            return ResponseEntity.ok(goal);
        }

        DailyGoal goal = new DailyGoal();
        goal.setUserId(userId);
        goal.setGoalDate(goalDate);
        goal.setDailyCalorieGoal(Integer.parseInt(calObj.toString()));
        goal.setProteinGoal(body.containsKey("proteinGoal") ? Integer.parseInt(body.get("proteinGoal").toString()) : 0);
        goal.setCarbGoal(body.containsKey("carbGoal")       ? Integer.parseInt(body.get("carbGoal").toString())    : 0);
        goal.setFatGoal(body.containsKey("fatGoal")         ? Integer.parseInt(body.get("fatGoal").toString())     : 0);

        return ResponseEntity.ok(dailyGoalRepository.save(goal));
    }

    // PUT /api/goals/{id} — update an existing goal by ID
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGoal(
            @PathVariable int id,
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        Integer userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        // Verify ownership by finding among this user's goals
        Optional<DailyGoal> goalOpt = dailyGoalRepository.findByUserId(userId)
                .stream().filter(g -> g.getGoalId().equals(id)).findFirst();
        if (goalOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Goal not found"));

        DailyGoal goal = goalOpt.get();
        if (body.containsKey("dailyCalorieGoal")) goal.setDailyCalorieGoal(Integer.parseInt(body.get("dailyCalorieGoal").toString()));
        if (body.containsKey("proteinGoal"))      goal.setProteinGoal(Integer.parseInt(body.get("proteinGoal").toString()));
        if (body.containsKey("carbGoal"))         goal.setCarbGoal(Integer.parseInt(body.get("carbGoal").toString()));
        if (body.containsKey("fatGoal"))          goal.setFatGoal(Integer.parseInt(body.get("fatGoal").toString()));

        int updated = dailyGoalRepository.update(goal);
        if (updated == 0) return ResponseEntity.status(404).body(Map.of("error", "Goal not found"));
        return ResponseEntity.ok(goal);
    }

    // DELETE /api/goals/{id} — delete a goal by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable int id, HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        int deleted = dailyGoalRepository.delete(id, userId);
        if (deleted == 0) return ResponseEntity.status(404).body(Map.of("error", "Goal not found"));
        return ResponseEntity.ok(Map.of("message", "Goal deleted"));
    }
}
