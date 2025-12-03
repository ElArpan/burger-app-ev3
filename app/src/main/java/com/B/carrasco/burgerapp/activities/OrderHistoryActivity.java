package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.adapters.OrderAdapter;
import com.B.carrasco.burgerapp.database.DatabaseHelper;
import com.B.carrasco.burgerapp.models.Order;
import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {
    private ListView lvOrders;
    private LinearLayout tvEmpty;  // Cambiado de TextView a LinearLayout
    private DatabaseHelper dbHelper;
    private List<Order> orderList;
    private OrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        initViews();
        loadOrders();
    }

    private void initViews() {
        lvOrders = findViewById(R.id.lvOrders);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    private void loadOrders() {
        // Datos de ejemplo
        orderList = getSampleOrders();

        if (orderList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            lvOrders.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            lvOrders.setVisibility(View.VISIBLE);

            adapter = new OrderAdapter(this, orderList);
            adapter.setOnReorderClickListener(new OrderAdapter.OnReorderClickListener() {
                @Override
                public void onReorderClick(Order order) {
                    // Lógica para repetir el pedido
                    repeatOrder(order);
                }
            });
            lvOrders.setAdapter(adapter);
        }
    }

    private void repeatOrder(Order order) {
        // Aquí puedes implementar la lógica para repetir el pedido
        // Por ejemplo, abrir BuildBurgerActivity con los datos del pedido
        Intent intent = new Intent(OrderHistoryActivity.this, BuildBurgerActivity.class);
        intent.putExtra("REPEAT_ORDER", true);
        intent.putExtra("BURGER_NAME", order.getBurgerName());
        intent.putExtra("BURGER_PRICE", order.getTotalAmount());
        startActivity(intent);

        // Mientras tanto, mostramos un Toast
        // Toast.makeText(this, "Repitiendo pedido: " + order.getBurgerName(), Toast.LENGTH_SHORT).show();
    }

    private List<Order> getSampleOrders() {
        List<Order> orders = new ArrayList<>();

        // Pedidos de ejemplo
        Order order1 = new Order(1, "Doble Hamburguesa BBQ", 3500, "Entregado");
        order1.setCreatedAt(System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000L));
        orders.add(order1);

        Order order2 = new Order(1, "Hamburguesa Tradicional", 2800, "Entregado");
        order2.setCreatedAt(System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L));
        orders.add(order2);

        Order order3 = new Order(1, "Hamburguesa Especial", 4200, "Pendiente");
        order3.setCreatedAt(System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000L));
        orders.add(order3);

        Order order4 = new Order(1, "Hamburguesa Mega", 5100, "Cancelado");
        order4.setCreatedAt(System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L));
        orders.add(order4);

        Order order5 = new Order(1, "Hamburguesa Clásica", 2300, "Entregado");
        order5.setCreatedAt(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L));
        orders.add(order5);

        return orders;
    }
}