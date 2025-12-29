package com.B.carrasco.burgerapp.activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.models.OrderModel; // Asegúrate de tener tu modelo
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminKitchenActivity extends AppCompatActivity {

    private ListView lvKitchenOrders;
    private TextView tvKitchenStatus;
    private FirebaseFirestore db;
    private List<DocumentSnapshot> pendingOrders = new ArrayList<>();
    private KitchenAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_kitchen); // Usa un layout simple con un ListView

        db = FirebaseFirestore.getInstance();
        lvKitchenOrders = findViewById(R.id.lvKitchenOrders); // Asegúrate que tu XML tenga este ID
        tvKitchenStatus = findViewById(R.id.tvKitchenStatus); // Un TextView para decir "X Pedidos Pendientes"

        loadPendingOrders();
    }

    private void loadPendingOrders() {
        // Solo traemos lo que hay que cocinar (No entregados, no cancelados)
        db.collection("orders")
                .whereEqualTo("status", "Pendiente de Envío")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) return;
                        if (value != null) {
                            pendingOrders.clear();
                            pendingOrders.addAll(value.getDocuments());

                            // Ordenar por hora (Los más viejos primero -> Prioridad)
                            pendingOrders.sort((d1, d2) -> {
                                try {
                                    return d1.getDate("createdAt").compareTo(d2.getDate("createdAt"));
                                } catch (Exception e) { return 0; }
                            });

                            updateUI();
                        }
                    }
                });
    }

    private void updateUI() {
        if (adapter == null) {
            adapter = new KitchenAdapter();
            lvKitchenOrders.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

        if (tvKitchenStatus != null) {
            tvKitchenStatus.setText("🔥 PARRILLA ACTIVA: " + pendingOrders.size() + " PEDIDOS");
        }
    }

    // --- LA MAGIA: MOSTRAR LA COMANDA GIGANTE ---
    private void showSmartComanda(DocumentSnapshot doc) {
        OrderModel order = doc.toObject(OrderModel.class);
        if (order == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_kitchen_comanda, null);
        builder.setView(view);

        TextView tvTitle = view.findViewById(R.id.tvOrderTitle);
        TextView tvSummary = view.findViewById(R.id.tvIngredientsSummary);
        TextView tvDetail = view.findViewById(R.id.tvFullDetail);
        Button btnReady = view.findViewById(R.id.btnReady);
        Button btnClose = view.findViewById(R.id.btnClose);

        // 1. Título
        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(order.getCreatedAt());
        tvTitle.setText("PEDIDO #" + doc.getId().substring(0, 4).toUpperCase() + " (" + hora + ")");

        // 2. Detalle Completo
        tvDetail.setText(order.getOrderDescription());

        // 3. EL CEREBRO: Calcular Totales contando palabras
        String resumen = calcularResumenIngredientes(order.getOrderDescription());
        tvSummary.setText(resumen);

        AlertDialog dialog = builder.create();
        if(dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // 4. Acción: Cerrar
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // 5. Acción: Pedido Listo
        btnReady.setOnClickListener(v -> {
            marcarComoListo(doc.getId());
            dialog.dismiss();
        });

        dialog.show();
    }

    // --- EL CONTADOR DE INGREDIENTES ---
    // Esta función lee el texto y busca palabras clave para ayudarte a organizarte
    private String calcularResumenIngredientes(String descripcion) {
        if (descripcion == null) return "";
        String descLower = descripcion.toLowerCase();

        // Mapa de contadores
        Map<String, Integer> contadores = new HashMap<>();

        // Palabras clave a buscar (Agrega aquí tus ingredientes más usados)
        String[] ingredientesClave = {"queso", "tocino", "palta", "tomate", "lechuga", "cebolla", "carne", "doble"};

        // Lógica simple: Contamos cuántas veces aparece la palabra
        // OJO: Esto es una estimación basada en texto, ideal para referencia rápida
        for (String ingrediente : ingredientesClave) {
            int count = 0;
            int lastIndex = 0;
            while (lastIndex != -1) {
                lastIndex = descLower.indexOf(ingrediente, lastIndex);
                if (lastIndex != -1) {
                    count++;
                    lastIndex += ingrediente.length();
                }
            }
            if (count > 0) {
                contadores.put(ingrediente.toUpperCase(), count);
            }
        }

        // Construir el mensaje final
        StringBuilder sb = new StringBuilder();
        // Siempre calculamos carnes base (cada "Burger" cuenta como 1 carne min)
        int burgers = 0;
        int lastIndex = 0;
        while ((lastIndex = descLower.indexOf("burger", lastIndex)) != -1) { burgers++; lastIndex += 6; }

        sb.append("🍔 ").append(burgers).append("x PANES\n");
        sb.append("🥩 ").append(burgers + contadores.getOrDefault("DOBLE", 0)).append("x CARNES\n"); // Suma dobles

        for (Map.Entry<String, Integer> entry : contadores.entrySet()) {
            if (!entry.getKey().equals("CARNE") && !entry.getKey().equals("DOBLE")) {
                sb.append("• ").append(entry.getValue()).append("x ").append(entry.getKey()).append("\n");
            }
        }

        if (sb.length() == 0) return "Revisar detalle abajo...";
        return sb.toString();
    }

    private void marcarComoListo(String docId) {
        db.collection("orders").document(docId)
                .update("status", "En Reparto") // O el estado que prefieras
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "¡Pedido despachado! 🚀", Toast.LENGTH_SHORT).show());
    }

    // ADAPTADOR DE LA LISTA PRINCIPAL
    class KitchenAdapter extends ArrayAdapter<DocumentSnapshot> {
        public KitchenAdapter() {
            super(AdminKitchenActivity.this, android.R.layout.simple_list_item_1, pendingOrders);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Usamos un layout simple de Android o puedes crear uno custom si quieres
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            DocumentSnapshot doc = getItem(position);
            OrderModel order = doc.toObject(OrderModel.class);

            TextView text1 = convertView.findViewById(android.R.id.text1);
            TextView text2 = convertView.findViewById(android.R.id.text2);

            if (order != null) {
                String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(order.getCreatedAt());
                text1.setText("PEDIDO DE: " + order.getClientEmail());
                text1.setTextSize(20);
                text1.setTextColor(Color.BLACK);

                text2.setText("Hora: " + hora + " | Total: $" + (int)order.getTotalPrice());
                text2.setTextSize(16);

                // Color de fondo para resaltar
                convertView.setBackgroundColor(Color.parseColor("#E8F5E9")); // Verde clarito
            }

            // AL HACER CLICK SE ABRE LA COMANDA GIGANTE
            convertView.setOnClickListener(v -> showSmartComanda(doc));

            return convertView;
        }
    }
}