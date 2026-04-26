package com.calorietracker.controller;

import com.calorietracker.model.Food;
import com.calorietracker.repository.FoodRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private final FoodRepository foodRepository;

    public FoodController(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    private ResponseEntity<?> requireAuth(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        return null;
    }

    // GET /api/foods — return all foods (used to populate food picker)
    @GetMapping
    public ResponseEntity<?> getAllFoods(HttpSession session) {
        ResponseEntity<?> authCheck = requireAuth(session);
        if (authCheck != null) return authCheck;

        List<Food> foods = foodRepository.findAll();
        return ResponseEntity.ok(foods);
    }

    // GET /api/foods/{id} — get a single food by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getFoodById(@PathVariable int id, HttpSession session) {
        ResponseEntity<?> authCheck = requireAuth(session);
        if (authCheck != null) return authCheck;

        Optional<Food> food = foodRepository.findById(id);
        return food.<ResponseEntity<?>>map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Food not found")));
    }

    // GET /api/foods/search?q=chicken — dynamic query 1: ILIKE name search
    @GetMapping("/search")
    public ResponseEntity<?> searchFoods(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            HttpSession session) {

        ResponseEntity<?> authCheck = requireAuth(session);
        if (authCheck != null) return authCheck;

        if (q != null && !q.isBlank()) {
            // Dynamic query 1: search by food name with ILIKE
            return ResponseEntity.ok(foodRepository.searchByName(q.trim()));
        }
        if (category != null && !category.isBlank()) {
            // Dynamic query 6: search by category with ILIKE
            return ResponseEntity.ok(foodRepository.searchByCategory(category.trim()));
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Provide 'q' or 'category' query parameter"));
    }
}
