package com.B.carrasco.burgerapp.models;

import java.util.Date;

public class OrderModel {
    // Campos básicos
    private String clientEmail;
    private String orderDescription;
    private Double totalPrice;
    private String paymentMethod;
    private String status;
    private Date createdAt;
    private Double payWith;
    private Double changeDue;
    private String userId; // Útil para el historial del cliente

    // --- NUEVOS CAMPOS DE LOGÍSTICA ---
    private String deliveryMode;
    private String deliveryAddress;
    private Double tipAmount;

    // Constructor vacío OBLIGATORIO para Firebase
    public OrderModel() {}

    // --- GETTERS BLINDADOS (Anti-Crash) ---
    // Si el dato viene nulo de Firebase, devolvemos un valor seguro

    public String getClientEmail() {
        return clientEmail != null ? clientEmail : "Cliente Anónimo";
    }

    public String getOrderDescription() {
        return orderDescription != null ? orderDescription : "Sin detalles";
    }

    public double getTotalPrice() {
        return totalPrice != null ? totalPrice : 0.0;
    }

    public String getPaymentMethod() {
        return paymentMethod != null ? paymentMethod : "Desconocido";
    }

    public String getStatus() {
        return status != null ? status : "Pendiente";
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public double getPayWith() {
        return payWith != null ? payWith : 0.0;
    }

    public double getChangeDue() {
        return changeDue != null ? changeDue : 0.0;
    }

    public String getUserId() {
        return userId;
    }

    // Seguridad para los nuevos campos
    public String getDeliveryMode() {
        return deliveryMode != null ? deliveryMode : "Retiro";
    }

    public String getDeliveryAddress() {
        return deliveryAddress != null ? deliveryAddress : "Sin dirección";
    }

    public double getTipAmount() {
        return tipAmount != null ? tipAmount : 0.0;
    }

    // Setters (Necesarios para que Firebase escriba los datos)
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }
    public void setOrderDescription(String orderDescription) { this.orderDescription = orderDescription; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setPayWith(Double payWith) { this.payWith = payWith; }
    public void setChangeDue(Double changeDue) { this.changeDue = changeDue; }
    public void setDeliveryMode(String deliveryMode) { this.deliveryMode = deliveryMode; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public void setTipAmount(Double tipAmount) { this.tipAmount = tipAmount; }
    public void setUserId(String userId) { this.userId = userId; }
}