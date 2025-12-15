package com.B.carrasco.burgerapp.utils;

import com.B.carrasco.burgerapp.models.Burger;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<Burger> currentOrder;

    // --- DATOS DE ENTREGA ---
    private String deliveryMode = ""; // "Domicilio" o "Retiro"
    private String deliveryAddress = "";
    private double tipAmount = 0.0;

    private CartManager() {
        currentOrder = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addBurger(Burger burger) {
        currentOrder.add(burger);
    }

    public List<Burger> getOrder() {
        return currentOrder;
    }

    public void clearCart() {
        currentOrder.clear();
        // Reseteamos datos de entrega también
        deliveryMode = "";
        deliveryAddress = "";
        tipAmount = 0.0;
    }

    public double getTotalOrderPrice() {
        double total = 0;
        for (Burger b : currentOrder) {
            total += b.getTotalPrice();
        }
        return total;
    }

    // Getters y Setters de Entrega
    public String getDeliveryMode() { return deliveryMode; }
    public void setDeliveryMode(String deliveryMode) { this.deliveryMode = deliveryMode; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public double getTipAmount() { return tipAmount; }
    public void setTipAmount(double tipAmount) { this.tipAmount = tipAmount; }

    // Genera un resumen en texto
    public String getOrderSummary() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentOrder.size(); i++) {
            Burger b = currentOrder.get(i);
            sb.append("🍔 Burger #").append(i + 1).append(": ").append(b.getName()).append("\n");

            // Si tienes ingredientes en el modelo, podrías listarlos aquí
            // Por simplicidad mostramos el nombre compuesto

            sb.append("   Valor: $").append((int)b.getTotalPrice()).append("\n\n");
        }
        return sb.toString();
    }
}