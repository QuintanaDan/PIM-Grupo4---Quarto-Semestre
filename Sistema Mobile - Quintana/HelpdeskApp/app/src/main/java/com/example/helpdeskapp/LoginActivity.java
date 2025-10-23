package com.example.helpdeskapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.helpdeskapp.api.RetrofitClient;
import com.example.helpdeskapp.api.UsuarioService;
import com.example.helpdeskapp.api.requests.LoginRequest;
import com.example.helpdeskapp.api.responses.LoginResponse;
import com.example.helpdeskapp.dao.UsuarioDAO;
import com.example.helpdeskapp.helpers.AuditoriaHelper;
import com.example.helpdeskapp.models.Usuario;
import com.example.helpdeskapp.utils.NetworkHelper;
import com.example.helpdeskapp.utils.SessionManager;
import com.example.helpdeskapp.utils.ThemeManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText etEmail, etSenha;
    private Button btnLogin, btnTesteAdmin, btnTesteCliente;
    private TextView tvModoOffline;
    private ProgressBar progressBar;
    private UsuarioDAO usuarioDAO;
    private SessionManager sessionManager;

    private boolean forcarModoOffline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ThemeManager(this).applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Verificar se já está logado
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            irParaHome();
            return;
        }

        inicializarComponentes();
        configurarEventos();

        usuarioDAO = new UsuarioDAO(this);
        criarUsuariosIniciais();
    }

    private void inicializarComponentes() {
        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        btnLogin = findViewById(R.id.btnLogin);
        btnTesteAdmin = findViewById(R.id.btnTesteAdmin);
        btnTesteCliente = findViewById(R.id.btnTestCliente);
        tvModoOffline = findViewById(R.id.tvModoOffline);

        progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
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

        // ✅ BOTÃO MODO OFFLINE
        if (tvModoOffline != null) {
            tvModoOffline.setOnClickListener(v -> {
                forcarModoOffline = !forcarModoOffline;

                if (forcarModoOffline) {
                    tvModoOffline.setText("✅ Modo Offline ATIVADO");
                    tvModoOffline.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    Toast.makeText(this, "📱 Modo Offline ativado", Toast.LENGTH_SHORT).show();
                } else {
                    tvModoOffline.setText("🔧 Modo Desenvolvedor: Forçar Offline");
                    tvModoOffline.setTextColor(getResources().getColor(R.color.text_secondary));
                    Toast.makeText(this, "🌐 Modo Online ativado", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean validarCampos(String email, String senha) {
        if (email.isEmpty()) {
            etEmail.setError("Email obrigatório");
            etEmail.requestFocus();
            return false;
        }

        if (senha.isEmpty()) {
            etSenha.setError("Senha obrigatória");
            etSenha.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email inválido");
            etEmail.requestFocus();
            return false;
        }

        return true;
    }

    private void realizarLogin() {
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();

        if (!validarCampos(email, senha)) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // ✅ SE MODO OFFLINE FORÇADO, PULAR API
        if (forcarModoOffline) {
            Log.d(TAG, "📱 Modo OFFLINE FORÇADO");
            tentarLoginLocal(email, senha);
            return;
        }

        // ✅ VERIFICAR DISPONIBILIDADE EM BACKGROUND
        new Thread(() -> {
            final boolean apiOnline = NetworkHelper.apiDisponivel(
                    this,
                    RetrofitClient.getBaseUrl()
            );

            runOnUiThread(() -> {
                if (apiOnline) {
                    Log.d(TAG, "🌐 Modo ONLINE - Tentando login via API");
                    tentarLoginAPI(email, senha);
                } else {
                    Log.d(TAG, "📱 Modo OFFLINE - Tentando login local");
                    tentarLoginLocal(email, senha);
                }
            });
        }).start();
    }

    private void tentarLoginAPI(String email, String senha) {
        LoginRequest loginRequest = new LoginRequest(email, senha);

        UsuarioService service = RetrofitClient.getRetrofit().create(UsuarioService.class);
        service.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    Log.d(TAG, "✅ Login via API bem-sucedido!");

                    // ✅ SALVAR TODOS OS DADOS INCLUINDO TIPO!
                    sessionManager.saveToken(loginResponse.getToken());
                    sessionManager.saveUserId(loginResponse.getUserId());
                    sessionManager.saveUserName(loginResponse.getUserName());
                    sessionManager.saveUserEmail(email);
                    sessionManager.saveUserType(loginResponse.getTipo()); // ✅ ADICIONAR ESTA LINHA!

                    Log.d(TAG, "🔑 Tipo de usuário salvo: " + loginResponse.getTipo());

                    // Salvar no SQLite para uso offline
                    salvarUsuarioOffline(loginResponse, senha);

                    Toast.makeText(LoginActivity.this,
                            "✅ Login realizado (Online)", Toast.LENGTH_SHORT).show();

                    irParaHome();
                } else {
                    Log.w(TAG, "API falhou, tentando login local...");
                    tentarLoginLocal(email, senha);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "Erro na API, usando modo offline", t);
                tentarLoginLocal(email, senha);
            }
        });
    }

    private void tentarLoginLocal(String email, String senha) {
        Log.d(TAG, "💾 Tentando login OFFLINE...");

        // Buscar no SQLite local
        usuarioDAO.open();
        Usuario usuario = usuarioDAO.verificarLogin(email, senha);
        usuarioDAO.close();

        progressBar.setVisibility(View.GONE);
        btnLogin.setEnabled(true);

        if (usuario != null) {
            Log.d(TAG, "✅ Login offline bem-sucedido!");

            // ✅ SALVAR TODOS OS DADOS INCLUINDO TIPO!
            sessionManager.saveUserId(usuario.getId());
            sessionManager.saveUserName(usuario.getNome());
            sessionManager.saveUserEmail(usuario.getEmail());
            sessionManager.saveUserType(usuario.getTipo()); // ✅ ADICIONAR ESTA LINHA!

            Log.d(TAG, "🔑 Tipo de usuário salvo: " + usuario.getTipo());

            // Registrar auditoria
            try {
                AuditoriaHelper.registrarLogin(this, usuario.getId(), usuario.getNome());
            } catch (Exception e) {
                Log.e(TAG, "Erro ao registrar auditoria", e);
            }

            Toast.makeText(this,
                    "✅ Login realizado (Offline)", Toast.LENGTH_SHORT).show();

            irParaHome();
        } else {
            Toast.makeText(this,
                    "❌ Email ou senha incorretos", Toast.LENGTH_LONG).show();
        }
    }

    private void salvarUsuarioOffline(LoginResponse loginResponse, String senha) {
        usuarioDAO.open();

        try {
            Usuario usuarioExistente = usuarioDAO.buscarPorEmail(loginResponse.getEmail());

            if (usuarioExistente == null) {
                // Criar novo usuário
                Usuario novoUsuario = new Usuario();
                novoUsuario.setNome(loginResponse.getUserName());
                novoUsuario.setEmail(loginResponse.getEmail());
                novoUsuario.setSenha(senha); // Salvar senha para uso offline
                novoUsuario.setTipo(loginResponse.getTipo());

                long id = usuarioDAO.inserirUsuario(novoUsuario);
                Log.d(TAG, "💾 Usuário salvo localmente com ID: " + id);
            } else {
                // Atualizar usuário existente
                usuarioExistente.setNome(loginResponse.getUserName());
                usuarioExistente.setSenha(senha);
                usuarioExistente.setTipo(loginResponse.getTipo());
                usuarioDAO.atualizarUsuario(usuarioExistente);
                Log.d(TAG, "💾 Usuário atualizado localmente");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao salvar usuário localmente: " + e.getMessage());
        } finally {
            usuarioDAO.close();
        }
    }

    private void irParaHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void criarUsuariosIniciais() {
        usuarioDAO.open();

        try {
            int totalUsuarios = usuarioDAO.contarUsuarios();
            Log.d(TAG, "📊 Total de usuários no banco: " + totalUsuarios);

            if (totalUsuarios == 0) {
                Log.d(TAG, "🔧 Criando usuários iniciais...");

                // Admin
                Usuario admin = new Usuario();
                admin.setNome("Administrador");
                admin.setEmail("admin@helpdesk.com");
                admin.setSenha("admin123");
                admin.setTipo(1);

                long adminId = usuarioDAO.inserirUsuario(admin);
                if (adminId > 0) {
                    Log.d(TAG, "✅ Admin criado com ID: " + adminId);
                } else {
                    Log.e(TAG, "❌ Erro ao criar admin");
                }

                // Cliente
                Usuario cliente = new Usuario();
                cliente.setNome("Cliente Teste");
                cliente.setEmail("cliente@helpdesk.com");
                cliente.setSenha("123456");
                cliente.setTipo(0);

                long clienteId = usuarioDAO.inserirUsuario(cliente);
                if (clienteId > 0) {
                    Log.d(TAG, "✅ Cliente criado com ID: " + clienteId);
                } else {
                    Log.e(TAG, "❌ Erro ao criar cliente");
                }

                Toast.makeText(this,
                        "✅ Usuários criados!\n\n" +
                                "👨‍💼 Admin: admin@helpdesk.com / admin123\n" +
                                "👤 Cliente: cliente@helpdesk.com / 123456",
                        Toast.LENGTH_LONG).show();

            } else {
                Log.d(TAG, "✅ Usuários já existem (" + totalUsuarios + " usuários)");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao criar usuários iniciais", e);
        } finally {
            usuarioDAO.close();
        }
    }
}