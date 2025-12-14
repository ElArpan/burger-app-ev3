package com.B.carrasco.burgerapp.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.B.carrasco.burgerapp.R;
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

public class AdminOrdersActivity extends AppCompatActivity {

    private ListView lvOrders;
    private FirebaseFirestore db;
    private List<DocumentSnapshot> orderList = new ArrayList<>();
    private OrdersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // CORRECCIÓN AQUÍ: Quitamos el ".xml"
        setContentView(R.layout.activity_admin_orders);

        lvOrders = findViewById(R.id.lvAdminOrders);
        db = FirebaseFirestore.getInstance();

        setupRealtimeOrders();
    }

    private void setupRealtimeOrders() {
        // Escuchar pedidos en tiempo real, ordenados por fecha (más recientes primero)
        db.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(AdminOrdersActivity.this, "Error cargando pedidos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (value != null) {
                            orderList.clear();
                            orderList.addAll(value.getDocuments());

                            // Si es la primera vez, configuramos el adapter, si no, notificamos cambios
                            if (adapter == null) {
                                adapter = new OrdersAdapter();
                                lvOrders.setAdapter(adapter);
                            } else {
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    // Adaptador Interno para manejar la lista
    class OrdersAdapter extends ArrayAdapter<DocumentSnapshot> {

        public OrdersAdapter() {
            super(AdminOrdersActivity.this, R.layout.item_admin_order, orderList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_admin_order, parent, false);
            }

            DocumentSnapshot doc = getItem(position);

            TextView tvName = convertView.findViewById(R.id.tvClientName);
            TextView tvPrice = convertView.findViewById(R.id.tvOrderPrice);
            TextView tvDetails = convertView.findViewById(R.id.tvBurgerDetails);
            TextView tvIngredients = convertView.findViewById(R.id.tvIngredients);
            TextView tvDate = convertView.findViewById(R.id.tvDate);
            Button btnAction = convertView.findViewById(R.id.btnMarkReady);

            // Obtener datos de forma segura
            String clientName = doc.getString("clientName");
            Double price = doc.getDouble("totalPrice");
            String meatType = doc.getString("meatType");
            Boolean isDouble = doc.getBoolean("isDouble");
            List<String> extras = (List<String>) doc.get("ingredients");
            List<String> sauces = (List<String>) doc.get("sauces");
            Date date = doc.getDate("createdAt");
            String status = doc.getString("status");

            // Rellenar UI
            tvName.setText(clientName != null ? clientName : "Cliente");
            tvPrice.setText("$" + (price != null ? price.intValue() : 0));

            // Formatear fecha
            if (date != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault());
                tvDate.setText(sdf.format(date));
            }

            // Construir detalle
            StringBuilder details = new StringBuilder();
            details.append("• Carne: ").append(meatType);
            if (isDouble != null && isDouble) details.append(" (DOBLE)");
            details.append("\n");

            if (sauces != null && !sauces.isEmpty()) {
                details.append("• Salsas: ").append(String.join(", ", sauces));
            } else {
                details.append("• Sin salsas");
            }
            tvDetails.setText(details.toString());

            // Extras
            if (extras != null && !extras.isEmpty()) {
                tvIngredients.setText("Extras: " + String.join(", ", extras));
                tvIngredients.setVisibility(View.VISIBLE);
            } else {
                tvIngredients.setVisibility(View.GONE);
            }

            // Lógica del Botón según estado
            if ("Pendiente".equals(status)) {
                btnAction.setText("✅ MARCAR LISTO");
                btnAction.setEnabled(true);
                btnAction.setAlpha(1.0f);

                btnAction.setOnClickListener(v -> {
                    // Actualizar estado en Firebase
                    db.collection("orders").document(doc.getId())
                            .update("status", "Listo")
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "¡Pedido marcado como listo!", Toast.LENGTH_SHORT).show());
                });
            } else {
                btnAction.setText("👍 YA ENTREGADO");
                btnAction.setEnabled(false);
                btnAction.setAlpha(0.5f);
            }

            return convertView;
        }
    }
}