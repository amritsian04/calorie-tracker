package com.calorietracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GoalComparison {

    private LocalDate date;
    private Integer calorieGoal;
    private BigDecimal actualCalories;
    private Integer proteinGoal;
    private BigDecimal actualProtein;
    private Integer carbGoal;
    private BigDecimal actualCarbs;
    private Integer fatGoal;
    private BigDecimal actualFat;

    public GoalComparison() {}

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getCalorieGoal() { return calorieGoal; }
    public void setCalorieGoal(Integer calorieGoal) { this.calorieGoal = calorieGoal; }

    public BigDecimal getActualCalories() { return actualCalories; }
    public void setActualCalories(BigDecimal actualCalories) { this.actualCalories = actualCalories; }

    public Integer getProteinGoal() { return proteinGoal; }
    public void setProteinGoal(Integer proteinGoal) { this.proteinGoal = proteinGoal; }

    public BigDecimal getActualProtein() { return actualProtein; }
    public void setActualProtein(BigDecimal actualProtein) { this.actualProtein = actualProtein; }

    public Integer getCarbGoal() { return carbGoal; }
    public void setCarbGoal(Integer carbGoal) { this.carbGoal = carbGoal; }

    public BigDecimal getActualCarbs() { return actualCarbs; }
    public void setActualCarbs(BigDecimal actualCarbs) { this.actualCarbs = actualCarbs; }

    public Integer getFatGoal() { return fatGoal; }
    public void setFatGoal(Integer fatGoal) { this.fatGoal = fatGoal; }

    public BigDecimal getActualFat() { return actualFat; }
    public void setActualFat(BigDecimal actualFat) { this.actualFat = actualFat; }
}
