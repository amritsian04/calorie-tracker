package com.calorietracker.model;

import java.math.BigDecimal;

public class MealEntry {

    private Integer mealEntryId;
    private Integer mealId;
    private Integer foodId;
    private BigDecimal quantity;

    // Denormalized food fields populated by the JOIN query in MealEntryRepository
    private String foodName;
    private String category;
    private BigDecimal servingSize;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fat;

    public MealEntry() {}

    public Integer getMealEntryId() { return mealEntryId; }
    public void setMealEntryId(Integer mealEntryId) { this.mealEntryId = mealEntryId; }

    public Integer getMealId() { return mealId; }
    public void setMealId(Integer mealId) { this.mealId = mealId; }

    public Integer getFoodId() { return foodId; }
    public void setFoodId(Integer foodId) { this.foodId = foodId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

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
