package com.example.helpdeskapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.helpdeskapp.utils.ThemeManager;

public class DiversidadeActivity extends AppCompatActivity {

    private ThemeManager themeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        themeManager = new ThemeManager(this);
        themeManager.applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diversidade);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("✊🏿 Consciência e Diversidade");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        configurarBotoes();
    }

    private void configurarBotoes() {
        Button btnMover = findViewById(R.id.btnConhecerMover);
        Button btnTemaAncestral = findViewById(R.id.btnTemaAncestral);

        btnMover.setOnClickListener(v -> abrirSiteMover());

        btnTemaAncestral.setOnClickListener(v -> {
            Intent intent = new Intent(this, PersonalizarTemaActivity.class);
            startActivity(intent);
        });
    }

    private void abrirSiteMover() {
        String url = "https://somosmover.org/quem-somos/";
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir o navegador", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}