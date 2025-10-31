package com.example.helpdeskapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.helpdeskapp.api.RetrofitClient;
import com.example.helpdeskapp.api.UsuarioService;
import com.example.helpdeskapp.api.requests.LoginRequest;
import com.example.helpdeskapp.api.responses.LoginResponse;
import com.example.helpdeskapp.dao.UsuarioDAO;
import com.example.helpdeskapp.helpers.AuditoriaHelper;
import com.example.helpdeskapp.models.Usuario;
import com.example.helpdeskapp.utils.LoadingDialog;
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

    // ✅ SUBSTITUÍDO: ProgressBar por LoadingDialog
    private LoadingDialog loadingDialog;

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

        // ✅ INICIALIZAR LOADING DIALOG
        loadingDialog = new LoadingDialog(this);
    }

    private void inicializarComponentes() {
        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        btnLogin = findViewById(R.id.btnLogin);
        btnTesteAdmin = findViewById(R.id.btnTesteAdmin);
        btnTesteCliente = findViewById(R.id.btnTestCliente);
        tvModoOffline = findViewById(R.id.tvModoOffline);

        // ✅ REMOVIDO: progressBar (não é mais necessário)
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

        // ✅ MOSTRAR LOADING DIALOG LOGO NO INÍCIO
        loadingDialog.show("Verificando conexão...", "Aguarde um momento");

        // ✅ SE MODO OFFLINE FORÇADO, PULAR API
        if (forcarModoOffline) {
            Log.d(TAG, "📱 Modo OFFLINE FORÇADO");
            loadingDialog.updateMessage("Autenticando offline...");
            loadingDialog.updateSubMessage("Buscando dados locais");

            // Adicionar pequeno delay para melhor UX
            new Handler().postDelayed(() -> {
                tentarLoginLocal(email, senha);
            }, 800);
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
                    loadingDialog.updateMessage("Conectando ao servidor...");
                    loadingDialog.updateSubMessage("Autenticando usuário");
                    tentarLoginAPI(email, senha);
                } else {
                    Log.d(TAG, "📱 Modo OFFLINE - Tentando login local");
                    loadingDialog.updateMessage("Modo offline ativado");
                    loadingDialog.updateSubMessage("Autenticando localmente");

                    // Pequeno delay para mostrar a mensagem
                    new Handler().postDelayed(() -> {
                        tentarLoginLocal(email, senha);
                    }, 600);
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
                // ✅ FECHAR LOADING DIALOG
                loadingDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    Log.d(TAG, "✅ Login via API bem-sucedido!");

                    // ✅ SALVAR TODOS OS DADOS INCLUINDO TIPO
                    sessionManager.saveToken(loginResponse.getToken());
                    sessionManager.saveUserId(loginResponse.getUserId());
                    sessionManager.saveUserName(loginResponse.getUserName());
                    sessionManager.saveUserEmail(email);
                    sessionManager.saveUserType(loginResponse.getTipo());

                    Log.d(TAG, "🔑 Tipo de usuário salvo: " + loginResponse.getTipo());

                    // Salvar no SQLite para uso offline
                    salvarUsuarioOffline(loginResponse, senha);

                    Toast.makeText(LoginActivity.this,
                            "✅ Login realizado (Online)", Toast.LENGTH_SHORT).show();

                    irParaHome();
                } else {
                    Log.w(TAG, "API falhou, tentando login local...");
                    loadingDialog.show("Tentando modo offline...", "Buscando dados locais");

                    new Handler().postDelayed(() -> {
                        tentarLoginLocal(email, senha);
                    }, 600);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "Erro na API, usando modo offline", t);
                loadingDialog.updateMessage("Sem conexão com servidor");
                loadingDialog.updateSubMessage("Tentando modo offline");

                new Handler().postDelayed(() -> {
                    tentarLoginLocal(email, senha);
                }, 800);
            }
        });
    }

    private void tentarLoginLocal(String email, String senha) {
        Log.d(TAG, "💾 Tentando login OFFLINE...");

        // Buscar no SQLite local
        usuarioDAO.open();
        Usuario usuario = usuarioDAO.verificarLogin(email, senha);
        usuarioDAO.close();

        // ✅ FECHAR LOADING DIALOG
        loadingDialog.dismiss();

        if (usuario != null) {
            Log.d(TAG, "✅ Login offline bem-sucedido!");

            // ✅ SALVAR TODOS OS DADOS INCLUINDO TIPO
            sessionManager.saveUserId(usuario.getId());
            sessionManager.saveUserName(usuario.getNome());
            sessionManager.saveUserEmail(usuario.getEmail());
            sessionManager.saveUserType(usuario.getTipo());

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ✅ GARANTIR QUE O DIALOG SEJA FECHADO AO DESTRUIR A ACTIVITY
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
