package com.B.carrasco.burgerapp.models;

import java.io.Serializable;

public class Ingredient implements Serializable {
    private String id;
    private String name;
    private boolean available;

    public Ingredient() {} // Constructor vacío

    public Ingredient(String name, boolean available) {
        this.name = name;
        this.available = available;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}