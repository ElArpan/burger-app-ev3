package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.B.carrasco.burgerapp.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class PriceGuideActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout layoutDots;
    private MaterialButton btnFinish;
    private TextView[] dots;
    private GuideAdapter guideAdapter;
    private List<GuideItem> guideItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_price_guide);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_guide_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupContent();
        setupViewPager();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPagerGuide);
        layoutDots = findViewById(R.id.layoutDots);
        btnFinish = findViewById(R.id.btnFinishGuide);
        ImageButton btnClose = findViewById(R.id.btnCloseGuide);

        btnClose.setOnClickListener(v -> finish());

        btnFinish.setOnClickListener(v -> {
            startActivity(new Intent(PriceGuideActivity.this, BuildBurgerActivity.class));
            finish();
        });
    }

    private void setupContent() {
        guideItems = new ArrayList<>();

        // 1. MANO (img_guide_start): El inicio, tú tienes el poder.
        guideItems.add(new GuideItem(
                "1. Tú eres el Chef",
                "Toca 'Pedir mi Regalona' para empezar. Tú eliges, nosotros cocinamos.",
                R.drawable.img_guide_start
        ));

        // 2. PLATO (img_guide_food): La calidad de la comida.
        guideItems.add(new GuideItem(
                "2. Calidad Garantizada",
                "El precio base incluye nuestro pan artesanal y carne casera. ¡Las verduras y salsas son GRATIS!",
                R.drawable.img_guide_food
        ));

        // 3. DINERO (img_guide_money): Explicación de precios.
        guideItems.add(new GuideItem(
                "3. Precios Claros",
                "Sin sorpresas. Solo pagas extra si agregas ingredientes premium (tocino, queso extra, etc).",
                R.drawable.img_guide_money
        ));

        // 4. RELOJ (img_guide_time): Rapidez.
        guideItems.add(new GuideItem(
                "4. Sin Esperas",
                "Recibimos tu pedido al instante. Te avisaremos el tiempo exacto de entrega.",
                R.drawable.img_guide_time
        ));

        // 5. BOLSA (img_guide_bag): Entrega y final.
        guideItems.add(new GuideItem(
                "5. ¡A Disfrutar!",
                "Recibe tu pedido calientito. ¿Te gustó? Búscala en tu Historial para repetir.",
                R.drawable.img_guide_bag
        ));
    }

    private void setupViewPager() {
        guideAdapter = new GuideAdapter(guideItems);
        viewPager.setAdapter(guideAdapter);
        addBottomDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                addBottomDots(position);
                if (position == guideItems.size() - 1) {
                    btnFinish.setVisibility(View.VISIBLE);
                    layoutDots.setVisibility(View.GONE);
                } else {
                    btnFinish.setVisibility(View.GONE);
                    layoutDots.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[guideItems.size()];
        layoutDots.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;", Html.FROM_HTML_MODE_LEGACY));
            dots[i].setTextSize(40);
            dots[i].setTextColor(getResources().getColor(R.color.gold_light));
            layoutDots.addView(dots[i]);
        }

        if (dots.length > 0) {
            dots[currentPage].setTextColor(getResources().getColor(R.color.gold_500));
        }
    }
}