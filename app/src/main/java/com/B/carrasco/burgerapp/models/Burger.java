package com.B.carrasco.burgerapp.models;

import java.util.List;

public class Burger {
    private int id;
    private String name;
    private int userId;
    private double totalPrice;
    private List<Ingredient> ingredients;

    // Constructores
    public Burger() {}

    public Burger(String name, int userId, List<Ingredient> ingredients) {
        this.name = name;
        this.userId = userId;
        this.ingredients = ingredients;
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        totalPrice = 0;
        if (ingredients != null) {
            for (Ingredient ingredient : ingredients) {
                totalPrice += ingredient.getPrice();
            }
        }
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
        calculateTotalPrice();
    }
}