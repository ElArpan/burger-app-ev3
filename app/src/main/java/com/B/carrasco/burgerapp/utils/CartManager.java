package com.B.carrasco.burgerapp.utils;

import com.B.carrasco.burgerapp.models.Burger;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<Burger> currentOrder;

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
    }

    public double getTotalOrderPrice() {
        double total = 0;
        for (Burger b : currentOrder) {
            total += b.getTotalPrice();
        }
        return total;
    }

    // Genera un resumen en texto de TODO el pedido para guardar en Firebase
    public String getOrderSummary() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentOrder.size(); i++) {
            Burger b = currentOrder.get(i);
            sb.append("🍔 Burger #").append(i + 1).append(": ").append(b.getName()).append("\n");
            // Aquí deberías iterar los ingredientes si tu modelo Burger los tiene accesibles
            sb.append("   Precio: $").append((int)b.getTotalPrice()).append("\n\n");
        }
        return sb.toString();
    }
}