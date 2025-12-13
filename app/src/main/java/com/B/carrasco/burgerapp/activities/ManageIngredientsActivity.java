package com.B.carrasco.burgerapp.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.adapters.IngredientsAdapter;
import com.B.carrasco.burgerapp.database.DatabaseHelper;
import com.B.carrasco.burgerapp.models.Ingredient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ManageIngredientsActivity extends AppCompatActivity implements IngredientsAdapter.OnIngredientActionListener {

    private RecyclerView rvIngredients;
    private FloatingActionButton fabAdd;
    private TextView tvEmpty;
    private DatabaseHelper dbHelper;
    private IngredientsAdapter adapter;
    private List<Ingredient> ingredientList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_ingredients);

        dbHelper = new DatabaseHelper(this);
        initViews();
        loadIngredients();
        setupListeners();
    }

    private void initViews() {
        rvIngredients = findViewById(R.id.rvIngredients);
        fabAdd = findViewById(R.id.fabAddIngredient);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadIngredients() {
        ingredientList = dbHelper.getAllIngredients();
        if (ingredientList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvIngredients.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvIngredients.setVisibility(View.VISIBLE);
            adapter = new IngredientsAdapter(ingredientList, this);
            rvIngredients.setAdapter(adapter);
        }
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> showAddEditDialog(null));
    }

    // Muestra diálogo para añadir o editar. Si ing == null -> add, else -> edit
    private void showAddEditDialog(Ingredient ing) {
        boolean isEdit = (ing != null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isEdit ? "Editar Ingrediente" : "Nuevo Ingrediente");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_ingredient, null);
        EditText etName = view.findViewById(R.id.etIngredientName);
        EditText etPrice = view.findViewById(R.id.etIngredientPrice);
        EditText etCategory = view.findViewById(R.id.etIngredientCategory);
        Switch swAvailable = view.findViewById(R.id.swIngredientAvailable);

        if (isEdit) {
            etName.setText(ing.getName());
            etPrice.setText(String.valueOf((int) ing.getPrice()));
            etCategory.setText(ing.getCategory());
            swAvailable.setChecked(ing.isAvailable());
        }

        builder.setView(view);
        builder.setPositiveButton(isEdit ? "Guardar" : "Agregar", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String priceS = etPrice.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            boolean available = swAvailable.isChecked();

            if (name.isEmpty() || priceS.isEmpty() || category.isEmpty()) {
                Toast.makeText(ManageIngredientsActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceS);
            } catch (NumberFormatException e) {
                Toast.makeText(ManageIngredientsActivity.this, "Precio inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEdit) {
                ing.setName(name);
                ing.setPrice(price);
                ing.setCategory(category);
                ing.setAvailable(available);
                int updated = dbHelper.updateIngredient(ing);
                if (updated > 0) Toast.makeText(this, "Ingrediente actualizado", Toast.LENGTH_SHORT).show();
            } else {
                Ingredient newIng = new Ingredient();
                newIng.setName(name);
                newIng.setPrice(price);
                newIng.setCategory(category);
                newIng.setAvailable(available);
                long id = dbHelper.insertIngredient(newIng);
                if (id != -1) Toast.makeText(this, "Ingrediente agregado", Toast.LENGTH_SHORT).show();
            }
            // recargar lista
            loadIngredients();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // Adapter callbacks
    @Override
    public void onEditClicked(@NonNull Ingredient ingredient) {
        showAddEditDialog(ingredient);
    }

    @Override
    public void onDeleteClicked(@NonNull Ingredient ingredient) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Eliminar ingrediente \"" + ingredient.getName() + "\"?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    int deleted = dbHelper.deleteIngredient(ingredient.getId());
                    if (deleted > 0) {
                        Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show();
                        loadIngredients();
                    } else {
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}