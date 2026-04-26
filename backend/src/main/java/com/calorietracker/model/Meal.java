package com.calorietracker.model;

import java.time.LocalDate;

public class Meal {

    private Integer mealId;
    private Integer userId;
    private String mealName;
    private LocalDate mealDate;
    private String mealType; // breakfast, lunch, dinner, snack

    public Meal() {}

    public Integer getMealId() { return mealId; }
    public void setMealId(Integer mealId) { this.mealId = mealId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }

    public LocalDate getMealDate() { return mealDate; }
    public void setMealDate(LocalDate mealDate) { this.mealDate = mealDate; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
}
