package com.B.carrasco.burgerapp.models;

public class Order {
    private int id;
    private int userId;
    private int burgerId;
    private String status;
    private String depositPath;
    private boolean depositVerified;
    private double totalAmount;
    private String createdAt;

    // Constructores
    public Order() {}

    public Order(int userId, int burgerId, double totalAmount) {
        this.userId = userId;
        this.burgerId = burgerId;
        this.totalAmount = totalAmount;
        this.status = "pending";
        this.depositVerified = false;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getBurgerId() { return burgerId; }
    public void setBurgerId(int burgerId) { this.burgerId = burgerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDepositPath() { return depositPath; }
    public void setDepositPath(String depositPath) { this.depositPath = depositPath; }

    public boolean isDepositVerified() { return depositVerified; }
    public void setDepositVerified(boolean depositVerified) { this.depositVerified = depositVerified; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}