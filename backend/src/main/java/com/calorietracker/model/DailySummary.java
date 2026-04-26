package com.calorietracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailySummary {

    private LocalDate date;
    private BigDecimal totalCalories;
    private BigDecimal totalProtein;
    private BigDecimal totalCarbs;
    private BigDecimal totalFat;

    public DailySummary() {}

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getTotalCalories() { return totalCalories; }
    public void setTotalCalories(BigDecimal totalCalories) { this.totalCalories = totalCalories; }

    public BigDecimal getTotalProtein() { return totalProtein; }
    public void setTotalProtein(BigDecimal totalProtein) { this.totalProtein = totalProtein; }

    public BigDecimal getTotalCarbs() { return totalCarbs; }
    public void setTotalCarbs(BigDecimal totalCarbs) { this.totalCarbs = totalCarbs; }

    public BigDecimal getTotalFat() { return totalFat; }
    public void setTotalFat(BigDecimal totalFat) { this.totalFat = totalFat; }
}
