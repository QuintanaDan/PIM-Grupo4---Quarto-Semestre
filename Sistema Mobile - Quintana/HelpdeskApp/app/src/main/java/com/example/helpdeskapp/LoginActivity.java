package com.example.helpdeskapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.helpdeskapp.dao.UsuarioDAO;
import com.example.helpdeskapp.models.Usuario;
import com.example.helpdeskapp.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etSenha;
    private Button btnLogin, btnTesteAdmin, btnTesteCliente;
    private UsuarioDAO usuarioDAO;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar se já está logado
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            redirecionarParaMain();
            return;
        }

        setContentView(R.layout.activity_login);

        inicializarComponentes();
        configurarEventos();

        usuarioDAO = new UsuarioDAO(this);
        criarUsuariosIniciais();
    }

    private void redirecionarParaMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void inicializarComponentes() {
        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        btnLogin = findViewById(R.id.btnLogin);
        btnTesteAdmin = findViewById(R.id.btnTesteAdmin);
        btnTesteCliente = findViewById(R.id.btnTestCliente);
    }

    private void configurarEventos() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realizarLogin();
            }
        });

        btnTesteAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etEmail.setText("admin@teste.com");
                etSenha.setText("123456");
                realizarLogin();
            }
        });

        btnTesteCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etEmail.setText("cliente@exemplo.com");
                etSenha.setText("123456");
                realizarLogin();
            }
        });
    }


    private void realizarLogin() {
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Digite o email");
            etEmail.requestFocus();
            return;
        }

        if (senha.isEmpty()) {
            etSenha.setError("Digite a senha");
            etSenha.requestFocus();
            return;
        }

        // Abrir conexão com banco
        usuarioDAO.open();

        // Verificar login
        Usuario usuario = usuarioDAO.verificarLogin(email, senha);

        if (usuario != null) {
            // Login bem-sucedido - USAR APENAS createLoginSession
            sessionManager.createLoginSession(
                    usuario.getId(),
                    usuario.getEmail(),
                    usuario.getNome(),
                    usuario.getTipo()
            );

            Toast.makeText(this, "✅ Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

            // Ir para MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        } else {
            // Login falhou
            Toast.makeText(this, "❌ Email ou senha incorretos", Toast.LENGTH_SHORT).show();
        }

        // Fechar conexão
        usuarioDAO.close();
    }


    private void criarUsuariosIniciais() {
        usuarioDAO.open();

        // Verificar se já existem usuários
        if (usuarioDAO.contarUsuarios() == 0) {
            // Criar usuário admin
            Usuario admin = new Usuario("admin@teste.com", "123456", "Administrador", 0);
            usuarioDAO.inserirUsuario(admin);

            // Criar usuário cliente
            Usuario cliente = new Usuario("cliente@exemplo.com", "123456", "Cliente Teste", 1);
            usuarioDAO.inserirUsuario(cliente);

            Toast.makeText(this, "Usuários de teste criados!", Toast.LENGTH_SHORT).show();
        }

        usuarioDAO.close();
    }

}
