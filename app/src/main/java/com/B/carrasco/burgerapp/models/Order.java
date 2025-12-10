package com.B.carrasco.burgerapp.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Order {
    private int id;
    private int userId;
    private String burgerName;
    private double totalAmount;
    private String status;
    private long createdAt;

    public Order() {}

    public Order(int userId, String burgerName, double totalAmount, String status) {
        this.userId = userId;
        this.burgerName = burgerName;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getBurgerName() { return burgerName; }
    public void setBurgerName(String burgerName) { this.burgerName = burgerName; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Métodos útiles
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(createdAt));
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(createdAt));
    }

    public String getFormattedAmount() {
        return "$" + totalAmount;
    }
}