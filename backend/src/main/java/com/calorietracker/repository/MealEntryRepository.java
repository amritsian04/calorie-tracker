package com.calorietracker.repository;

import com.calorietracker.model.MealEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class MealEntryRepository {

    private final JdbcTemplate jdbc;

    public MealEntryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<MealEntry> entryRowMapper = (rs, rowNum) -> {
        MealEntry entry = new MealEntry();
        entry.setMealEntryId(rs.getInt("meal_entry_id"));
        entry.setMealId(rs.getInt("meal_id"));
        entry.setFoodId(rs.getInt("food_id"));
        entry.setQuantity(rs.getBigDecimal("quantity"));
        entry.setFoodName(rs.getString("food_name"));
        entry.setCategory(rs.getString("category"));
        entry.setServingSize(rs.getBigDecimal("serving_size"));
        entry.setCalories(rs.getBigDecimal("calories"));
        entry.setProtein(rs.getBigDecimal("protein"));
        entry.setCarbs(rs.getBigDecimal("carbs"));
        entry.setFat(rs.getBigDecimal("fat"));
        return entry;
    };

    // Dynamic query with JOIN: fetch all entries for a meal, joined with food details
    public List<MealEntry> findByMealId(int mealId) {
        String sql = """
                SELECT me.meal_entry_id,
                       me.meal_id,
                       me.food_id,
                       me.quantity,
                       f.food_name,
                       f.category,
                       f.serving_size,
                       f.calories,
                       f.protein,
                       f.carbs,
                       f.fat
                FROM meal_entry me
                JOIN food f ON me.food_id = f.food_id
                WHERE me.meal_id = ?
                """;
        return jdbc.query(sql, entryRowMapper, mealId);
    }

    public MealEntry save(MealEntry entry) {
        String sql = """
                INSERT INTO meal_entry (meal_id, food_id, quantity)
                VALUES (?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"meal_entry_id"});
            ps.setInt(1, entry.getMealId());
            ps.setInt(2, entry.getFoodId());
            ps.setBigDecimal(3, entry.getQuantity());
            return ps;
        }, keyHolder);
        entry.setMealEntryId(keyHolder.getKey().intValue());
        return entry;
    }

    public int update(int mealEntryId, int mealId, BigDecimal quantity) {
        String sql = """
                UPDATE meal_entry
                SET quantity = ?
                WHERE meal_entry_id = ? AND meal_id = ?
                """;
        return jdbc.update(sql, quantity, mealEntryId, mealId);
    }

    public int delete(int mealEntryId, int mealId) {
        String sql = """
                DELETE FROM meal_entry
                WHERE meal_entry_id = ? AND meal_id = ?
                """;
        return jdbc.update(sql, mealEntryId, mealId);
    }
}
