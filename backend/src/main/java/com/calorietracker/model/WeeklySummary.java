package com.calorietracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class WeeklySummary {

    private LocalDate mealDate;
    private BigDecimal totalCalories;
    private BigDecimal totalProtein;
    private BigDecimal totalCarbs;
    private BigDecimal totalFat;
    private Integer mealCount;

    public WeeklySummary() {}

    public LocalDate getMealDate() { return mealDate; }
    public void setMealDate(LocalDate mealDate) { this.mealDate = mealDate; }

    public BigDecimal getTotalCalories() { return totalCalories; }
    public void setTotalCalories(BigDecimal totalCalories) { this.totalCalories = totalCalories; }

    public BigDecimal getTotalProtein() { return totalProtein; }
    public void setTotalProtein(BigDecimal totalProtein) { this.totalProtein = totalProtein; }

    public BigDecimal getTotalCarbs() { return totalCarbs; }
    public void setTotalCarbs(BigDecimal totalCarbs) { this.totalCarbs = totalCarbs; }

    public BigDecimal getTotalFat() { return totalFat; }
    public void setTotalFat(BigDecimal totalFat) { this.totalFat = totalFat; }

    public Integer getMealCount() { return mealCount; }
    public void setMealCount(Integer mealCount) { this.mealCount = mealCount; }
}
