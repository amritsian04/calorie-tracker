package com.calorietracker.repository;

import com.calorietracker.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return user;
    };

    public Optional<User> findByEmail(String email) {
        String sql = """
                SELECT user_id, first_name, last_name, email, password_hash, created_at
                FROM users
                WHERE email = ?
                """;
        List<User> results = jdbc.query(sql, userRowMapper, email);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<User> findById(int userId) {
        String sql = """
                SELECT user_id, first_name, last_name, email, password_hash, created_at
                FROM users
                WHERE user_id = ?
                """;
        List<User> results = jdbc.query(sql, userRowMapper, userId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public User save(User user) {
        String sql = """
                INSERT INTO users (first_name, last_name, email, password_hash)
                VALUES (?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            return ps;
        }, keyHolder);
        user.setUserId(keyHolder.getKey().intValue());
        return user;
    }
}
