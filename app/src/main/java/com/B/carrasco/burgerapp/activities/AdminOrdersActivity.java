package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.B.carrasco.burgerapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
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
        Query query = db.collection("orders").orderBy("createdAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<OrderModel> options = new FirestoreRecyclerOptions.Builder<OrderModel>()
                .setQuery(query, OrderModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<OrderModel, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, int position, @NonNull OrderModel model) {
                try {
                    String docId = getSnapshots().getSnapshot(position).getId();

                    holder.tvClientName.setText(model.getClientEmail());
                    holder.tvOrderDescription.setText(model.getOrderDescription());
                    holder.tvTotal.setText("$" + (int)model.getTotalPrice());

                    // Fecha
                    if (model.getCreatedAt() != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault());
                        holder.tvOrderTime.setText(sdf.format(model.getCreatedAt()));
                    } else {
                        holder.tvOrderTime.setText("--:--");
                    }

                    // ENTREGA Y PROPINA
                    String mode = model.getDeliveryMode();
                    if (mode != null && !mode.isEmpty()) {
                        holder.tvDeliveryMode.setText(mode.toUpperCase());
                    } else {
                        holder.tvDeliveryMode.setText("ENTREGA");
                    }
                    holder.tvDeliveryAddress.setText(model.getDeliveryAddress());
                    holder.tvTipInfo.setText("🎁 Propina: $" + (int)model.getTipAmount());

                    // Pago
                    String method = model.getPaymentMethod();
                    if (method.contains("Efectivo")) {
                        holder.tvPaymentMethod.setText("💵 EFECTIVO (Paga con $" + (int)model.getPayWith() + ")");
                        if (model.getChangeDue() > 0) {
                            holder.tvPaymentMethod.append("\nVuelto: $" + (int)model.getChangeDue());
                        }
                        holder.tvPaymentMethod.setTextColor(Color.parseColor("#388E3C"));
                    } else {
                        holder.tvPaymentMethod.setText("📱 TRANSFERENCIA");
                        holder.tvPaymentMethod.setTextColor(Color.parseColor("#1976D2"));
                    }

                    // Estados
                    String status = model.getStatus();
                    holder.tvOrderStatus.setText(status.toUpperCase());

                    if (status.contains("Pendiente")) {
                        holder.headerContainer.setBackgroundColor(Color.parseColor("#FFC107"));
                        holder.btnNextStatus.setText("MARCAR EN COCINA");
                        holder.btnNextStatus.setVisibility(View.VISIBLE);
                    } else if (status.contains("Cocina")) {
                        holder.headerContainer.setBackgroundColor(Color.parseColor("#FF9800"));
                        holder.btnNextStatus.setText("MARCAR LISTO");
                        holder.btnNextStatus.setVisibility(View.VISIBLE);
                    } else if (status.contains("Listo") || status.contains("Entregado")) {
                        holder.headerContainer.setBackgroundColor(Color.parseColor("#4CAF50"));
                        holder.btnNextStatus.setText("ARCHIVAR");
                        holder.btnNextStatus.setVisibility(View.VISIBLE);
                    } else {
                        holder.headerContainer.setBackgroundColor(Color.GRAY);
                        holder.btnNextStatus.setVisibility(View.GONE);
                    }

                    holder.btnNextStatus.setOnClickListener(v -> {
                        String nextStatus = "Archivado";
                        if (status.contains("Pendiente")) nextStatus = "En Cocina";
                        else if (status.contains("Cocina")) nextStatus = "Listo";
                        else if (status.contains("Listo")) nextStatus = "Entregado";

                        db.collection("orders").document(docId).update("status", nextStatus);
                    });

                } catch (Exception e) {
                    Log.e("AdminOrders", "Error pintando: " + e.getMessage());
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

    // --- MODELO ROBUSTO (USAR DOUBLE) ---
    public static class OrderModel {
        private String clientEmail;
        private String orderDescription;
        private Double totalPrice;
        private String paymentMethod;
        private String status;
        private Date createdAt;
        private Double payWith;
        private Double changeDue;

        // Campos de Entrega
        private String deliveryMode;
        private String deliveryAddress;
        private Double tipAmount;

        public OrderModel() {}

        public String getClientEmail() { return clientEmail != null ? clientEmail : "Sin email"; }
        public String getOrderDescription() { return orderDescription != null ? orderDescription : ""; }
        public double getTotalPrice() { return totalPrice != null ? totalPrice : 0.0; }
        public String getPaymentMethod() { return paymentMethod != null ? paymentMethod : "Desconocido"; }
        public String getStatus() { return status != null ? status : "Pendiente"; }
        public Date getCreatedAt() { return createdAt; }
        public double getPayWith() { return payWith != null ? payWith : 0.0; }
        public double getChangeDue() { return changeDue != null ? changeDue : 0.0; }

        public String getDeliveryMode() { return deliveryMode != null ? deliveryMode : ""; }
        public String getDeliveryAddress() { return deliveryAddress != null ? deliveryAddress : ""; }
        public double getTipAmount() { return tipAmount != null ? tipAmount : 0.0; }
    }

    // --- VIEWHOLDER ---
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

            // Nuevos campos
            tvDeliveryMode = itemView.findViewById(R.id.tvDeliveryMode);
            tvDeliveryAddress = itemView.findViewById(R.id.tvDeliveryAddress);
            tvTipInfo = itemView.findViewById(R.id.tvTipInfo);
        }
    }
}