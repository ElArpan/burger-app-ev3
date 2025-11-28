package com.B.carrasco.burgerapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.database.DatabaseHelper;
import com.B.carrasco.burgerapp.models.Ingredient;
import java.util.*;

public class BuildBurgerActivity extends AppCompatActivity {
    private ListView lvIngredients;
    private TextView tvSelectedCount, tvTotalPrice;
    private Button btnCreateBurger;
    private DatabaseHelper dbHelper;
    private List<Ingredient> allIngredients;
    private List<Ingredient> selectedIngredients;
    private double totalPrice = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_burger);

        dbHelper = new DatabaseHelper(this);
        selectedIngredients = new ArrayList<>();
        initViews();
        loadIngredients();
        setupClickListeners();
    }

    private void initViews() {
        lvIngredients = findViewById(R.id.lvIngredients);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnCreateBurger = findViewById(R.id.btnCreateBurger);
    }

    private void loadIngredients() {
        // Por ahora usamos datos mock, luego conectamos con DB
        allIngredients = Arrays.asList(
                new Ingredient("Pan clásico", 1.0, "pan"),
                new Ingredient("Pan integral", 1.5, "pan"),
                new Ingredient("Carne de res", 3.0, "carne"),
                new Ingredient("Pollo", 2.5, "carne"),
                new Ingredient("Queso cheddar", 1.0, "queso"),
                new Ingredient("Lechuga", 0.5, "vegetales"),
                new Ingredient("Tomate", 0.5, "vegetales"),
                new Ingredient("Tocino", 1.5, "extras")
        );

        ArrayAdapter<Ingredient> adapter = new ArrayAdapter<Ingredient>(this,
                android.R.layout.simple_list_item_multiple_choice, allIngredients) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Ingredient ingredient = getItem(position);
                ((TextView) view).setText(ingredient.getName() + " - $" + ingredient.getPrice());
                return view;
            }
        };

        lvIngredients.setAdapter(adapter);
        lvIngredients.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    private void setupClickListeners() {
        lvIngredients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Ingredient ingredient = allIngredients.get(position);
                CheckedTextView checkedTextView = (CheckedTextView) view;

                if (checkedTextView.isChecked()) {
                    if (selectedIngredients.size() < 5) {
                        selectedIngredients.add(ingredient);
                        totalPrice += ingredient.getPrice();
                    } else {
                        checkedTextView.setChecked(false);
                        Toast.makeText(BuildBurgerActivity.this,
                                "Máximo 5 ingredientes", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    selectedIngredients.remove(ingredient);
                    totalPrice -= ingredient.getPrice();
                }

                updateUI();
            }
        });

        btnCreateBurger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedIngredients.size() >= 3) {
                    // Crear pedido
                    Toast.makeText(BuildBurgerActivity.this,
                            "Hamburguesa creada! Total: $" + totalPrice, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(BuildBurgerActivity.this,
                            "Selecciona al menos 3 ingredientes", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI() {
        tvSelectedCount.setText("Ingredientes: " + selectedIngredients.size() + "/5");
        tvTotalPrice.setText("Total: $" + totalPrice);
        btnCreateBurger.setEnabled(selectedIngredients.size() >= 3);
    }
}