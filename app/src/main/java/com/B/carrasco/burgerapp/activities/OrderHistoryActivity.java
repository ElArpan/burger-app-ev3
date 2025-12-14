package com.B.carrasco.burgerapp.activities;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryActivity extends AppCompatActivity {

    private ListView lvOrders;
    private LinearLayout layoutEmpty;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Datos
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
            finish(); // Si no está logueado, sacar de aquí
            return;
        }

        // Consulta: Pedidos donde el userId sea igual al mío, ordenados por fecha
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
                            // Ordenamos manualmente por fecha (más reciente primero)
                            // Nota: Firestore requiere un índice compuesto para ordenar + filtrar,
                            // así que lo hacemos en Java para evitar complicaciones de configuración ahora.
                            List<DocumentSnapshot> docs = value.getDocuments();

                            // Ordenar lista en Java (Descendente)
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

    // Adaptador Interno
    class OrderHistoryAdapter extends ArrayAdapter<DocumentSnapshot> {

        public OrderHistoryAdapter() {
            super(OrderHistoryActivity.this, R.layout.item_order_history, orderList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_order_history, parent, false);
            }

            DocumentSnapshot doc = getItem(position);

            // Vinculamos vistas
            TextView tvDate = convertView.findViewById(R.id.tvOrderDate);
            TextView tvStatus = convertView.findViewById(R.id.tvOrderStatus);
            TextView tvName = convertView.findViewById(R.id.tvBurgerName);
            TextView tvDetails = convertView.findViewById(R.id.tvOrderDetails);
            TextView tvPrice = convertView.findViewById(R.id.tvOrderPrice);
            View statusStrip = convertView.findViewById(R.id.viewStatusStrip);

            // Obtener datos
            Double price = doc.getDouble("totalPrice");
            String meatType = doc.getString("meatType");
            Boolean isDouble = doc.getBoolean("isDouble");
            List<String> sauces = (List<String>) doc.get("sauces");
            Date date = doc.getDate("createdAt");
            String status = doc.getString("status");
            if (status == null) status = "Pendiente";

            // Formatear
            tvPrice.setText("$" + (price != null ? price.intValue() : 0));

            if (date != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM - HH:mm", Locale.getDefault());
                tvDate.setText(sdf.format(date));
            }

            // Nombre dinámico
            String burgerTitle = "Hamburguesa " + (meatType != null ? meatType : "Clásica");
            if (Boolean.TRUE.equals(isDouble)) burgerTitle += " Doble";
            tvName.setText(burgerTitle);

            // Detalles
            String sauceText = (sauces != null && !sauces.isEmpty()) ? "Con " + String.join(", ", sauces) : "Sin salsas";
            tvDetails.setText(sauceText);

            // --- Lógica Visual de Estados ---
            switch (status) {
                case "Listo":
                case "Entregado":
                    tvStatus.setText("LISTO PARA RETIRAR");
                    tvStatus.setTextColor(Color.parseColor("#388E3C")); // Verde oscuro
                    tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E8F5E9"))); // Verde claro
                    statusStrip.setBackgroundColor(Color.parseColor("#4CAF50"));
                    break;
                case "Cancelado":
                    tvStatus.setText("CANCELADO");
                    tvStatus.setTextColor(Color.parseColor("#D32F2F"));
                    tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFEBEE")));
                    statusStrip.setBackgroundColor(Color.parseColor("#D32F2F"));
                    break;
                default: // Pendiente
                    tvStatus.setText("EN COCINA...");
                    tvStatus.setTextColor(Color.parseColor("#F57C00")); // Naranja
                    tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF3E0")));
                    statusStrip.setBackgroundColor(Color.parseColor("#FF9800"));
                    break;
            }

            return convertView;
        }
    }
}