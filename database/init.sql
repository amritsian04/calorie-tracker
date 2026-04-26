-- Calorie Tracker Database Schema
-- PostgreSQL

CREATE TABLE users (
    user_id        SERIAL PRIMARY KEY,
    first_name     VARCHAR(50)  NOT NULL,
    last_name      VARCHAR(50)  NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE daily_goal (
    goal_id            SERIAL PRIMARY KEY,
    user_id            INTEGER NOT NULL
                       REFERENCES users(user_id) ON DELETE CASCADE,
    goal_date          DATE NOT NULL,
    daily_calorie_goal INTEGER NOT NULL CHECK (daily_calorie_goal > 0),
    protein_goal       INTEGER DEFAULT 0,
    carb_goal          INTEGER DEFAULT 0,
    fat_goal           INTEGER DEFAULT 0,
    UNIQUE(user_id, goal_date)
);

CREATE TABLE meal (
    meal_id    SERIAL PRIMARY KEY,
    user_id    INTEGER NOT NULL
               REFERENCES users(user_id) ON DELETE CASCADE,
    meal_name  VARCHAR(100),
    meal_date  DATE NOT NULL,
    meal_type  VARCHAR(10) NOT NULL
               CHECK (meal_type IN ('breakfast', 'lunch', 'dinner', 'snack'))
);

CREATE TABLE food (
    food_id      SERIAL PRIMARY KEY,
    food_name    VARCHAR(100) NOT NULL,
    category     VARCHAR(50),
    serving_size DECIMAL(8,2) NOT NULL DEFAULT 1
                 CHECK (serving_size > 0),
    calories     DECIMAL(8,2) NOT NULL CHECK (calories >= 0),
    protein      DECIMAL(8,2) NOT NULL CHECK (protein >= 0),
    carbs        DECIMAL(8,2) NOT NULL CHECK (carbs >= 0),
    fat          DECIMAL(8,2) NOT NULL CHECK (fat >= 0)
);

CREATE TABLE meal_entry (
    meal_entry_id SERIAL PRIMARY KEY,
    meal_id       INTEGER NOT NULL
                  REFERENCES meal(meal_id) ON DELETE CASCADE,
    food_id       INTEGER NOT NULL
                  REFERENCES food(food_id) ON DELETE CASCADE,
    quantity      DECIMAL(5,2) NOT NULL CHECK (quantity > 0)
);

-- Seed some common foods
INSERT INTO food (food_name, category, serving_size, calories, protein, carbs, fat) VALUES
('Chicken Breast (grilled)', 'Protein', 100, 165, 31, 0, 3.6),
('Brown Rice', 'Grain', 100, 112, 2.3, 23.5, 0.8),
('Broccoli', 'Vegetable', 100, 34, 2.8, 7, 0.4),
('Banana', 'Fruit', 118, 105, 1.3, 27, 0.4),
('Egg (large)', 'Protein', 50, 72, 6.3, 0.4, 4.8),
('Whole Wheat Bread', 'Grain', 28, 69, 3.6, 12, 1),
('Salmon (baked)', 'Protein', 100, 208, 20, 0, 13),
('Sweet Potato', 'Vegetable', 130, 112, 2, 26, 0.1),
('Greek Yogurt (plain)', 'Dairy', 150, 100, 17, 6, 0.7),
('Almonds', 'Nuts', 28, 164, 6, 6, 14),
('Apple', 'Fruit', 182, 95, 0.5, 25, 0.3),
('Oatmeal', 'Grain', 40, 150, 5, 27, 2.5),
('Ground Beef (lean)', 'Protein', 100, 250, 26, 0, 15),
('Avocado', 'Fruit', 150, 240, 3, 12, 22),
('Spinach', 'Vegetable', 100, 23, 2.9, 3.6, 0.4),
('Milk (2%)', 'Dairy', 240, 122, 8, 12, 5),
('Peanut Butter', 'Nuts', 32, 190, 7, 7, 16),
('White Rice', 'Grain', 100, 130, 2.7, 28, 0.3),
('Cheddar Cheese', 'Dairy', 28, 113, 7, 0.4, 9.3),
('Orange', 'Fruit', 131, 62, 1.2, 15, 0.2);
