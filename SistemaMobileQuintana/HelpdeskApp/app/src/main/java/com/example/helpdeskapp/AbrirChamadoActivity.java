package com.example.helpdeskapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.cardview.widget.CardView;
import android.widget.ProgressBar;
import com.example.helpdeskapp.dao.ChamadoDAO;
import com.example.helpdeskapp.models.Chamado;
import com.example.helpdeskapp.utils.SessionManager;
import com.example.helpdeskapp.utils.NotificationHelper;
import com.example.helpdeskapp.utils.NetworkHelper;
import com.example.helpdeskapp.helpers.AuditoriaHelper;
import com.example.helpdeskapp.utils.ThemeManager;
import com.example.helpdeskapp.api.ChamadoService;
import com.example.helpdeskapp.api.RetrofitClient;
import com.example.helpdeskapp.api.requests.ChamadoRequest;
import com.example.helpdeskapp.api.GroqClient;
import com.example.helpdeskapp.api.GroqService;
import com.example.helpdeskapp.models.groq.GroqRequest;
import com.example.helpdeskapp.models.groq.GroqResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class AbrirChamadoActivity extends AppCompatActivity {
    private static final String TAG = "AbrirChamado";

    private EditText etTitulo, etDescricao;
    private Spinner spinnerCategoria;
    private Spinner spinnerPrioridade;
    private Button btnSalvar, btnCancelar;
    private SessionManager sessionManager;
    private ChamadoDAO chamadoDAO;
    private Button btnAssistenteIA;
    private CardView cardPerguntasIA;
    private ProgressBar progressBarPerguntasIA;
    private TextView tvPerguntasIA;
    private EditText etRespostasIA;
    private Button btnGerarDescricao;
    private String perguntasGeradas = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ThemeManager(this).applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abrir_chamado);

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
        btnAssistenteIA = findViewById(R.id.btnAssistenteIA);
        cardPerguntasIA = findViewById(R.id.cardPerguntasIA);
        progressBarPerguntasIA = findViewById(R.id.progressBarPerguntasIA);
        tvPerguntasIA = findViewById(R.id.tvPerguntasIA);
        etRespostasIA = findViewById(R.id.etRespostasIA);
        btnGerarDescricao = findViewById(R.id.btnGerarDescricao);

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
        Log.d(TAG, "Spinner de Categoria configurado");
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
        Log.d(TAG, "Spinner de Prioridade configurado");
    }

    private void configurarEventos() {
        btnSalvar.setOnClickListener(v -> enviarChamado());
        btnCancelar.setOnClickListener(v -> {
            Log.d(TAG, "Cancelando abertura de chamado");
            finish();
        });
        btnAssistenteIA.setOnClickListener(v -> buscarPerguntasIA());
        btnGerarDescricao.setOnClickListener(v -> gerarDescricaoCompleta());

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

            int prioridadePosition = spinnerPrioridade.getSelectedItemPosition();
            String prioridadeSelecionada = "";

            if (prioridadePosition > 0) {
                prioridadeSelecionada = spinnerPrioridade.getSelectedItem().toString();
                if (prioridadeSelecionada.contains("-")) {
                    prioridadeSelecionada = prioridadeSelecionada.split("-")[0].trim();
                }
            } else {
                prioridadeSelecionada = "Média";
            }

            Log.d(TAG, "📊 Dados coletados:");
            Log.d(TAG, "   Título: " + titulo);
            Log.d(TAG, "   Categoria: " + categoria);
            Log.d(TAG, "   Prioridade: " + prioridadeSelecionada);

            if (!validarFormulario(titulo, descricao, categoriaPosition)) {
                return;
            }

            if (prioridadePosition == 0) {
                Toast.makeText(this, "Selecione uma prioridade", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!validarSessao()) {
                return;
            }

            long clienteId = sessionManager.getUserId();

            Chamado novoChamado = new Chamado();
            novoChamado.setTitulo(titulo);
            novoChamado.setDescricao(descricao);
            novoChamado.setClienteId(clienteId);
            novoChamado.setPrioridade(prioridadeSelecionada);
            novoChamado.setCategoria(categoria);
            novoChamado.setStatus("Aberto");

            // ✅ DESABILITAR BOTÃO E MOSTRAR LOADING
            btnSalvar.setEnabled(false);
            btnSalvar.setText("Salvando...");

            // ✅ VERIFICAR API EM BACKGROUND (IGUAL AO LOGIN!)
            new Thread(() -> {
                final boolean apiOnline = NetworkHelper.apiDisponivel(
                        this,
                        RetrofitClient.getBaseUrl()
                );

                runOnUiThread(() -> {
                    if (apiOnline) {
                        Log.d(TAG, "🌐 API Online - Salvando na API");
                        salvarNaAPI(novoChamado);
                    } else {
                        Log.d(TAG, "📱 API Offline - Salvando localmente");
                        salvarApenasLocalmente(novoChamado);
                    }
                });
            }).start();

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao enviar chamado", e);
            Toast.makeText(this, "❌ Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnSalvar.setEnabled(true);
            btnSalvar.setText("Salvar");
        }
    }

    private void salvarNaAPI(final Chamado chamado) {
        Log.d(TAG, "📤 === ENVIANDO PARA API ===");
        Log.d(TAG, "   Título: " + chamado.getTitulo());
        Log.d(TAG, "   Descrição: " + chamado.getDescricao());
        Log.d(TAG, "   Categoria: " + chamado.getCategoria());
        Log.d(TAG, "   Prioridade: " + chamado.getPrioridade());
        Log.d(TAG, "   Status: " + chamado.getStatus());
        Log.d(TAG, "   Usuario ID: " + chamado.getClienteId());

        // ✅ PEGAR O TOKEN DO SESSION MANAGER
        String authHeader = sessionManager.getAuthHeader();

        if (authHeader == null || authHeader.isEmpty()) {
            Log.e(TAG, "❌ Token não encontrado! Redirecionando para login...");
            Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_LONG).show();
            btnSalvar.setEnabled(true);
            btnSalvar.setText("Salvar");
            finish();
            return;
        }

        Log.d(TAG, "🔑 Usando Authorization Header: " + authHeader.substring(0, Math.min(20, authHeader.length())) + "...");

        ChamadoRequest request = new ChamadoRequest();
        request.setTitulo(chamado.getTitulo());
        request.setDescricao(chamado.getDescricao());
        request.setCategoria(chamado.getCategoria());
        request.setPrioridade(chamado.getPrioridade());
        request.setStatus(chamado.getStatus());
        request.setUsuarioId(chamado.getClienteId());

        Log.d(TAG, "📦 Request montado, chamando API com autenticação...");

        ChamadoService service = RetrofitClient.getRetrofit().create(ChamadoService.class);

        // ✅ PASSAR O TOKEN NO HEADER
        service.criarChamado(authHeader, request).enqueue(new Callback<Chamado>() {
            @Override
            public void onResponse(Call<Chamado> call, Response<Chamado> response) {
                Log.d(TAG, "📥 === RESPOSTA DA API ===");
                Log.d(TAG, "   Código HTTP: " + response.code());
                Log.d(TAG, "   Sucesso: " + response.isSuccessful());
                Log.d(TAG, "   Body null: " + (response.body() == null));

                btnSalvar.setEnabled(true);
                btnSalvar.setText("Salvar");

                if (response.isSuccessful() && response.body() != null) {
                    Chamado chamadoCriado = response.body();

                    Log.d(TAG, "✅ Chamado salvo na API!");
                    Log.d(TAG, "   ID retornado: " + chamadoCriado.getId());
                    Log.d(TAG, "   Número: " + chamadoCriado.getNumero());

                    // Salvar cache local
                    salvarCacheLocal(chamadoCriado);

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

                    // Notificar admins
                    try {
                        Log.d(TAG, "🔔 Notificando administradores...");
                        NotificationHelper notificationHelper = new NotificationHelper(AbrirChamadoActivity.this);
                        notificationHelper.notificarAdministradores(
                                chamadoCriado.getId(),
                                chamadoCriado.getTitulo(),
                                chamadoCriado.getPrioridade(),
                                sessionManager.getUserName()
                        );
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Erro ao notificar", e);
                        e.printStackTrace();
                    }

                    Toast.makeText(AbrirChamadoActivity.this,
                            "✅ Chamado #" + chamadoCriado.getNumero() + " criado! (Online)",
                            Toast.LENGTH_LONG).show();

                    finish();
                } else {
                    Log.e(TAG, "❌ API retornou erro!");
                    Log.e(TAG, "   Código: " + response.code());

                    // ✅ LER O ERRO
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "   Error Body: " + errorBody);

                            // ✅ SE FOR 401, TALVEZ O TOKEN EXPIROU
                            if (response.code() == 401) {
                                Log.e(TAG, "   ⚠️ Token inválido ou expirado!");
                                Toast.makeText(AbrirChamadoActivity.this,
                                        "Sessão expirada. Salvando localmente...",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "   Erro ao ler errorBody", e);
                    }

                    Log.w(TAG, "⚠️ Salvando localmente devido a erro da API...");
                    salvarApenasLocalmente(chamado);
                }
            }

            @Override
            public void onFailure(Call<Chamado> call, Throwable t) {
                Log.e(TAG, "❌ === FALHA NA CHAMADA DA API ===");
                Log.e(TAG, "   Tipo: " + t.getClass().getSimpleName());
                Log.e(TAG, "   Mensagem: " + t.getMessage());
                t.printStackTrace();

                salvarApenasLocalmente(chamado);
            }
        });
    }

    private void salvarCacheLocal(Chamado chamado) {
        new Thread(() -> {
            try {
                Log.d(TAG, "💾 === SALVANDO CACHE LOCAL ===");
                Log.d(TAG, "   Chamado ID da API: " + chamado.getId());
                Log.d(TAG, "   Título: " + chamado.getTitulo());
                Log.d(TAG, "   Número/Protocolo: " + chamado.getNumero());

                ChamadoDAO dao = new ChamadoDAO(this);
                dao.open();

                // ✅ USAR inserirComId() para manter ID e protocolo da API
                long resultado = dao.inserirComId(chamado);

                dao.close();

                if (resultado > 0) {
                    Log.d(TAG, "💾 ✅ Cache local salvo com sucesso!");
                    Log.d(TAG, "   ID mantido: " + chamado.getId());
                    Log.d(TAG, "   Protocolo: " + chamado.getNumero());
                } else {
                    Log.e(TAG, "❌ Erro ao salvar cache local. Resultado: " + resultado);
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Exceção ao salvar cache: ", e);
                e.printStackTrace();
            }
        }).start();
    }

    private void salvarApenasLocalmente(Chamado chamado) {
        new Thread(() -> {
            try {
                ChamadoDAO dao = new ChamadoDAO(this);
                dao.open();
                long id = dao.inserir(chamado);
                dao.close();

                runOnUiThread(() -> {
                    btnSalvar.setEnabled(true);
                    btnSalvar.setText("Salvar");

                    if (id > 0) {
                        // Buscar chamado para pegar número gerado
                        dao.open();
                        Chamado salvo = dao.buscarPorId(id);
                        dao.close();

                        String protocolo = salvo != null ? salvo.getNumero() : "???";

                        Log.d(TAG, "✅ Chamado salvo localmente! ID: " + id);

                        Toast.makeText(this,
                                "✅ Chamado #" + protocolo + " criado! (Offline)\n" +
                                        "Será sincronizado quando houver conexão.",
                                Toast.LENGTH_LONG).show();

                        finish();
                    } else {
                        Toast.makeText(this,
                                "❌ Erro ao salvar localmente",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "❌ Exceção ao salvar localmente", e);
                runOnUiThread(() -> {
                    btnSalvar.setEnabled(true);
                    btnSalvar.setText("Salvar");
                    Toast.makeText(this,
                            "❌ Erro: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private boolean validarFormulario(String titulo, String descricao, int categoriaPosition) {
        if (TextUtils.isEmpty(titulo)) {
            etTitulo.setError("Digite o título do problema");
            etTitulo.requestFocus();
            return false;
        }

        if (titulo.length() < 5) {
            etTitulo.setError("Título deve ter pelo menos 5 caracteres");
            etTitulo.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(descricao)) {
            etDescricao.setError("Descreva o problema");
            etDescricao.requestFocus();
            return false;
        }

        if (descricao.length() < 10) {
            etDescricao.setError("Descrição deve ter pelo menos 10 caracteres");
            etDescricao.requestFocus();
            return false;
        }

        if (categoriaPosition == 0) {
            Toast.makeText(this, "Selecione uma categoria", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean validarSessao() {
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Erro: Sessão inválida", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        if (sessionManager.getUserId() <= 0) {
            Toast.makeText(this, "Erro: ID do usuário inválido", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // ========== FUNCIONALIDADE IA (mantida igual) ==========

    private void buscarPerguntasIA() {
        Log.d(TAG, "🤖 === BUSCAR PERGUNTAS IA - INÍCIO ===");

        String titulo = etTitulo.getText().toString().trim();

        if (titulo.isEmpty()) {
            Toast.makeText(this, "Digite um título primeiro", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoriaPosition = spinnerCategoria.getSelectedItemPosition();
        if (categoriaPosition == 0) {
            Toast.makeText(this, "Selecione uma categoria primeiro", Toast.LENGTH_SHORT).show();
            return;
        }

        String categoria = spinnerCategoria.getSelectedItem().toString();

        Log.d(TAG, "📝 Dados coletados:");
        Log.d(TAG, "   Título: " + titulo);
        Log.d(TAG, "   Categoria: " + categoria);

        cardPerguntasIA.setVisibility(View.VISIBLE);
        progressBarPerguntasIA.setVisibility(View.VISIBLE);
        tvPerguntasIA.setVisibility(View.GONE);
        etRespostasIA.setVisibility(View.GONE);
        btnGerarDescricao.setVisibility(View.GONE);
        btnAssistenteIA.setEnabled(false);

        String prompt = "Você é um assistente de helpdesk técnico. " +
                "Com base no seguinte problema de TI, gere EXATAMENTE 5 perguntas específicas e práticas " +
                "para ajudar o usuário a descrever melhor o problema. " +
                "As perguntas devem ser diretas e técnicas.\n\n" +
                "Categoria: " + categoria + "\n" +
                "Título do problema: " + titulo + "\n\n" +
                "Formato da resposta: Liste as 5 perguntas numeradas (1. 2. 3. 4. 5.) sem texto adicional.";

        Log.d(TAG, "📋 Prompt criado, montando request...");

        List<GroqRequest.Message> messages = new ArrayList<>();
        messages.add(new GroqRequest.Message("system",
                "Você é um especialista em suporte técnico de TI."));
        messages.add(new GroqRequest.Message("user", prompt));

        GroqRequest request = new GroqRequest(
                "llama-3.3-70b-versatile",
                messages,
                0.7,
                500
        );

        Log.d(TAG, "🚀 Criando GroqService...");

        GroqService service = GroqClient.getRetrofit().create(GroqService.class);

        Log.d(TAG, "📤 Enviando requisição para Groq API...");

        service.createChatCompletion(request).enqueue(new Callback<GroqResponse>() {
            @Override
            public void onResponse(Call<GroqResponse> call, Response<GroqResponse> response) {
                Log.d(TAG, "📥 === RESPOSTA RECEBIDA ===");
                Log.d(TAG, "   Código HTTP: " + response.code());
                Log.d(TAG, "   Mensagem: " + response.message());
                Log.d(TAG, "   Sucesso: " + response.isSuccessful());
                Log.d(TAG, "   Body null: " + (response.body() == null));

                btnAssistenteIA.setEnabled(true);
                progressBarPerguntasIA.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    GroqResponse groqResponse = response.body();

                    Log.d(TAG, "✅ Body não é null");
                    Log.d(TAG, "   Choices null: " + (groqResponse.getChoices() == null));
                    Log.d(TAG, "   Choices size: " + (groqResponse.getChoices() != null ? groqResponse.getChoices().size() : 0));

                    if (groqResponse.getChoices() != null && !groqResponse.getChoices().isEmpty()) {
                        perguntasGeradas = groqResponse.getChoices().get(0).getMessage().getContent();

                        Log.d(TAG, "✨ Perguntas geradas com sucesso!");
                        Log.d(TAG, "   Tamanho: " + perguntasGeradas.length() + " caracteres");
                        Log.d(TAG, "   Preview: " + perguntasGeradas.substring(0, Math.min(100, perguntasGeradas.length())));

                        exibirPerguntas(perguntasGeradas);
                    } else {
                        Log.e(TAG, "❌ Choices vazio ou null!");
                        exibirErroPergunta("Nenhuma pergunta gerada. Tente novamente.");
                    }
                } else {
                    Log.e(TAG, "❌ Resposta não foi bem-sucedida!");
                    Log.e(TAG, "   Código: " + response.code());

                    // ✅ LER O ERRO DA API
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "   Error Body: " + errorBody);
                            exibirErroPergunta("Erro da API: " + errorBody);
                        } else {
                            exibirErroPergunta("Erro ao gerar perguntas: " + response.message());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "   Erro ao ler errorBody", e);
                        exibirErroPergunta("Erro ao gerar perguntas: " + response.message());
                    }
                }
            }

            @Override
            public void onFailure(Call<GroqResponse> call, Throwable t) {
                Log.e(TAG, "❌ === FALHA NA REQUISIÇÃO ===");
                Log.e(TAG, "   Tipo de erro: " + t.getClass().getSimpleName());
                Log.e(TAG, "   Mensagem: " + t.getMessage());
                Log.e(TAG, "   Causa: " + (t.getCause() != null ? t.getCause().getMessage() : "null"));
                t.printStackTrace();

                btnAssistenteIA.setEnabled(true);
                progressBarPerguntasIA.setVisibility(View.GONE);
                exibirErroPergunta("Erro de conexão: " + t.getMessage());
            }
        });
    }

    private void exibirPerguntas(String perguntas) {
        tvPerguntasIA.setVisibility(View.VISIBLE);
        tvPerguntasIA.setText(perguntas);
        tvPerguntasIA.setTextColor(getResources().getColor(android.R.color.black));
        etRespostasIA.setVisibility(View.VISIBLE);
        btnGerarDescricao.setVisibility(View.VISIBLE);
    }

    private void exibirErroPergunta(String mensagem) {
        tvPerguntasIA.setVisibility(View.VISIBLE);
        tvPerguntasIA.setText("❌ " + mensagem);
        tvPerguntasIA.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
    }

    private void gerarDescricaoCompleta() {
        String respostas = etRespostasIA.getText().toString().trim();

        if (respostas.isEmpty()) {
            Toast.makeText(this, "Responda as perguntas primeiro", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBarPerguntasIA.setVisibility(View.VISIBLE);
        btnGerarDescricao.setEnabled(false);

        String prompt = "Com base nas seguintes perguntas e respostas, " +
                "gere uma descrição técnica completa e bem estruturada do problema:\n\n" +
                "PERGUNTAS:\n" + perguntasGeradas + "\n\n" +
                "RESPOSTAS DO USUÁRIO:\n" + respostas + "\n\n" +
                "Gere uma descrição detalhada e profissional do problema em um único parágrafo coeso.";

        List<GroqRequest.Message> messages = new ArrayList<>();
        messages.add(new GroqRequest.Message("system",
                "Você é um redator técnico especializado em documentação de TI."));
        messages.add(new GroqRequest.Message("user", prompt));

        GroqRequest request = new GroqRequest(
                "llama-3.3-70b-versatile",
                messages,
                0.5,
                800
        );

        GroqService service = GroqClient.getRetrofit().create(GroqService.class);
        service.createChatCompletion(request).enqueue(new Callback<GroqResponse>() {
            @Override
            public void onResponse(Call<GroqResponse> call, Response<GroqResponse> response) {
                btnGerarDescricao.setEnabled(true);
                progressBarPerguntasIA.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    GroqResponse groqResponse = response.body();
                    if (groqResponse.getChoices() != null && !groqResponse.getChoices().isEmpty()) {
                        String descricaoGerada = groqResponse.getChoices().get(0).getMessage().getContent();
                        etDescricao.setText(descricaoGerada);
                        cardPerguntasIA.setVisibility(View.GONE);
                        Toast.makeText(AbrirChamadoActivity.this,
                                "✨ Descrição gerada!", Toast.LENGTH_LONG).show();
                        etDescricao.requestFocus();
                    }
                } else {
                    Toast.makeText(AbrirChamadoActivity.this,
                            "Erro ao gerar descrição", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroqResponse> call, Throwable t) {
                btnGerarDescricao.setEnabled(true);
                progressBarPerguntasIA.setVisibility(View.GONE);
                Toast.makeText(AbrirChamadoActivity.this,
                        "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chamadoDAO != null) {
            chamadoDAO.close();
        }
    }
}