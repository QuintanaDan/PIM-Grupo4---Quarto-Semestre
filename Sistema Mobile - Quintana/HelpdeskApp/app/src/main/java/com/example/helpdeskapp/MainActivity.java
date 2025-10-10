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
import android.net.Uri;
import android.content.Context;

public class MainActivity extends AppCompatActivity {

    // ========== COMPONENTES DA INTERFACE ==========
    private TextView tvBemVindo, tvTipoUsuario;
    private Button btnLogout;

    // ========== CARDS ==========
    private CardView cardAbrirChamado, cardMeusChamados, cardBuscarChamado, cardTodosChamados, cardDiversidade;

    // ========== MANAGERS ==========
    private SessionManager sessionManager;

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

        // ========== INICIALIZAR CARDS ==========
        cardAbrirChamado = findViewById(R.id.cardAbrirChamado);
        cardMeusChamados = findViewById(R.id.cardMeusChamados);
        cardBuscarChamado = findViewById(R.id.cardBuscarChamado);
        cardTodosChamados = findViewById(R.id.cardTodosChamados);
        cardDiversidade = findViewById(R.id.cardDiversidade);
    }

    private void configurarInformacoesUsuario() {
        String nomeUsuario = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        if (!nomeUsuario.isEmpty()) {
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

    private void configurarEventos() {
        // ========== CARD ABRIR CHAMADO ==========
        cardAbrirChamado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AbrirChamadoActivity.class);
                startActivity(intent);
            }
        });

        // ========== CARD MEUS CHAMADOS ==========
        cardMeusChamados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MeusChamadosActivity.class);
                startActivity(intent);
            }
        });

        // ========== CARD BUSCAR CHAMADO ==========
        cardBuscarChamado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BuscarChamadoActivity.class);
                startActivity(intent);
            }
        });

        // ========== CARD TODOS CHAMADOS (ADMIN) ==========
        cardTodosChamados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "👨‍💼 Painel admin em desenvolvimento!", Toast.LENGTH_SHORT).show();
            }
        });

        // ========== CARD DIVERSIDADE ==========
        cardDiversidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirSiteDiversidade();
            }
        });

        // ========== BOTÃO LOGOUT (CORRIGIDO) ==========
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarConfirmacaoLogout();
            }
        });
    }

    private void abrirSiteDiversidade() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🌍 MOVER - Diversidade e Inclusão");
        builder.setMessage("A MOVER é uma organização dedicada a promover diversidade e inclusão no mercado de tecnologia.\n\n" +
                "Escolha uma opção:");

        builder.setPositiveButton("🌐 Abrir Site", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tentarAbrirLinkForcado();
            }
        });

        builder.setNeutralButton("📋 Ver Info", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mostrarInformacoesMover();
            }
        });

        builder.setNegativeButton("❌ Cancelar", null);
        builder.show();
    }

    private void tentarAbrirLinkForcado() {
        String url = "https://somosmover.org/quem-somos/";

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, "Escolha um navegador"));
            Toast.makeText(this, "🌐 Tentando abrir o site...", Toast.LENGTH_SHORT).show();

        } catch (android.content.ActivityNotFoundException e) {
            tentarMetodoAlternativo(url);
        } catch (Exception e) {
            Toast.makeText(this, "❌ Erro ao abrir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            mostrarInformacoesMover();
        }
    }

    private void tentarMetodoAlternativo(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            Toast.makeText(this, "🌐 Método alternativo: abrindo site...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            mostrarOpcoesManualParaAbrirSite(url);
        }
    }

    private void mostrarOpcoesManualParaAbrirSite(String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📱 Abrir Site Manualmente");
        builder.setMessage(
                "⚠️ O sistema não conseguiu abrir o link automaticamente.\n\n" +
                        "📋 OPÇÕES:\n\n" +
                        "1️⃣ Copie o link e cole no navegador\n" +
                        "2️⃣ Abra o Chrome e digite: somosmover.org\n" +
                        "3️⃣ Veja as informações da MOVER no app\n\n" +
                        "🔗 LINK: " + url
        );

        builder.setPositiveButton("📋 Copiar Link", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                copiarLinkParaClipboard();
            }
        });

        builder.setNeutralButton("📱 Ver no App", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mostrarInformacoesMover();
            }
        });

        builder.setNegativeButton("❌ Fechar", null);
        builder.show();
    }

    private void copiarLinkParaClipboard() {
        try {
            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip =
                    android.content.ClipData.newPlainText("MOVER Site", "https://somosmover.org/quem-somos/");
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "📋 Link copiado! Abra o navegador e cole o link.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "❌ Erro ao copiar link", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarInformacoesMover() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🌍 MOVER - Diversidade e Inclusão");
        builder.setMessage(
                "📋 SOBRE A MOVER:\n\n" +
                        "🎯 MISSÃO: Promover diversidade e inclusão no mercado de tecnologia\n\n" +
                        "👥 FOCO: Pessoas negras na tecnologia\n\n" +
                        "🚀 AÇÕES:\n" +
                        "• Programas de capacitação\n" +
                        "• Mentoria profissional\n" +
                        "• Conexão com oportunidades\n" +
                        "• Networking inclusivo\n\n" +
                        "🌐 SITE: somosmover.org\n" +
                        "📧 CONTATO: contato@somosmover.org\n" +
                        "📱 Instagram: @somosmover\n\n" +
                        "💡 Este projeto apoia a diversidade racial na tecnologia!"
        );

        builder.setPositiveButton("📋 Copiar Site", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                copiarLinkParaClipboard();
            }
        });

        builder.setNegativeButton("✅ Entendi", null);
        builder.show();
    }

    private void mostrarConfirmacaoLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Saída")
                .setMessage("Deseja realmente sair do aplicativo?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fazerLogout();
                    }
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void fazerLogout() {
        sessionManager.logout();
        redirecionarParaLogin();
    }
}