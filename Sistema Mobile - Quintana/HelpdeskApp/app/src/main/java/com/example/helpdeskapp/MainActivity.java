package com.example.helpdeskapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.helpdeskapp.utils.SessionManager;
import android.net.Uri;
import android.content.Context;


public class MainActivity extends AppCompatActivity {

    // ========== COMPONENTES DA INTERFACE ==========
    private TextView tvBemVindo, tvTipoUsuario;
    private Button btnAbrirChamado, btnMeusChamados, btnBuscarChamado, btnTodosChamados, btnLogout, btnDiversidade;

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
        btnAbrirChamado = findViewById(R.id.btnAbrirChamado);
        btnMeusChamados = findViewById(R.id.btnMeusChamados);
        btnBuscarChamado = findViewById(R.id.btnBuscarChamado);
        btnTodosChamados = findViewById(R.id.btnTodosChamados);
        btnDiversidade = findViewById(R.id.btnDiversidade);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void configurarInformacoesUsuario() {
        String nomeUsuario = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        if (!nomeUsuario.isEmpty()) {
            tvBemVindo.setText("Bem-vindo, " + nomeUsuario + "!");
        } else {
            tvBemVindo.setText("Bem-vindo, " + email + "!");
        }

        tvTipoUsuario.setText("Tipo: " + sessionManager.getUserTypeText());
    }

    private void configurarVisibilidadeBotoes() {
        if (sessionManager.isAdmin()) {
            btnTodosChamados.setVisibility(View.VISIBLE);
        } else {
            btnTodosChamados.setVisibility(View.GONE);
        }
    }

    private void configurarEventos() {
        // ========== ABRIR CHAMADO ==========
        btnAbrirChamado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AbrirChamadoActivity.class);
                startActivity(intent);
            }
        });

        // ========== MEUS CHAMADOS ==========
        btnMeusChamados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MeusChamadosActivity.class);
                startActivity(intent);
            }
        });

        // ========== BUSCAR CHAMADO ==========
        btnBuscarChamado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "🔍 Busca em desenvolvimento!", Toast.LENGTH_SHORT).show();
            }
        });

        // ========== TODOS CHAMADOS (ADMIN) ==========
        btnTodosChamados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "👨‍💼 Painel admin em desenvolvimento!", Toast.LENGTH_SHORT).show();
            }
        });

        // ========== DIVERSIDADE ==========
        btnDiversidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarInformacoesMover();
            }
        });

        // ========== LOGOUT ==========
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarConfirmacaoLogout();
            }
        });
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
                        "📧 CONTATO: contato@somosmover.org\n\n" +
                        "💡 Este projeto apoia a diversidade racial na tecnologia!"
        );

        builder.setPositiveButton("✅ Entendi", null);
        builder.setNeutralButton("📋 Copiar Site", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Copiar URL para clipboard
                android.content.ClipboardManager clipboard =
                        (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip =
                        android.content.ClipData.newPlainText("MOVER Site", "https://somosmover.org/quem-somos/");
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "📋 Link copiado para área de transferência!", Toast.LENGTH_SHORT).show();
            }
        });

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

} // ← CHAVE QUE FECHA A CLASSE
