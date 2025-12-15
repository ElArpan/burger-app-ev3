package com.B.carrasco.burgerapp.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.models.Ingredient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageIngredientsActivity extends AppCompatActivity {

    private RecyclerView rvIngredients;
    private FloatingActionButton fabAdd;
    private TextView tvEmpty;
    private FirebaseFirestore db;
    private IngredientAdapter adapter;
    private List<Ingredient> ingredientList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_ingredients);

        db = FirebaseFirestore.getInstance();
        initViews();
        loadIngredients(); // Carga en tiempo real desde Firebase
    }

    private void initViews() {
        rvIngredients = findViewById(R.id.rvIngredients);
        fabAdd = findViewById(R.id.fabAddIngredient);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvIngredients.setLayoutManager(new LinearLayoutManager(this));

        ingredientList = new ArrayList<>();
        adapter = new IngredientAdapter(ingredientList);
        rvIngredients.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showAddEditDialog(null));
    }

    private void loadIngredients() {
        db.collection("ingredients")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    ingredientList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Ingredient i = doc.toObject(Ingredient.class);
                        i.setId(doc.getId());
                        ingredientList.add(i);
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(ingredientList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void showAddEditDialog(Ingredient ing) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_ingredient, null);
        builder.setView(view);

        EditText etName = view.findViewById(R.id.etIngredientName);
        Switch swAvailable = view.findViewById(R.id.swIngredientAvailable);
        Button btnSave = view.findViewById(R.id.btnSaveIngredient);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);

        AlertDialog dialog = builder.create();
        if(dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        if (ing != null) {
            tvTitle.setText("Editar " + ing.getName());
            etName.setText(ing.getName());
            swAvailable.setChecked(ing.isAvailable());
        } else {
            tvTitle.setText("Nuevo Ingrediente");
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("Nombre requerido");
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("available", swAvailable.isChecked());

            if (ing == null) {
                db.collection("ingredients").add(data);
                Toast.makeText(this, "Ingrediente Agregado", Toast.LENGTH_SHORT).show();
            } else {
                db.collection("ingredients").document(ing.getId()).update(data);
                Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    // Adaptador Interno (Para simplicidad y evitar conflictos de archivos)
    class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {
        private List<Ingredient> list;

        public IngredientAdapter(List<Ingredient> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Usamos el layout del item que diseñamos
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingredient_admin, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Ingredient item = list.get(position);
            holder.tvName.setText(item.getName());

            if (item.isAvailable()) {
                holder.tvStatus.setText("DISPONIBLE");
                holder.tvStatus.setTextColor(getResources().getColor(R.color.success_green));
                holder.ivIcon.setImageResource(R.drawable.ic_check_circle);
            } else {
                holder.tvStatus.setText("AGOTADO");
                holder.tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                holder.ivIcon.setImageResource(R.drawable.ic_cancel);
            }

            // Click en todo el item para editar
            holder.itemView.setOnClickListener(v -> showAddEditDialog(item));

            // Click en borrar
            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(ManageIngredientsActivity.this)
                        .setTitle("Eliminar")
                        .setMessage("¿Borrar " + item.getName() + " permanentemente?")
                        .setPositiveButton("Sí", (d, w) -> db.collection("ingredients").document(item.getId()).delete())
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvStatus;
            ImageView ivIcon, btnDelete;
            public ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvIngredientName);
                tvStatus = v.findViewById(R.id.tvIngredientStatus);
                ivIcon = v.findViewById(R.id.ivStatusIcon);
                btnDelete = v.findViewById(R.id.btnDeleteIngredient);
            }
        }
    }
}