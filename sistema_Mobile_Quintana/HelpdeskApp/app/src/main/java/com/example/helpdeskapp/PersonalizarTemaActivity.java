package com.example.helpdeskapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.example.helpdeskapp.utils.ThemeManager;

public class PersonalizarTemaActivity extends AppCompatActivity {
    private ThemeManager themeManager;

    private CardView cardAmanhecer, cardAnoitecer, cardAncestral;
    private RadioButton rbAmanhecer, rbAnoitecer, rbAncestral;
    private Button btnAplicar, btnCancelar;

    private int temaTemporario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        themeManager = new ThemeManager(this);
        themeManager.applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personalizar_tema);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Personalizar Tema");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inicializarComponentes();
        configurarTemaAtual();
        configurarEventos();
    }

    private void inicializarComponentes() {
        cardAmanhecer = findViewById(R.id.cardAmanhecer);
        cardAnoitecer = findViewById(R.id.cardAnoitecer);
        cardAncestral = findViewById(R.id.cardAncestral);

        rbAmanhecer = findViewById(R.id.rbAmanhecer);
        rbAnoitecer = findViewById(R.id.rbAnoitecer);
        rbAncestral = findViewById(R.id.rbAncestral);

        btnAplicar = findViewById(R.id.btnAplicar);
        btnCancelar = findViewById(R.id.btnCancelar);

        temaTemporario = themeManager.getCurrentTheme();
    }

    private void configurarTemaAtual() {
        int temaAtual = themeManager.getCurrentTheme();

        switch (temaAtual) {
            case ThemeManager.THEME_AMANHECER:
                rbAmanhecer.setChecked(true);
                break;
            case ThemeManager.THEME_ANOITECER:
                rbAnoitecer.setChecked(true);
                break;
            case ThemeManager.THEME_ANCESTRAL:
                rbAncestral.setChecked(true);
                break;
        }
    }

    private void configurarEventos() {
        // Card Amanhecer
        cardAmanhecer.setOnClickListener(v -> {
            rbAmanhecer.setChecked(true);
            rbAnoitecer.setChecked(false);
            rbAncestral.setChecked(false);
            temaTemporario = ThemeManager.THEME_AMANHECER;
        });

        // Card Anoitecer
        cardAnoitecer.setOnClickListener(v -> {
            rbAmanhecer.setChecked(false);
            rbAnoitecer.setChecked(true);
            rbAncestral.setChecked(false);
            temaTemporario = ThemeManager.THEME_ANOITECER;
        });

        // Card Ancestral
        cardAncestral.setOnClickListener(v -> {
            rbAmanhecer.setChecked(false);
            rbAnoitecer.setChecked(false);
            rbAncestral.setChecked(true);
            temaTemporario = ThemeManager.THEME_ANCESTRAL;
        });

        // RadioButtons
        rbAmanhecer.setOnClickListener(v -> {
            rbAnoitecer.setChecked(false);
            rbAncestral.setChecked(false);
            temaTemporario = ThemeManager.THEME_AMANHECER;
        });

        rbAnoitecer.setOnClickListener(v -> {
            rbAmanhecer.setChecked(false);
            rbAncestral.setChecked(false);
            temaTemporario = ThemeManager.THEME_ANOITECER;
        });

        rbAncestral.setOnClickListener(v -> {
            rbAmanhecer.setChecked(false);
            rbAnoitecer.setChecked(false);
            temaTemporario = ThemeManager.THEME_ANCESTRAL;
        });

        // Botão Aplicar
        btnAplicar.setOnClickListener(v -> aplicarTema());

        // Botão Cancelar
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void aplicarTema() {
        themeManager.setTheme(temaTemporario);

        String nomeTema = themeManager.getThemeName(temaTemporario);
        String emoji = themeManager.getThemeEmoji(temaTemporario);

        Toast.makeText(this,
                emoji + " Tema " + nomeTema + " aplicado!",
                Toast.LENGTH_SHORT).show();

        // Recriar a activity para aplicar o tema
        recreate();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}