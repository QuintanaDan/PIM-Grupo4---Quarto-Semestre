package com.example.helpdeskapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.helpdeskapp.dao.AvaliacaoDAO;
import com.example.helpdeskapp.models.Avaliacao;
import com.example.helpdeskapp.utils.AuditoriaHelper;
import com.example.helpdeskapp.utils.SessionManager;
import com.example.helpdeskapp.utils.AuditoriaHelper;
import com.example.helpdeskapp.utils.ThemeManager;

public class AvaliarChamadoActivity extends AppCompatActivity {
    private static final String TAG = "AvaliarChamado";

    private TextView tvTituloChamado, tvDescricaoAvaliacao;
    private RatingBar ratingBar;
    private EditText etComentarioAvaliacao;
    private Button btnEnviarAvaliacao, btnCancelarAvaliacao;

    private long chamadoId;
    private String chamadoTitulo;
    private AvaliacaoDAO avaliacaoDAO;
    private SessionManager sessionManager;
    private int notaSelecionada = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ThemeManager(this).applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avaliar_chamado);

        sessionManager = new SessionManager(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("⭐ Avaliar Atendimento");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inicializarComponentes();
        receberDados();
        configurarEventos();
    }

    private void inicializarComponentes() {
        tvTituloChamado = findViewById(R.id.tvTituloChamado);
        tvDescricaoAvaliacao = findViewById(R.id.tvDescricaoAvaliacao);
        ratingBar = findViewById(R.id.ratingBar);
        etComentarioAvaliacao = findViewById(R.id.etComentarioAvaliacao);
        btnEnviarAvaliacao = findViewById(R.id.btnEnviarAvaliacao);
        btnCancelarAvaliacao = findViewById(R.id.btnCancelarAvaliacao);

        avaliacaoDAO = new AvaliacaoDAO(this);
    }

    private void receberDados() {
        chamadoId = getIntent().getLongExtra("chamado_id", -1);
        chamadoTitulo = getIntent().getStringExtra("chamado_titulo");

        if (chamadoId <= 0) {
            Toast.makeText(this, "❌ Erro: ID do chamado inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Verificar se já foi avaliado
        if (avaliacaoDAO.chamadoJaAvaliado(chamadoId)) {
            Toast.makeText(this, "⚠️ Este chamado já foi avaliado!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvTituloChamado.setText("Chamado: " + (chamadoTitulo != null ? chamadoTitulo : "Sem título"));
    }

    private void configurarEventos() {
        // Atualizar texto conforme a nota
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                int nota = (int) rating;
                String texto = "";

                switch (nota) {
                    case 1:
                        texto = "😞 Muito Ruim - Sentimos muito pela experiência";
                        break;
                    case 2:
                        texto = "😕 Ruim - Vamos melhorar nosso atendimento";
                        break;
                    case 3:
                        texto = "😐 Regular - Podemos fazer melhor";
                        break;
                    case 4:
                        texto = "😊 Bom - Obrigado pelo feedback!";
                        break;
                    case 5:
                        texto = "🎉 Excelente - Ficamos muito felizes!";
                        break;
                }

                tvDescricaoAvaliacao.setText(texto);
            }
        });

        btnEnviarAvaliacao.setOnClickListener(v -> enviarAvaliacao());
        btnCancelarAvaliacao.setOnClickListener(v -> finish());
    }

    private void enviarAvaliacao() {
        // Validar avaliação
        notaSelecionada = (int) ratingBar.getRating();
        float rating = ratingBar.getRating();

        if (rating == 0) {
            Toast.makeText(this, "⭐ Por favor, selecione uma nota", Toast.LENGTH_SHORT).show();
            return;
        }

        int nota = (int) rating;
        String comentario = etComentarioAvaliacao.getText().toString().trim();

        try {
            Avaliacao avaliacao = new Avaliacao(chamadoId, nota, comentario);
            long resultado = avaliacaoDAO.inserirAvaliacao(avaliacao);

            if (resultado > 0) {
                Log.d(TAG, "✅ Avaliação salva com sucesso: " + resultado);
                Toast.makeText(this, "✅ Obrigado pela sua avaliação!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "❌ Erro ao salvar avaliação", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao enviar avaliação: ", e);
            Toast.makeText(this, "❌ Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}