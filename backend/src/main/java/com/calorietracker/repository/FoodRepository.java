package com.calorietracker.repository;

import com.calorietracker.model.Food;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FoodRepository {

    private final JdbcTemplate jdbc;

    public FoodRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Food> foodRowMapper = (rs, rowNum) -> {
        Food food = new Food();
        food.setFoodId(rs.getInt("food_id"));
        food.setFoodName(rs.getString("food_name"));
        food.setCategory(rs.getString("category"));
        food.setServingSize(rs.getBigDecimal("serving_size"));
        food.setCalories(rs.getBigDecimal("calories"));
        food.setProtein(rs.getBigDecimal("protein"));
        food.setCarbs(rs.getBigDecimal("carbs"));
        food.setFat(rs.getBigDecimal("fat"));
        return food;
    };

    public List<Food> findAll() {
        String sql = """
                SELECT food_id, food_name, category, serving_size, calories, protein, carbs, fat
                FROM food
                ORDER BY food_name
                """;
        return jdbc.query(sql, foodRowMapper);
    }

    public Optional<Food> findById(int foodId) {
        String sql = """
                SELECT food_id, food_name, category, serving_size, calories, protein, carbs, fat
                FROM food
                WHERE food_id = ?
                """;
        List<Food> results = jdbc.query(sql, foodRowMapper, foodId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    // Dynamic query 1: search food by name using ILIKE (case-insensitive partial match)
    public List<Food> searchByName(String name) {
        String sql = """
                SELECT food_id, food_name, category, serving_size, calories, protein, carbs, fat
                FROM food
                WHERE food_name ILIKE ?
                ORDER BY food_name
                """;
        return jdbc.query(sql, foodRowMapper, "%" + name + "%");
    }

    // Dynamic query 6: search food by category using ILIKE
    public List<Food> searchByCategory(String category) {
        String sql = """
                SELECT food_id, food_name, category, serving_size, calories, protein, carbs, fat
                FROM food
                WHERE category ILIKE ?
                ORDER BY food_name
                """;
        return jdbc.query(sql, foodRowMapper, "%" + category + "%");
    }
}
