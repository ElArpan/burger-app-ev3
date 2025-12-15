package com.B.carrasco.burgerapp.models;

import java.io.Serializable;
import java.util.List;

public class Burger implements Serializable {
    private String id; // Cambiado a String para compatibilidad futura con Firebase
    private String name;
    private String userId; // Cambiado a String para Firebase Auth ID
    private double totalPrice;
    private List<Ingredient> ingredients;

    // Constructor vacío (Obligatorio para Firebase)
    public Burger() {}

    // Constructor simple
    public Burger(String name, List<Ingredient> ingredients, double totalPrice) {
        this.name = name;
        this.ingredients = ingredients;
        this.totalPrice = totalPrice;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients; }
}