package com.calorietracker.model;

import java.math.BigDecimal;

public class Food {

    private Integer foodId;
    private String foodName;
    private String category;
    private BigDecimal servingSize;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fat;

    public Food() {}

    public Integer getFoodId() { return foodId; }
    public void setFoodId(Integer foodId) { this.foodId = foodId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getServingSize() { return servingSize; }
    public void setServingSize(BigDecimal servingSize) { this.servingSize = servingSize; }

    public BigDecimal getCalories() { return calories; }
    public void setCalories(BigDecimal calories) { this.calories = calories; }

    public BigDecimal getProtein() { return protein; }
    public void setProtein(BigDecimal protein) { this.protein = protein; }

    public BigDecimal getCarbs() { return carbs; }
    public void setCarbs(BigDecimal carbs) { this.carbs = carbs; }

    public BigDecimal getFat() { return fat; }
    public void setFat(BigDecimal fat) { this.fat = fat; }
}
