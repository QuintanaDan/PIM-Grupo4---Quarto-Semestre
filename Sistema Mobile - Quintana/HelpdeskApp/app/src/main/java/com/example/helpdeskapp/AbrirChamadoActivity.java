package com.example.helpdeskapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Log;

import com.example.helpdeskapp.dao.ChamadoDAO;
import com.example.helpdeskapp.models.Chamado;
import com.example.helpdeskapp.utils.SessionManager;
import com.example.helpdeskapp.utils.NotificationHelper;
import com.example.helpdeskapp.helpers.AuditoriaHelper;
import com.example.helpdeskapp.utils.ThemeManager;
import com.example.helpdeskapp.api.ChamadoService;
import com.example.helpdeskapp.api.RetrofitClient;
import com.example.helpdeskapp.api.requests.ChamadoRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AbrirChamadoActivity extends AppCompatActivity {
    private static final String TAG = "AbrirChamado";

    private EditText etTitulo, etDescricao;
    private Spinner spinnerCategoria;
    private Spinner spinnerPrioridade;
    private Button btnSalvar, btnCancelar;
    private SessionManager sessionManager;
    private ChamadoDAO chamadoDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ThemeManager(this).applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abrir_chamado);

        // Configurar ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Abrir Chamado");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inicializarComponentes();
        configurarSpinnerCategoria();
        configurarSpinnerPrioridade();
        configurarEventos();
        sessionManager = new SessionManager(this);
        chamadoDAO = new ChamadoDAO(this);

        Log.d(TAG, "Activity inicializada com sucesso");
    }

    private void inicializarComponentes() {
        etTitulo = findViewById(R.id.etTitulo);
        etDescricao = findViewById(R.id.etDescricao);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnCancelar = findViewById(R.id.btnCancelar);
        spinnerPrioridade = findViewById(R.id.spinnerPrioridade);

        Log.d(TAG, "Componentes inicializados");
    }

    private void configurarSpinnerCategoria() {
        String[] categorias = {
                "Selecione uma categoria",
                "Hardware - Problemas com equipamentos",
                "Software - Problemas com aplicativos",
                "Rede - Problemas de conectividade",
                "Impressora - Problemas com impressão",
                "Email - Problemas com email",
                "Sistema - Problemas no sistema",
                "Outros - Outros tipos de problemas"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categorias);
        spinnerCategoria.setAdapter(adapter);
        Log.d(TAG, "Spinner de Categoria configurado com " + categorias.length + " categorias");
    }

    private void configurarSpinnerPrioridade() {
        String[] prioridades = {
                "Selecione a prioridade",
                "🟢Baixa-Não afeta o trabalho",
                "🟠Média-Afeta o trabalho",
                "🔴Alta-Paralisação Parcial",
                "🆘Crítica-Interrupção total do serviço"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, prioridades);
        spinnerPrioridade.setAdapter(adapter);
        Log.d(TAG, "Spinner de Prioridade configurado com " + prioridades.length + " prioridades");
    }


    private void configurarEventos() {
        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarChamado();
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Cancelando abertura de chamado");
                finish();
            }
        });

        Log.d(TAG, "Eventos configurados");
    }

    private void enviarChamado() {
        Log.d(TAG, "=== INICIANDO ENVIO DE CHAMADO ===");

        try {
            String titulo = etTitulo.getText().toString().trim();
            String descricao = etDescricao.getText().toString().trim();
            int categoriaPosition = spinnerCategoria.getSelectedItemPosition();
            String categoria = categoriaPosition > 0 ?
                    spinnerCategoria.getSelectedItem().toString() : "";
            String prioridade = spinnerPrioridade.getSelectedItem().toString();

            // Validações
            if (!validarFormulario(titulo, descricao, categoriaPosition)) {
                return;
            }

            if (!validarSessao()) {
                return;
            }

            long clienteId = sessionManager.getUserId();

            // Criar objeto Chamado
            Chamado novoChamado = new Chamado();
            novoChamado.setTitulo(titulo);
            novoChamado.setDescricao(descricao);
            novoChamado.setClienteId(clienteId);
            novoChamado.setPrioridade(prioridade);
            novoChamado.setCategoria(categoria);
            novoChamado.setStatus("Aberto");

            // ✅ SALVAR NA API (SQL SERVER)
            salvarNaAPI(novoChamado);

        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO: Erro ao enviar chamado", e);
            Toast.makeText(this, "❌ Erro ao enviar chamado: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ MÉTODO CORRIGIDO: Salvar na API
    private void salvarNaAPI(final Chamado chamado) {
        // Criar request
        ChamadoRequest request = new ChamadoRequest();
        request.setTitulo(chamado.getTitulo());
        request.setDescricao(chamado.getDescricao());
        request.setCategoria(chamado.getCategoria());
        request.setPrioridade(chamado.getPrioridade());
        request.setStatus(chamado.getStatus());
        request.setUsuarioId(chamado.getClienteId());

        // Chamar API
        ChamadoService service = RetrofitClient.getRetrofit().create(ChamadoService.class);

        service.criarChamado(request).enqueue(new Callback<Chamado>() {
            @Override
            public void onResponse(Call<Chamado> call, Response<Chamado> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Chamado chamadoCriado = response.body();

                    Log.d(TAG, "✅ Chamado salvo na API com ID: " + chamadoCriado.getId());

                    // Salvar localmente também (SQLite)
                    salvarLocalmente(chamadoCriado);

                    // Registrar auditoria
                    try {
                        AuditoriaHelper.registrarAcao(
                                AbrirChamadoActivity.this,
                                sessionManager.getUserId(),
                                "Criar Chamado",
                                "Chamado " + chamadoCriado.getNumero() + " criado: " + chamadoCriado.getTitulo(),
                                chamadoCriado.getId(),
                                "127.0.0.1"
                        );
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao registrar auditoria", e);
                    }

                    // Notificar administradores
                    try {
                        NotificationHelper notificationHelper = new NotificationHelper(AbrirChamadoActivity.this);
                        notificationHelper.notificarAdministradores(
                                chamadoCriado.getId(),
                                chamadoCriado.getTitulo(),
                                chamadoCriado.getPrioridade(),
                                sessionManager.getUserName()
                        );
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao notificar", e);
                    }

                    Toast.makeText(AbrirChamadoActivity.this,
                            "✅ Chamado " + chamadoCriado.getNumero() + " criado com sucesso!",
                            Toast.LENGTH_LONG).show();

                    finish();
                } else {
                    Log.e(TAG, "❌ Erro na resposta da API: " + response.code());
                    Toast.makeText(AbrirChamadoActivity.this,
                            "❌ Erro ao criar chamado: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Chamado> call, Throwable t) {
                Log.e(TAG, "❌ Falha ao conectar na API: " + t.getMessage(), t);

                // Log detalhado da URL tentada
                if (call != null && call.request() != null) {
                    Log.e(TAG, "URL tentada: " + call.request().url());
                }

                Toast.makeText(AbrirChamadoActivity.this,
                        "⚠️ Sem conexão. Salvando localmente...",
                        Toast.LENGTH_LONG).show();

                // ✅ CORRIGIDO: Usar 'chamado' que é o parâmetro do método
                salvarApenasLocalmente(chamado);
            }
        });
    }

    // Salvar localmente após sucesso na API
    private void salvarLocalmente(Chamado chamado) {
        try {
            chamadoDAO.open();
            chamadoDAO.inserir(chamado);
            chamadoDAO.close();
            Log.d(TAG, "✅ Chamado salvo localmente também");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar localmente", e);
        }
    }

    // Salvar apenas localmente (fallback)
    private void salvarApenasLocalmente(Chamado chamado) {
        try {
            ChamadoDAO dao = new ChamadoDAO(this);
            dao.open();
            long id = dao.inserir(chamado);
            dao.close();

            if (id > 0) {
                Log.d(TAG, "✅ Chamado salvo localmente com ID: " + id);

                runOnUiThread(() -> {
                    Toast.makeText(this,
                            "✅ Chamado " + chamado.getNumero() + " salvo localmente!",
                            Toast.LENGTH_LONG).show();

                    // ✅ FECHAR ACTIVITY E VOLTAR
                    finish();
                });
            } else {
                Log.e(TAG, "❌ Erro ao salvar localmente: ID inválido");
                runOnUiThread(() -> {
                    Toast.makeText(this,
                            "❌ Erro ao salvar localmente",
                            Toast.LENGTH_SHORT).show();
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Exceção ao salvar localmente: " + e.getMessage(), e);
            runOnUiThread(() -> {
                Toast.makeText(this,
                        "❌ Erro: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
        }
    }

    private boolean validarFormulario(String titulo, String descricao, int categoriaPosition) {
        if (TextUtils.isEmpty(titulo)) {
            Log.w(TAG, "❌ Validação falhou: Título vazio");
            etTitulo.setError("Digite o título do problema");
            etTitulo.requestFocus();
            return false;
        }

        if (titulo.length() < 5) {
            Log.w(TAG, "❌ Validação falhou: Título muito curto (" + titulo.length() + " chars)");
            etTitulo.setError("Título deve ter pelo menos 5 caracteres");
            etTitulo.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(descricao)) {
            Log.w(TAG, "❌ Validação falhou: Descrição vazia");
            etDescricao.setError("Descreva o problema");
            etDescricao.requestFocus();
            return false;
        }

        if (descricao.length() < 10) {
            Log.w(TAG, "❌ Validação falhou: Descrição muito curta (" + descricao.length() + " chars)");
            etDescricao.setError("Descrição deve ter pelo menos 10 caracteres");
            etDescricao.requestFocus();
            return false;
        }

        if (categoriaPosition == 0) {
            Log.w(TAG, "❌ Validação falhou: Nenhuma categoria selecionada");
            Toast.makeText(this, "Selecione uma categoria", Toast.LENGTH_SHORT).show();
            return false;
        }

        Log.d(TAG, "✅ Todas as validações do formulário passaram");
        return true;
    }

    private boolean validarSessao() {
        if (sessionManager == null) {
            Log.e(TAG, "❌ ERRO: SessionManager é null!");
            Toast.makeText(this, "Erro: Sessão não inicializada", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        if (!sessionManager.isLoggedIn()) {
            Log.e(TAG, "❌ ERRO: Usuário não está logado");
            Toast.makeText(this, "Erro: Usuário não está logado", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        long clienteId = sessionManager.getUserId();
        if (clienteId <= 0) {
            Log.e(TAG, "❌ ERRO: ID do usuário inválido: " + clienteId);
            Toast.makeText(this, "Erro: ID do usuário inválido", Toast.LENGTH_SHORT).show();
            return false;
        }

        Log.d(TAG, "✅ Sessão validada com sucesso");
        return true;
    }


    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "Navegação para trás pressionada");
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chamadoDAO != null) {
            chamadoDAO.close();
        }
        Log.d(TAG, "Activity destruída");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Activity pausada");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity retomada");
    }
}