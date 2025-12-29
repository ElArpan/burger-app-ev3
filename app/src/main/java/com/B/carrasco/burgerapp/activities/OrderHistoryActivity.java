package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.models.OrderModel; // Usamos el modelo nuevo
import com.B.carrasco.burgerapp.utils.CartManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryActivity extends AppCompatActivity {

    private ListView lvOrders;
    private LinearLayout layoutEmpty;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private List<DocumentSnapshot> orderList = new ArrayList<>();
    private OrderHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        loadOrdersFromCloud();
    }

    private void initViews() {
        lvOrders = findViewById(R.id.lvOrders);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void loadOrdersFromCloud() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        db.collection("orders")
                .whereEqualTo("userId", user.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(OrderHistoryActivity.this, "Error al cargar historial", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (value != null) {
                            orderList.clear();
                            List<DocumentSnapshot> docs = value.getDocuments();

                            // Ordenar por fecha (Descendente)
                            docs.sort((d1, d2) -> {
                                Date date1 = d1.getDate("createdAt");
                                Date date2 = d2.getDate("createdAt");
                                if (date1 == null || date2 == null) return 0;
                                return date2.compareTo(date1);
                            });

                            orderList.addAll(docs);
                            updateUI();
                        }
                    }
                });
    }

    private void updateUI() {
        if (orderList.isEmpty()) {
            lvOrders.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            lvOrders.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);

            if (adapter == null) {
                adapter = new OrderHistoryAdapter();
                lvOrders.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    class OrderHistoryAdapter extends ArrayAdapter<DocumentSnapshot> {

        public OrderHistoryAdapter() {
            super(OrderHistoryActivity.this, R.layout.item_admin_order, orderList);
            // NOTA: Usamos item_admin_order porque tiene el diseño de tarjeta que queremos
            // Si tienes un item_order_history.xml específico, úsalo aquí.
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_admin_order, parent, false);
            }

            // Convertir DocumentSnapshot a OrderModel para facilitar uso
            DocumentSnapshot doc = getItem(position);
            OrderModel model = doc.toObject(OrderModel.class);

            // Vinculamos vistas (Usando los IDs de item_admin_order)
            TextView tvDate = convertView.findViewById(R.id.tvOrderTime);
            TextView tvStatus = convertView.findViewById(R.id.tvOrderStatus);
            TextView tvClient = convertView.findViewById(R.id.tvClientName); // Lo usaremos para el título
            TextView tvDesc = convertView.findViewById(R.id.tvOrderDescription);
            TextView tvTotal = convertView.findViewById(R.id.tvTotal);
            View header = convertView.findViewById(R.id.headerContainer);

            // Ocultar botones de admin
            View btnAction = convertView.findViewById(R.id.btnNextStatus);
            if (btnAction != null) btnAction.setVisibility(View.GONE);

            // Llenar datos
            if (model != null) {
                tvClient.setText("Pedido #" + position); // Opcional
                tvDesc.setText(model.getOrderDescription());
                tvTotal.setText("$" + (int)model.getTotalPrice());
                tvStatus.setText(model.getStatus().toUpperCase());

                if (model.getCreatedAt() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM - HH:mm", Locale.getDefault());
                    tvDate.setText(sdf.format(model.getCreatedAt()));
                }

                // Colores
                String status = model.getStatus().toLowerCase();
                if (status.contains("entregado") || status.contains("archivado")) {
                    header.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde
                } else if (status.contains("cancelado")) {
                    header.setBackgroundColor(Color.parseColor("#D32F2F")); // Rojo
                } else {
                    header.setBackgroundColor(Color.parseColor("#FF9800")); // Naranja
                }

                // --- CLIC PARA RE-PEDIR ---
                convertView.setOnClickListener(v -> {
                    Toast.makeText(OrderHistoryActivity.this, "🔄 Cargando pedido anterior...", Toast.LENGTH_SHORT).show();

                    // 1. Limpiar Carrito
                    CartManager.getInstance().clearCart();

                    // 2. Recuperar Logística Antigua (Para ahorrar tiempo)
                    CartManager.getInstance().setDeliveryMode(model.getDeliveryMode());
                    CartManager.getInstance().setDeliveryAddress(model.getDeliveryAddress());
                    CartManager.getInstance().setTipAmount(0.0); // La propina la elige de nuevo por si acaso

                    // 3. Ir a Armar Burger (Pasando la descripción para reconstruir)
                    Intent intent = new Intent(OrderHistoryActivity.this, BuildBurgerActivity.class);
                    intent.putExtra("REORDER_DESC", model.getOrderDescription());
                    startActivity(intent);
                    finish();
                });
            }
            return convertView;
        }
    }
}