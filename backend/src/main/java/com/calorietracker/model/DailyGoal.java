package com.calorietracker.model;

import java.time.LocalDate;

public class DailyGoal {

    private Integer goalId;
    private Integer userId;
    private LocalDate goalDate;
    private Integer dailyCalorieGoal;
    private Integer proteinGoal;
    private Integer carbGoal;
    private Integer fatGoal;

    public DailyGoal() {}

    public Integer getGoalId() { return goalId; }
    public void setGoalId(Integer goalId) { this.goalId = goalId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public LocalDate getGoalDate() { return goalDate; }
    public void setGoalDate(LocalDate goalDate) { this.goalDate = goalDate; }

    public Integer getDailyCalorieGoal() { return dailyCalorieGoal; }
    public void setDailyCalorieGoal(Integer dailyCalorieGoal) { this.dailyCalorieGoal = dailyCalorieGoal; }

    public Integer getProteinGoal() { return proteinGoal; }
    public void setProteinGoal(Integer proteinGoal) { this.proteinGoal = proteinGoal; }

    public Integer getCarbGoal() { return carbGoal; }
    public void setCarbGoal(Integer carbGoal) { this.carbGoal = carbGoal; }

    public Integer getFatGoal() { return fatGoal; }
    public void setFatGoal(Integer fatGoal) { this.fatGoal = fatGoal; }
}
