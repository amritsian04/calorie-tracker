package com.calorietracker.repository;

import com.calorietracker.model.Meal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class MealRepository {

    private final JdbcTemplate jdbc;

    public MealRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Meal> mealRowMapper = (rs, rowNum) -> {
        Meal meal = new Meal();
        meal.setMealId(rs.getInt("meal_id"));
        meal.setUserId(rs.getInt("user_id"));
        meal.setMealName(rs.getString("meal_name"));
        meal.setMealDate(rs.getDate("meal_date").toLocalDate());
        meal.setMealType(rs.getString("meal_type"));
        return meal;
    };

    // Dynamic query 5: get all meals for a user with JOIN (used by meal-entry sub-queries)
    public List<Meal> findByUserId(int userId) {
        String sql = """
                SELECT meal_id, user_id, meal_name, meal_date, meal_type
                FROM meal
                WHERE user_id = ?
                ORDER BY meal_date DESC, meal_type
                """;
        return jdbc.query(sql, mealRowMapper, userId);
    }

    // Dynamic query (also feeds daily summary): get meals for a user on a specific date
    public List<Meal> findByUserIdAndDate(int userId, LocalDate date) {
        String sql = """
                SELECT meal_id, user_id, meal_name, meal_date, meal_type
                FROM meal
                WHERE user_id = ? AND meal_date = ?
                ORDER BY meal_type
                """;
        return jdbc.query(sql, mealRowMapper, userId, Date.valueOf(date));
    }

    public Optional<Meal> findById(int mealId) {
        String sql = """
                SELECT meal_id, user_id, meal_name, meal_date, meal_type
                FROM meal
                WHERE meal_id = ?
                """;
        List<Meal> results = jdbc.query(sql, mealRowMapper, mealId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Meal save(Meal meal) {
        String sql = """
                INSERT INTO meal (user_id, meal_name, meal_date, meal_type)
                VALUES (?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"meal_id"});
            ps.setInt(1, meal.getUserId());
            ps.setString(2, meal.getMealName());
            ps.setDate(3, Date.valueOf(meal.getMealDate()));
            ps.setString(4, meal.getMealType());
            return ps;
        }, keyHolder);
        meal.setMealId(keyHolder.getKey().intValue());
        return meal;
    }

    public int update(Meal meal) {
        String sql = """
                UPDATE meal
                SET meal_name = ?, meal_type = ?
                WHERE meal_id = ? AND user_id = ?
                """;
        return jdbc.update(sql, meal.getMealName(), meal.getMealType(), meal.getMealId(), meal.getUserId());
    }

    public int delete(int mealId, int userId) {
        String sql = """
                DELETE FROM meal
                WHERE meal_id = ? AND user_id = ?
                """;
        return jdbc.update(sql, mealId, userId);
    }
}
