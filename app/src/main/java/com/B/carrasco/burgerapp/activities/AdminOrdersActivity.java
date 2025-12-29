package com.B.carrasco.burgerapp.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.models.OrderModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AdminOrdersActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter<OrderModel, OrderViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        db = FirebaseFirestore.getInstance();
        rvOrders = findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        setupFirestoreRecycler();
    }

    private void setupFirestoreRecycler() {
        // Mostrar primero los pedidos más nuevos
        Query query = db.collection("orders").orderBy("createdAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<OrderModel> options = new FirestoreRecyclerOptions.Builder<OrderModel>()
                .setQuery(query, OrderModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<OrderModel, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, int position, @NonNull OrderModel model) {
                try {
                    String docId = getSnapshots().getSnapshot(position).getId();

                    // --- 1. DATOS DEL CLIENTE ---
                    holder.tvClientName.setText(model.getClientEmail());
                    holder.tvOrderDescription.setText(model.getOrderDescription());
                    holder.tvTotal.setText("$" + (int)model.getTotalPrice());

                    // Hora del pedido
                    if (model.getCreatedAt() != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        holder.tvOrderTime.setText(sdf.format(model.getCreatedAt()));
                    } else {
                        holder.tvOrderTime.setText("--:--");
                    }

                    // --- 2. DATOS DE ENTREGA (Vital para ti) ---
                    String mode = model.getDeliveryMode();
                    holder.tvDeliveryMode.setText(mode.toUpperCase());

                    if (mode.contains("Domicilio")) {
                        holder.tvDeliveryAddress.setVisibility(View.VISIBLE);
                        holder.tvDeliveryAddress.setText("📍 " + model.getDeliveryAddress());
                        holder.tvDeliveryAddress.setTextColor(Color.parseColor("#C62828")); // Rojo para resaltar
                    } else {
                        holder.tvDeliveryAddress.setVisibility(View.VISIBLE);
                        holder.tvDeliveryAddress.setText("🏠 RETIRO EN LOCAL");
                        holder.tvDeliveryAddress.setTextColor(Color.parseColor("#2E7D32")); // Verde
                    }

                    // Propina
                    if (model.getTipAmount() > 0) {
                        holder.tvTipInfo.setVisibility(View.VISIBLE);
                        holder.tvTipInfo.setText("🎁 Incluye Propina: $" + (int)model.getTipAmount());
                    } else {
                        holder.tvTipInfo.setVisibility(View.GONE);
                    }

                    // --- 3. DINERO Y VUELTO (¡Lo más importante al salir!) ---
                    String method = model.getPaymentMethod();
                    if (method.contains("Efectivo")) {
                        String pagoTxt = "💵 EFECTIVO";
                        // Si hay vuelto, lo mostramos GRANDE
                        if (model.getChangeDue() > 0) {
                            pagoTxt += "\n⚠️ LLEVAR VUELTO: $" + (int)model.getChangeDue();
                            holder.tvPaymentMethod.setTextColor(Color.RED); // Rojo alerta
                            holder.tvPaymentMethod.setTextSize(16); // Más grande
                        } else {
                            pagoTxt += " (Pago exacto o sin datos)";
                            holder.tvPaymentMethod.setTextColor(Color.parseColor("#388E3C"));
                        }
                        holder.tvPaymentMethod.setText(pagoTxt);
                    } else {
                        holder.tvPaymentMethod.setText("📱 TRANSFERENCIA (Revisar Foto)");
                        holder.tvPaymentMethod.setTextColor(Color.parseColor("#1976D2")); // Azul
                    }

                    // --- 4. FLUJO DE ESTADOS (Tus Botones) ---
                    String status = model.getStatus();
                    holder.tvOrderStatus.setText(status.toUpperCase());

                    // Configuración visual según estado
                    int colorFondo;
                    String textoBoton;
                    String proximoEstado;
                    boolean mostrarBoton = true;

                    if (status.equals("Pendiente de Envío") || status.equals("Pendiente")) {
                        colorFondo = Color.parseColor("#FFC107"); // Amarillo
                        textoBoton = "🔥 A COCINA";
                        proximoEstado = "En Cocina";
                    }
                    else if (status.equals("En Cocina")) {
                        colorFondo = Color.parseColor("#FF9800"); // Naranja
                        textoBoton = "🛵 A REPARTO / LISTO";
                        proximoEstado = "En Camino"; // O "Listo para retiro"
                    }
                    else if (status.equals("En Camino") || status.equals("Listo para retiro")) {
                        colorFondo = Color.parseColor("#2196F3"); // Azul
                        textoBoton = "✅ ENTREGADO";
                        proximoEstado = "Entregado";
                    }
                    else if (status.equals("Entregado")) {
                        colorFondo = Color.parseColor("#4CAF50"); // Verde
                        textoBoton = "ARCHIVAR"; // Opcional
                        proximoEstado = "Archivado";
                        mostrarBoton = false; // Ya terminó el proceso
                    }
                    else {
                        colorFondo = Color.GRAY;
                        textoBoton = "";
                        proximoEstado = "";
                        mostrarBoton = false;
                    }

                    holder.headerContainer.setBackgroundColor(colorFondo);

                    if (mostrarBoton) {
                        holder.btnNextStatus.setVisibility(View.VISIBLE);
                        holder.btnNextStatus.setText(textoBoton);

                        String finalProximoEstado = proximoEstado; // Variable final para lambda
                        holder.btnNextStatus.setOnClickListener(v -> {
                            // Actualizamos Firebase -> El cliente lo ve en su celular al instante
                            db.collection("orders").document(docId).update("status", finalProximoEstado);
                            Toast.makeText(AdminOrdersActivity.this, "Cambiado a: " + finalProximoEstado, Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        holder.btnNextStatus.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    Log.e("AdminOrders", "Error visual: " + e.getMessage());
                }
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_order, parent, false);
                return new OrderViewHolder(view);
            }
        };

        rvOrders.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        View headerContainer;
        TextView tvOrderStatus, tvOrderTime, tvClientName, tvOrderDescription, tvPaymentMethod, tvTotal;
        TextView tvDeliveryMode, tvDeliveryAddress, tvTipInfo;
        Button btnNextStatus, btnViewProof;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerContainer = itemView.findViewById(R.id.headerContainer);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvOrderDescription = itemView.findViewById(R.id.tvOrderDescription);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            btnNextStatus = itemView.findViewById(R.id.btnNextStatus);
            btnViewProof = itemView.findViewById(R.id.btnViewProof);
            tvDeliveryMode = itemView.findViewById(R.id.tvDeliveryMode);
            tvDeliveryAddress = itemView.findViewById(R.id.tvDeliveryAddress);
            tvTipInfo = itemView.findViewById(R.id.tvTipInfo);
        }
    }
}