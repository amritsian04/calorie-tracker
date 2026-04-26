package com.calorietracker.repository;

import com.calorietracker.model.DailyGoal;
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
public class DailyGoalRepository {

    private final JdbcTemplate jdbc;

    public DailyGoalRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<DailyGoal> goalRowMapper = (rs, rowNum) -> {
        DailyGoal goal = new DailyGoal();
        goal.setGoalId(rs.getInt("goal_id"));
        goal.setUserId(rs.getInt("user_id"));
        goal.setGoalDate(rs.getDate("goal_date").toLocalDate());
        goal.setDailyCalorieGoal(rs.getInt("daily_calorie_goal"));
        goal.setProteinGoal(rs.getInt("protein_goal"));
        goal.setCarbGoal(rs.getInt("carb_goal"));
        goal.setFatGoal(rs.getInt("fat_goal"));
        return goal;
    };

    // Dynamic query: get goal for a specific user + date
    public Optional<DailyGoal> findByUserIdAndDate(int userId, LocalDate date) {
        String sql = """
                SELECT goal_id, user_id, goal_date, daily_calorie_goal, protein_goal, carb_goal, fat_goal
                FROM daily_goal
                WHERE user_id = ? AND goal_date = ?
                """;
        List<DailyGoal> results = jdbc.query(sql, goalRowMapper, userId, Date.valueOf(date));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<DailyGoal> findByUserId(int userId) {
        String sql = """
                SELECT goal_id, user_id, goal_date, daily_calorie_goal, protein_goal, carb_goal, fat_goal
                FROM daily_goal
                WHERE user_id = ?
                ORDER BY goal_date DESC
                """;
        return jdbc.query(sql, goalRowMapper, userId);
    }

    public DailyGoal save(DailyGoal goal) {
        String sql = """
                INSERT INTO daily_goal (user_id, goal_date, daily_calorie_goal, protein_goal, carb_goal, fat_goal)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"goal_id"});
            ps.setInt(1, goal.getUserId());
            ps.setDate(2, Date.valueOf(goal.getGoalDate()));
            ps.setInt(3, goal.getDailyCalorieGoal());
            ps.setInt(4, goal.getProteinGoal() != null ? goal.getProteinGoal() : 0);
            ps.setInt(5, goal.getCarbGoal() != null ? goal.getCarbGoal() : 0);
            ps.setInt(6, goal.getFatGoal() != null ? goal.getFatGoal() : 0);
            return ps;
        }, keyHolder);
        goal.setGoalId(keyHolder.getKey().intValue());
        return goal;
    }

    public int update(DailyGoal goal) {
        String sql = """
                UPDATE daily_goal
                SET daily_calorie_goal = ?, protein_goal = ?, carb_goal = ?, fat_goal = ?
                WHERE goal_id = ? AND user_id = ?
                """;
        return jdbc.update(sql,
                goal.getDailyCalorieGoal(),
                goal.getProteinGoal() != null ? goal.getProteinGoal() : 0,
                goal.getCarbGoal() != null ? goal.getCarbGoal() : 0,
                goal.getFatGoal() != null ? goal.getFatGoal() : 0,
                goal.getGoalId(),
                goal.getUserId());
    }

    public int delete(int goalId, int userId) {
        String sql = """
                DELETE FROM daily_goal
                WHERE goal_id = ? AND user_id = ?
                """;
        return jdbc.update(sql, goalId, userId);
    }
}
