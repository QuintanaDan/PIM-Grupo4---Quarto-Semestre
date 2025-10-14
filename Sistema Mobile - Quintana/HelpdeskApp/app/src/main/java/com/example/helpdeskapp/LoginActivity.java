package com.example.helpdeskapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.helpdeskapp.dao.UsuarioDAO;
import com.example.helpdeskapp.models.Usuario;
import com.example.helpdeskapp.utils.SessionManager;
import com.example.helpdeskapp.utils.AuditoriaHelper;
import com.example.helpdeskapp.utils.ThemeManager;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText etEmail, etSenha;
    private Button btnLogin, btnTesteAdmin, btnTesteCliente;
    private UsuarioDAO usuarioDAO;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ThemeManager(this).applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Verificar se já está logado
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            redirecionarParaMain();
            return;
        }

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
        btnLogin.setOnClickListener(v -> realizarLogin());

        btnTesteAdmin.setOnClickListener(v -> {
            etEmail.setText("admin@helpdesk.com");
            etSenha.setText("admin123");
            realizarLogin();
        });

        btnTesteCliente.setOnClickListener(v -> {
            etEmail.setText("cliente@helpdesk.com");
            etSenha.setText("123456");
            realizarLogin();
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
            // Login bem-sucedido
            sessionManager.createLoginSession(
                    usuario.getId(),
                    usuario.getEmail(),
                    usuario.getNome(),
                    usuario.getTipo()
            );

            // Registrar auditoria
            AuditoriaHelper.registrarLogin(
                    this,
                    usuario.getId(),
                    usuario.getNome()
            );

            String tipoUsuario = usuario.getTipo() == 1 ? "Administrador" : "Cliente";
            Toast.makeText(this,
                    "✅ Login realizado com sucesso!\n" +
                            "Bem-vindo, " + usuario.getNome() + " (" + tipoUsuario + ")",
                    Toast.LENGTH_SHORT).show();

            // Ir para MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        } else {
            Toast.makeText(this, "❌ Email ou senha incorretos", Toast.LENGTH_SHORT).show();
        }

        usuarioDAO.close();
    }

    private void criarUsuariosIniciais() {
        usuarioDAO.open();

        try {
            int totalUsuarios = usuarioDAO.contarUsuarios();
            Log.d(TAG, "📊 Total de usuários no banco: " + totalUsuarios);

            if (totalUsuarios == 0) {
                Log.d(TAG, "🔧 Criando usuários iniciais...");

                // ✅ CORRETO: Admin com tipo = 1
                Usuario admin = new Usuario();
                admin.setNome("Administrador");
                admin.setEmail("admin@helpdesk.com");
                admin.setSenha("admin123");
                admin.setTipo(1); // ✅ 1 = Admin

                long adminId = usuarioDAO.inserirUsuario(admin);
                if (adminId > 0) {
                    Log.d(TAG, "✅ Admin criado com ID: " + adminId);
                } else {
                    Log.e(TAG, "❌ Erro ao criar admin");
                }

                // ✅ CORRETO: Cliente com tipo = 0
                Usuario cliente = new Usuario();
                cliente.setNome("Cliente Teste");
                cliente.setEmail("cliente@helpdesk.com");
                cliente.setSenha("123456");
                cliente.setTipo(0); // ✅ 0 = Cliente

                long clienteId = usuarioDAO.inserirUsuario(cliente);
                if (clienteId > 0) {
                    Log.d(TAG, "✅ Cliente criado com ID: " + clienteId);
                } else {
                    Log.e(TAG, "❌ Erro ao criar cliente");
                }

                Toast.makeText(this,
                        "✅ Usuários criados com sucesso!\n\n" +
                                "👨‍💼 Admin: admin@helpdesk.com / admin123\n" +
                                "👤 Cliente: cliente@helpdesk.com / 123456",
                        Toast.LENGTH_LONG).show();

            } else {
                Log.d(TAG, "✅ Usuários já existem no banco (" + totalUsuarios + " usuários)");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao criar usuários iniciais", e);
            Toast.makeText(this, "❌ Erro ao criar usuários: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            usuarioDAO.close();
        }
    }
}