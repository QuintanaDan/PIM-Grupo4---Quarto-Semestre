package com.example.helpdeskapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.helpdeskapp.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private TextView tvBemVindo, tvTipoUsuario;
    private Button btnAbrirChamado, btnMeusChamados, btnBuscarChamado, btnTodosChamados, btnLogout;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar se está logado ANTES de definir o layout
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirecionarParaLogin();
            return;
        }

        setContentView(R.layout.activity_main);

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
        btnAbrirChamado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AbrirChamadoActivity.class);
                startActivity(intent);
            }
        });

        btnMeusChamados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MeusChamadosActivity.class);
                startActivity(intent);
            }
        });

        btnBuscarChamado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implementar busca
            }
        });

        btnTodosChamados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implementar lista admin
            }
        });

        // NOVO: Botão de logout
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarConfirmacaoLogout();
            }
        });
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
