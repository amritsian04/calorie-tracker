package com.calorietracker.repository;

import com.calorietracker.model.DailySummary;
import com.calorietracker.model.GoalComparison;
import com.calorietracker.model.WeeklySummary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class SummaryRepository {

    private final JdbcTemplate jdbc;

    public SummaryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Dynamic query 2: daily calorie summary — JOIN meal + meal_entry + food, filter by user + date
    public Optional<DailySummary> getDailySummary(int userId, LocalDate date) {
        String sql = """
                SELECT
                    m.meal_date                                         AS date,
                    COALESCE(SUM(me.quantity * f.calories / f.serving_size), 0) AS total_calories,
                    COALESCE(SUM(me.quantity * f.protein  / f.serving_size), 0) AS total_protein,
                    COALESCE(SUM(me.quantity * f.carbs    / f.serving_size), 0) AS total_carbs,
                    COALESCE(SUM(me.quantity * f.fat      / f.serving_size), 0) AS total_fat
                FROM meal m
                JOIN meal_entry me ON m.meal_id  = me.meal_id
                JOIN food f        ON me.food_id = f.food_id
                WHERE m.user_id = ? AND m.meal_date = ?
                GROUP BY m.meal_date
                """;
        List<DailySummary> results = jdbc.query(sql, (rs, rowNum) -> {
            DailySummary summary = new DailySummary();
            summary.setDate(rs.getDate("date").toLocalDate());
            summary.setTotalCalories(rs.getBigDecimal("total_calories"));
            summary.setTotalProtein(rs.getBigDecimal("total_protein"));
            summary.setTotalCarbs(rs.getBigDecimal("total_carbs"));
            summary.setTotalFat(rs.getBigDecimal("total_fat"));
            return summary;
        }, userId, Date.valueOf(date));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    // Dynamic query 3: weekly summary — JOIN + GROUP BY over a date range
    public List<WeeklySummary> getWeeklySummary(int userId, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT
                    m.meal_date                                                  AS meal_date,
                    COALESCE(SUM(me.quantity * f.calories / f.serving_size), 0) AS total_calories,
                    COALESCE(SUM(me.quantity * f.protein  / f.serving_size), 0) AS total_protein,
                    COALESCE(SUM(me.quantity * f.carbs    / f.serving_size), 0) AS total_carbs,
                    COALESCE(SUM(me.quantity * f.fat      / f.serving_size), 0) AS total_fat,
                    COUNT(DISTINCT m.meal_id)                                   AS meal_count
                FROM meal m
                JOIN meal_entry me ON m.meal_id  = me.meal_id
                JOIN food f        ON me.food_id = f.food_id
                WHERE m.user_id = ?
                  AND m.meal_date BETWEEN ? AND ?
                GROUP BY m.meal_date
                ORDER BY m.meal_date
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            WeeklySummary summary = new WeeklySummary();
            summary.setMealDate(rs.getDate("meal_date").toLocalDate());
            summary.setTotalCalories(rs.getBigDecimal("total_calories"));
            summary.setTotalProtein(rs.getBigDecimal("total_protein"));
            summary.setTotalCarbs(rs.getBigDecimal("total_carbs"));
            summary.setTotalFat(rs.getBigDecimal("total_fat"));
            summary.setMealCount(rs.getInt("meal_count"));
            return summary;
        }, userId, Date.valueOf(startDate), Date.valueOf(endDate));
    }

    // Dynamic query 4: goal vs actual — JOIN meal + meal_entry + food + daily_goal, filter by user + date
    public Optional<GoalComparison> getGoalComparison(int userId, LocalDate date) {
        String sql = """
                SELECT
                    dg.goal_date                                                          AS date,
                    dg.daily_calorie_goal                                                 AS calorie_goal,
                    COALESCE(SUM(me.quantity * f.calories / f.serving_size), 0)          AS actual_calories,
                    dg.protein_goal,
                    COALESCE(SUM(me.quantity * f.protein  / f.serving_size), 0)          AS actual_protein,
                    dg.carb_goal,
                    COALESCE(SUM(me.quantity * f.carbs    / f.serving_size), 0)          AS actual_carbs,
                    dg.fat_goal,
                    COALESCE(SUM(me.quantity * f.fat      / f.serving_size), 0)          AS actual_fat
                FROM daily_goal dg
                LEFT JOIN meal m        ON dg.user_id  = m.user_id  AND dg.goal_date = m.meal_date
                LEFT JOIN meal_entry me ON m.meal_id   = me.meal_id
                LEFT JOIN food f        ON me.food_id  = f.food_id
                WHERE dg.user_id = ? AND dg.goal_date = ?
                GROUP BY dg.goal_date,
                         dg.daily_calorie_goal,
                         dg.protein_goal,
                         dg.carb_goal,
                         dg.fat_goal
                """;
        List<GoalComparison> results = jdbc.query(sql, (rs, rowNum) -> {
            GoalComparison comparison = new GoalComparison();
            comparison.setDate(rs.getDate("date").toLocalDate());
            comparison.setCalorieGoal(rs.getInt("calorie_goal"));
            comparison.setActualCalories(rs.getBigDecimal("actual_calories"));
            comparison.setProteinGoal(rs.getInt("protein_goal"));
            comparison.setActualProtein(rs.getBigDecimal("actual_protein"));
            comparison.setCarbGoal(rs.getInt("carb_goal"));
            comparison.setActualCarbs(rs.getBigDecimal("actual_carbs"));
            comparison.setFatGoal(rs.getInt("fat_goal"));
            comparison.setActualFat(rs.getBigDecimal("actual_fat"));
            return comparison;
        }, userId, Date.valueOf(date));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
