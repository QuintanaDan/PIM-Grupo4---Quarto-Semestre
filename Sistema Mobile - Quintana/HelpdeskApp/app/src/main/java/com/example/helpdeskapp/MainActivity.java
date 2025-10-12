package com.example.helpdeskapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import com.example.helpdeskapp.utils.SessionManager;
import com.example.helpdeskapp.database.DatabaseHelper; // <-- ADICIONADO: Import do DatabaseHelper
import android.net.Uri;
import android.content.Context;

public class MainActivity extends AppCompatActivity {

    // ========== COMPONENTES DA INTERFACE ==========
    private TextView tvBemVindo, tvTipoUsuario;
    private TextView tvChamadosAbertos, tvChamadosProgresso; // <-- ADICIONADO: TextViews para estatísticas
    private Button btnLogout;

    // ========== CARDS ==========
    private CardView cardAbrirChamado, cardMeusChamados, cardBuscarChamado, cardTodosChamados, cardDiversidade;

    // ========== MANAGERS ==========
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper; // <-- ADICIONADO: Referência ao DatabaseHelper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ========== VERIFICAR SESSÃO ==========
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirecionarParaLogin();
            return;
        }

        // ========== CONFIGURAR LAYOUT ==========
        setContentView(R.layout.activity_main);

        // ========== INICIALIZAR COMPONENTES ==========
        inicializarComponentes();
        configurarInformacoesUsuario();
        configurarVisibilidadeBotoes();
        configurarEventos();
        configurarCardDashboard();
        configurarCardGerenciarTags();

        // ========== ATUALIZAR DADOS DINÂMICOS ==========
        updateStatusCards(); // <-- ADICIONADO: Chamada para atualizar os cards
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Garante que os dados sejam atualizados sempre que o usuário voltar para esta tela
        updateStatusCards(); // <-- ADICIONADO: Chamada no onResume
    }

    private void redirecionarParaLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void inicializarComponentes() {
        tvBemVindo = findViewById(R.id.tvBemVindo);
        tvTipoUsuario = findViewById(R.id.tvTipoUsuario);
        btnLogout = findViewById(R.id.btnLogout);

        // ADICIONADO: Inicialização dos TextViews de estatísticas e do DatabaseHelper
        tvChamadosAbertos = findViewById(R.id.tvChamadosAbertos);
        tvChamadosProgresso = findViewById(R.id.tvChamadosProgresso);
        dbHelper = new DatabaseHelper(this);

        // ========== INICIALIZAR CARDS ==========
        cardAbrirChamado = findViewById(R.id.cardAbrirChamado);
        cardMeusChamados = findViewById(R.id.cardMeusChamados);
        cardBuscarChamado = findViewById(R.id.cardBuscarChamado);
        cardTodosChamados = findViewById(R.id.cardTodosChamados);
        cardDiversidade = findViewById(R.id.cardDiversidade);
    }

    /**
     * ADICIONADO: Método para buscar contagens no banco de dados e atualizar a UI.
     */
    private void updateStatusCards() {
        // Obter os números reais do banco de dados
        int abertos = dbHelper.countChamadosByStatus("Aberto");
        int emAndamento = dbHelper.countChamadosByStatus("Em Andamento"); // Verifique se o status é "Em Andamento" ou "Em progresso"

        // Atualizar os TextViews com os valores do banco
        tvChamadosAbertos.setText(String.valueOf(abertos));
        tvChamadosProgresso.setText(String.valueOf(emAndamento));
    }


    private void configurarInformacoesUsuario() {
        String nomeUsuario = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        if (nomeUsuario != null && !nomeUsuario.isEmpty()) {
            tvBemVindo.setText("Olá, " + nomeUsuario + "! 👋");
        } else {
            tvBemVindo.setText("Olá, " + email + "! 👋");
        }

        tvTipoUsuario.setText(sessionManager.getUserTypeText());
    }

    private void configurarVisibilidadeBotoes() {
        if (sessionManager.isAdmin()) {
            cardTodosChamados.setVisibility(View.VISIBLE);
        } else {
            cardTodosChamados.setVisibility(View.GONE);
        }
    }

    private void configurarCardDashboard() {
        CardView cardDashboard = findViewById(R.id.cardDashboard);
        if (sessionManager.isAdmin() && cardDashboard != null) {
            cardDashboard.setVisibility(View.VISIBLE);
            cardDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(intent);
            });
        } else if (cardDashboard != null) {
            cardDashboard.setVisibility(View.GONE);
        }
    }

    private void configurarCardGerenciarTags() {
        CardView cardGerenciarTags = findViewById(R.id.cardGerenciarTags);
        if (sessionManager.isAdmin() && cardGerenciarTags != null) {
            cardGerenciarTags.setVisibility(View.VISIBLE);
            cardGerenciarTags.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, GerenciarTagsActivity.class);
                startActivity(intent);
            });
        } else if (cardGerenciarTags != null) {
            cardGerenciarTags.setVisibility(View.GONE);
        }
    }

    private void configurarEventos() {
        // ========== CARD ABRIR CHAMADO ==========
        cardAbrirChamado.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AbrirChamadoActivity.class);
            startActivity(intent);
        });

        // ========== CARD MEUS CHAMADOS ==========
        cardMeusChamados.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MeusChamadosActivity.class);
            startActivity(intent);
        });

        // ========== CARD BUSCAR CHAMADO ==========
        cardBuscarChamado.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BuscarChamadoActivity.class);
            startActivity(intent);
        });

        // ========== CARD TODOS CHAMADOS (ADMIN) ==========
        cardTodosChamados.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminPanelActivity.class);
            startActivity(intent);
        });

        // ========== CARD DIVERSIDADE ==========
        cardDiversidade.setOnClickListener(v -> abrirSiteDiversidade());

        // ========== BOTÃO LOGOUT ==========
        btnLogout.setOnClickListener(v -> mostrarConfirmacaoLogout());
    }

    private void abrirSiteDiversidade() {
        new AlertDialog.Builder(this)
                .setTitle("🌍 MOVER - Diversidade e Inclusão")
                .setMessage("A MOVER é uma organização dedicada a promover diversidade e inclusão no mercado de tecnologia.\n\nDeseja saber mais?")
                .setPositiveButton("🌐 Abrir Site", (dialog, which) -> tentarAbrirLinkForcado())
                .setNeutralButton("📋 Ver no App", (dialog, which) -> mostrarInformacoesMover())
                .setNegativeButton("❌ Cancelar", null)
                .show();
    }

    private void tentarAbrirLinkForcado() {
        String url = "https://somosmover.org/quem-somos/";
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível abrir o navegador. Tente copiar o link.", Toast.LENGTH_LONG).show();
            mostrarOpcoesManualParaAbrirSite(url);
        }
    }

    private void mostrarOpcoesManualParaAbrirSite(String url) {
        new AlertDialog.Builder(this)
                .setTitle("📱 Abrir Site Manualmente")
                .setMessage("Não foi possível abrir o link automaticamente.\n\n🔗 LINK: " + url)
                .setPositiveButton("📋 Copiar Link", (dialog, which) -> copiarLinkParaClipboard(url))
                .setNeutralButton("📱 Ver no App", (dialog, which) -> mostrarInformacoesMover())
                .setNegativeButton("❌ Fechar", null)
                .show();
    }

    private void copiarLinkParaClipboard(String url) {
        try {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("MOVER Site", url);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "📋 Link copiado! Abra o navegador e cole.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "❌ Erro ao copiar o link.", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarInformacoesMover() {
        new AlertDialog.Builder(this)
                .setTitle("🌍 MOVER - Diversidade e Inclusão")
                .setMessage(
                        "🎯 MISSÃO: Promover diversidade e inclusão no mercado de tecnologia, com foco em pessoas negras.\n\n" +
                                "🚀 AÇÕES: Programas de capacitação, mentoria e conexão com oportunidades.\n\n" +
                                "💡 Este projeto apoia a diversidade racial na tecnologia!"
                )
                .setPositiveButton("📋 Copiar Site", (dialog, which) -> copiarLinkParaClipboard("https://somosmover.org/"))
                .setNegativeButton("✅ Entendi", null)
                .show();
    }

    private void mostrarConfirmacaoLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Saída")
                .setMessage("Deseja realmente sair do aplicativo?")
                .setPositiveButton("Sim", (dialog, which) -> fazerLogout())
                .setNegativeButton("Não", null)
                .show();
    }

    private void fazerLogout() {
        sessionManager.logout();
        redirecionarParaLogin();
    }
}