package com.example.helpdeskapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.helpdeskapp.api.RetrofitClient;
import com.example.helpdeskapp.api.requests.LoginRequest;
import com.example.helpdeskapp.api.responses.LoginResponse;
import com.example.helpdeskapp.dao.UsuarioDAO;
import com.example.helpdeskapp.helpers.AuditoriaHelper;
import com.example.helpdeskapp.models.Usuario;
import com.example.helpdeskapp.utils.SessionManager;
import com.example.helpdeskapp.utils.ThemeManager;
import com.example.helpdeskapp.api.ApiService;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText etEmail, etSenha;
    private Button btnLogin, btnTesteAdmin, btnTesteCliente;
    private ProgressBar progressBar;
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

        // ✅ ADICIONAR PROGRESSBAR (se existir no layout)
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

        mostrarLoading(true);

        // ✅ TENTAR LOGIN VIA API PRIMEIRO
        loginViaAPI(email, senha);
    }

    private void loginViaAPI(String email, String senha) {
        Log.d(TAG, "🌐 Tentando login via API...");

        LoginRequest request = new LoginRequest(email, senha);

        RetrofitClient.getApiService().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    Log.d(TAG, "✅ Login via API bem-sucedido!");
                    Log.d(TAG, "📊 Dados recebidos:");
                    Log.d(TAG, "   ID: " + loginResponse.getId());
                    Log.d(TAG, "   Nome: " + loginResponse.getNome());
                    Log.d(TAG, "   Email: " + loginResponse.getEmail());
                    Log.d(TAG, "   Tipo: " + loginResponse.getTipo());
                    Log.d(TAG, "   Token: " + loginResponse.getToken().substring(0, 20) + "...");

                    processarLoginSucesso(loginResponse);
                } else {
                    Log.e(TAG, "❌ Erro na API: " + response.code());
                    Log.e(TAG, "❌ Mensagem: " + response.message());

                    // Fallback: tentar login offline
                    loginOffline(email, senha);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "❌ Falha na conexão com API: " + t.getMessage());
                t.printStackTrace();

                // Fallback: tentar login offline
                loginOffline(email, senha);
            }
        });
    }

    private void loginOffline(String email, String senha) {
        Log.d(TAG, "💾 Tentando login OFFLINE...");

        usuarioDAO.open();
        Usuario usuario = usuarioDAO.verificarLogin(email, senha);
        usuarioDAO.close();

        if (usuario != null) {
            Log.d(TAG, "✅ Login offline bem-sucedido!");

            // Salvar sessão SEM token (modo offline)
            sessionManager.createLoginSession(
                    usuario.getId(),
                    usuario.getEmail(),
                    usuario.getNome(),
                    usuario.getTipo()
            );

            // Registrar auditoria
            AuditoriaHelper.registrarLogin(LoginActivity.this, usuario.getId(), usuario.getNome());

            String tipoUsuario = usuario.getTipo() == 1 ? "Administrador" : "Cliente";

            Toast.makeText(LoginActivity.this,
                    "✅ Login offline realizado!\n" +
                            "Bem-vindo, " + usuario.getNome() + " (" + tipoUsuario + ")\n" +
                            "Modo: Offline",
                    Toast.LENGTH_SHORT).show();

            mostrarLoading(false);
            redirecionarParaMain();
        } else {
            Log.e(TAG, "❌ Login offline falhou - usuário não encontrado");
            mostrarLoading(false);
            Toast.makeText(LoginActivity.this,
                    "❌ Email ou senha inválidos\n(Sem conexão com a API)",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void processarLoginSucesso(LoginResponse response) {
        // 1. Salvar no banco local
        salvarUsuarioLocal(response);

        // 2. Salvar sessão COM token
        sessionManager.createLoginSession(
                response.getId(),
                response.getEmail(),
                response.getNome(),
                response.getTipo()
        );

        // 3. Salvar token
        sessionManager.saveToken(response.getToken());

        Log.d(TAG, "🔑 Token salvo com sucesso");

        // 4. Registrar auditoria
        AuditoriaHelper.registrarLogin(this, response.getId(), response.getNome());

        String tipoUsuario = response.getTipo() == 1 ? "Administrador" : "Cliente";

        Toast.makeText(this,
                "✅ Login realizado com sucesso!\n" +
                        "Bem-vindo, " + response.getNome() + " (" + tipoUsuario + ")\n" +
                        "Modo: Online",
                Toast.LENGTH_SHORT).show();

        mostrarLoading(false);
        redirecionarParaMain();
    }

    private void salvarUsuarioLocal(LoginResponse response) {
        usuarioDAO.open();

        try {
            // Verificar se usuário já existe
            Usuario usuarioExistente = usuarioDAO.buscarPorEmail(response.getEmail());

            if (usuarioExistente == null) {
                // Criar novo usuário local
                Usuario novoUsuario = new Usuario();
                novoUsuario.setNome(response.getNome());
                novoUsuario.setEmail(response.getEmail());
                novoUsuario.setSenha(""); // Não salvar senha vinda da API
                novoUsuario.setTipo(response.getTipo());

                long id = usuarioDAO.inserirUsuario(novoUsuario);
                Log.d(TAG, "💾 Usuário salvo localmente com ID: " + id);
            } else {
                // Atualizar dados do usuário existente
                usuarioExistente.setNome(response.getNome());
                usuarioExistente.setTipo(response.getTipo());

                usuarioDAO.atualizarUsuario(usuarioExistente);
                Log.d(TAG, "💾 Usuário atualizado localmente");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao salvar usuário localmente: " + e.getMessage());
        } finally {
            usuarioDAO.close();
        }
    }

    private void mostrarLoading(boolean mostrar) {
        if (progressBar != null) {
            progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }

        btnLogin.setEnabled(!mostrar);
        btnTesteAdmin.setEnabled(!mostrar);
        btnTesteCliente.setEnabled(!mostrar);
        etEmail.setEnabled(!mostrar);
        etSenha.setEnabled(!mostrar);
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
    ApiService service = RetrofitClient.getRetrofit().create(ApiService.class);
}