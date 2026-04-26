package com.calorietracker.controller;

import com.calorietracker.model.Meal;
import com.calorietracker.model.MealEntry;
import com.calorietracker.repository.MealEntryRepository;
import com.calorietracker.repository.MealRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    private final MealRepository mealRepository;
    private final MealEntryRepository mealEntryRepository;

    public MealController(MealRepository mealRepository, MealEntryRepository mealEntryRepository) {
        this.mealRepository = mealRepository;
        this.mealEntryRepository = mealEntryRepository;
    }

    private Integer getSessionUserId(HttpSession session) {
        return (Integer) session.getAttribute("userId");
    }

    // ── Meal endpoints ────────────────────────────────────────────────────────

    // GET /api/meals?date=2025-04-26  — get meals for today (or all if no date)
    @GetMapping
    public ResponseEntity<?> getMeals(
            @RequestParam(required = false) String date,
            HttpSession session) {

        Integer userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        if (date != null && !date.isBlank()) {
            LocalDate localDate = LocalDate.parse(date);
            return ResponseEntity.ok(mealRepository.findByUserIdAndDate(userId, localDate));
        }
        return ResponseEntity.ok(mealRepository.findByUserId(userId));
    }

    // POST /api/meals — create a new meal
    @PostMapping
    public ResponseEntity<?> createMeal(@RequestBody Map<String, Object> body, HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        String mealName = (String) body.get("mealName");
        String mealType = (String) body.get("mealType");
        String dateStr  = (String) body.get("mealDate");

        if (mealType == null || dateStr == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "mealType and mealDate are required"));
        }

        Meal meal = new Meal();
        meal.setUserId(userId);
        meal.setMealName(mealName);
        meal.setMealType(mealType);
        meal.setMealDate(LocalDate.parse(dateStr));

        return ResponseEntity.ok(mealRepository.save(meal));
    }

    // PUT /api/meals/{id} — update meal name or type
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMeal(
            @PathVariable int id,
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        Integer userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        Optional<Meal> existing = mealRepository.findById(id);
        if (existing.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Meal not found"));
        if (!existing.get().getUserId().equals(userId)) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        Meal meal = existing.get();
        if (body.containsKey("mealName")) meal.setMealName((String) body.get("mealName"));
        if (body.containsKey("mealType")) meal.setMealType((String) body.get("mealType"));

        int updated = mealRepository.update(meal);
        if (updated == 0) return ResponseEntity.status(404).body(Map.of("error", "Meal not found"));
        return ResponseEntity.ok(meal);
    }

    // DELETE /api/meals/{id} — delete a meal and its entries (CASCADE in DB)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMeal(@PathVariable int id, HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        int deleted = mealRepository.delete(id, userId);
        if (deleted == 0) return ResponseEntity.status(404).body(Map.of("error", "Meal not found"));
        return ResponseEntity.ok(Map.of("message", "Meal deleted"));
    }

    // ── Meal Entry endpoints ──────────────────────────────────────────────────

    // GET /api/meals/{mealId}/entries — list foods logged in a meal
    @GetMapping("/{mealId}/entries")
    public ResponseEntity<?> getEntries(@PathVariable int mealId, HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        // Verify the meal belongs to the session user
        Optional<Meal> meal = mealRepository.findById(mealId);
        if (meal.isEmpty() || !meal.get().getUserId().equals(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "Meal not found"));
        }

        List<MealEntry> entries = mealEntryRepository.findByMealId(mealId);
        return ResponseEntity.ok(entries);
    }

    // POST /api/meals/{mealId}/entries — add a food item to a meal
    @PostMapping("/{mealId}/entries")
    public ResponseEntity<?> addEntry(
            @PathVariable int mealId,
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        Integer userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        Optional<Meal> meal = mealRepository.findById(mealId);
        if (meal.isEmpty() || !meal.get().getUserId().equals(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "Meal not found"));
        }

        Integer foodId  = (Integer) body.get("foodId");
        Object  qtyRaw  = body.get("quantity");

        if (foodId == null || qtyRaw == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "foodId and quantity are required"));
        }

        BigDecimal quantity = new BigDecimal(qtyRaw.toString());

        MealEntry entry = new MealEntry();
        entry.setMealId(mealId);
        entry.setFoodId(foodId);
        entry.setQuantity(quantity);

        MealEntry saved = mealEntryRepository.save(entry);

        // Return with food details by fetching from the JOIN query result
        List<MealEntry> entries = mealEntryRepository.findByMealId(mealId);
        return entries.stream()
                .filter(e -> e.getMealEntryId().equals(saved.getMealEntryId()))
                .findFirst()
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(saved));
    }

    // PUT /api/meals/{mealId}/entries/{entryId} — update quantity
    @PutMapping("/{mealId}/entries/{entryId}")
    public ResponseEntity<?> updateEntry(
            @PathVariable int mealId,
            @PathVariable int entryId,
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        Integer userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        Optional<Meal> meal = mealRepository.findById(mealId);
        if (meal.isEmpty() || !meal.get().getUserId().equals(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "Meal not found"));
        }

        Object qtyRaw = body.get("quantity");
        if (qtyRaw == null) return ResponseEntity.badRequest().body(Map.of("error", "quantity is required"));

        BigDecimal quantity = new BigDecimal(qtyRaw.toString());
        int updated = mealEntryRepository.update(entryId, mealId, quantity);
        if (updated == 0) return ResponseEntity.status(404).body(Map.of("error", "Entry not found"));
        return ResponseEntity.ok(Map.of("message", "Entry updated"));
    }

    // DELETE /api/meals/{mealId}/entries/{entryId} — remove a food from a meal
    @DeleteMapping("/{mealId}/entries/{entryId}")
    public ResponseEntity<?> deleteEntry(
            @PathVariable int mealId,
            @PathVariable int entryId,
            HttpSession session) {

        Integer userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        Optional<Meal> meal = mealRepository.findById(mealId);
        if (meal.isEmpty() || !meal.get().getUserId().equals(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "Meal not found"));
        }

        int deleted = mealEntryRepository.delete(entryId, mealId);
        if (deleted == 0) return ResponseEntity.status(404).body(Map.of("error", "Entry not found"));
        return ResponseEntity.ok(Map.of("message", "Entry deleted"));
    }
}
