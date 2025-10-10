package com.example.helpdeskapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.helpdeskapp.adapters.ComentarioAdapter;
import com.example.helpdeskapp.dao.AvaliacaoDAO;
import com.example.helpdeskapp.dao.ComentarioDAO;
import com.example.helpdeskapp.models.Avaliacao;
import com.example.helpdeskapp.models.Chamado;
import com.example.helpdeskapp.models.Comentario;
import com.example.helpdeskapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class DetalheChamadoActivity extends AppCompatActivity {
    private static final String TAG = "DetalheChamadoActivity";

    // Componentes da interface - Informações do Chamado
    private TextView txtTituloDetalhe;
    private TextView txtDescricaoDetalhe;
    private TextView txtCategoriaDetalhe;
    private TextView txtPrioridadeDetalhe;
    private TextView txtStatusDetalhe;
    private TextView txtDataDetalhe;
    private TextView txtRespostaDetalhe;

    // Botões
    private Button btnVoltar;
    private Button btnAvaliarChamado;

    // Componentes de Comentários
    private RecyclerView recyclerViewComentarios;
    private EditText etNovoComentario;
    private Button btnEnviarComentario;

    // Adapters e DAOs
    private ComentarioAdapter comentarioAdapter;
    private ComentarioDAO comentarioDAO;
    private AvaliacaoDAO avaliacaoDAO;
    private SessionManager sessionManager;

    // Dados
    private long chamadoId;
    private List<Comentario> listaComentarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_chamado);

        // Inicializar DAOs e Session
        comentarioDAO = new ComentarioDAO(this);
        avaliacaoDAO = new AvaliacaoDAO(this);
        sessionManager = new SessionManager(this);
        listaComentarios = new ArrayList<>();

        // Inicializar componentes
        inicializarViews();

        // Receber dados do Intent
        receberDadosChamado();

        // Configurar eventos
        configurarEventos();

        // Configurar ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detalhes do Chamado");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Carregar comentários
        carregarComentarios();
    }

    private void inicializarViews() {
        // Informações do Chamado
        txtTituloDetalhe = findViewById(R.id.txtTituloDetalhe);
        txtDescricaoDetalhe = findViewById(R.id.txtDescricaoDetalhe);
        txtCategoriaDetalhe = findViewById(R.id.txtCategoriaDetalhe);
        txtPrioridadeDetalhe = findViewById(R.id.txtPrioridadeDetalhe);
        txtStatusDetalhe = findViewById(R.id.txtStatusDetalhe);
        txtDataDetalhe = findViewById(R.id.txtDataDetalhe);
        txtRespostaDetalhe = findViewById(R.id.txtRespostaDetalhe);

        // Botões
        btnVoltar = findViewById(R.id.btnVoltar);
        btnAvaliarChamado = findViewById(R.id.btnAvaliarChamado);

        // Componentes de Comentários
        recyclerViewComentarios = findViewById(R.id.recyclerViewComentarios);
        etNovoComentario = findViewById(R.id.etNovoComentario);
        btnEnviarComentario = findViewById(R.id.btnEnviarComentario);

        // Configurar RecyclerView
        recyclerViewComentarios.setLayoutManager(new LinearLayoutManager(this));

        Log.d(TAG, "Views inicializadas com sucesso");
    }

    private void configurarEventos() {
        // Botão Voltar
        btnVoltar.setOnClickListener(v -> finish());

        // Botão Enviar Comentário
        btnEnviarComentario.setOnClickListener(v -> enviarComentario());

        Log.d(TAG, "Eventos configurados");
    }

    private void receberDadosChamado() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Tentar receber como objeto Chamado primeiro
            Chamado chamado = (Chamado) extras.getSerializable("chamado");

            if (chamado != null) {
                // Recebeu objeto completo
                preencherCamposChamado(chamado);
                chamadoId = chamado.getId();

                // Verificar se pode avaliar
                verificarSePodeAvaliar(chamado);

                Log.d(TAG, "Chamado recebido como objeto: ID=" + chamadoId);
            } else {
                // Receber campos individuais
                preencherCamposIndividuais(extras);
                chamadoId = extras.getLong("chamado_id", -1);

                // Verificar status para mostrar botão avaliar
                String status = extras.getString("chamado_status", "");
                mostrarBotaoAvaliar(status);

                Log.d(TAG, "Chamado recebido por campos: ID=" + chamadoId);
            }

            // Validar ID do chamado
            if (chamadoId <= 0) {
                Log.e(TAG, "❌ ID do chamado inválido: " + chamadoId);
                Toast.makeText(this, "Erro: ID do chamado inválido", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "❌ Nenhum dado recebido no Intent");
            Toast.makeText(this, "Erro ao carregar detalhes do chamado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void preencherCamposChamado(Chamado chamado) {
        try {
            // Título
            if (chamado.getTitulo() != null && !chamado.getTitulo().isEmpty()) {
                txtTituloDetalhe.setText(chamado.getTitulo());
            } else {
                txtTituloDetalhe.setText("Título não disponível");
            }

            // Descrição
            if (chamado.getDescricao() != null && !chamado.getDescricao().isEmpty()) {
                txtDescricaoDetalhe.setText(chamado.getDescricao());
            } else {
                txtDescricaoDetalhe.setText("Descrição não disponível");
            }

            // Categoria
            if (chamado.getCategoria() != null && !chamado.getCategoria().isEmpty()) {
                txtCategoriaDetalhe.setText(chamado.getCategoria());
            } else {
                txtCategoriaDetalhe.setText("Não especificada");
            }

            // Prioridade
            if (chamado.getPrioridade() != null && !chamado.getPrioridade().isEmpty()) {
                txtPrioridadeDetalhe.setText(chamado.getPrioridade());
            } else {
                txtPrioridadeDetalhe.setText("Não definida");
            }

            // Status
            if (chamado.getStatus() != null && !chamado.getStatus().isEmpty()) {
                txtStatusDetalhe.setText(chamado.getStatus());
            } else {
                txtStatusDetalhe.setText("Status não disponível");
            }

            // Data
            if (chamado.getDataCriacaoFormatada() != null) {
                txtDataDetalhe.setText(chamado.getDataCriacaoFormatada());
            } else {
                txtDataDetalhe.setText("Data não disponível");
            }

            // Resposta
            if (chamado.getResposta() != null && !chamado.getResposta().isEmpty()) {
                txtRespostaDetalhe.setText(chamado.getResposta());
            } else {
                txtRespostaDetalhe.setText("Aguardando resposta...");
            }

            Log.d(TAG, "✅ Campos preenchidos com sucesso");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao preencher campos: ", e);
            Toast.makeText(this, "Erro ao exibir informações", Toast.LENGTH_SHORT).show();
        }
    }

    private void preencherCamposIndividuais(Bundle extras) {
        try {
            String titulo = extras.getString("chamado_titulo", "");
            String descricao = extras.getString("chamado_descricao", "");
            String categoria = extras.getString("chamado_categoria", "Não especificada");
            String prioridade = extras.getString("chamado_prioridade", "");
            String status = extras.getString("chamado_status", "");
            String data = extras.getString("chamado_data", "");
            String resposta = extras.getString("chamado_resposta", "");

            // Preencher TextViews
            txtTituloDetalhe.setText(!titulo.isEmpty() ? titulo : "Título não disponível");
            txtDescricaoDetalhe.setText(!descricao.isEmpty() ? descricao : "Descrição não disponível");
            txtCategoriaDetalhe.setText(categoria);
            txtPrioridadeDetalhe.setText(!prioridade.isEmpty() ? prioridade : "Não definida");
            txtStatusDetalhe.setText(!status.isEmpty() ? status : "Status não disponível");
            txtDataDetalhe.setText(!data.isEmpty() ? data : "Data não disponível");

            if (resposta != null && !resposta.isEmpty()) {
                txtRespostaDetalhe.setText(resposta);
            } else {
                txtRespostaDetalhe.setText("Aguardando resposta...");
            }

            Log.d(TAG, "✅ Campos individuais preenchidos com sucesso");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao preencher campos individuais: ", e);
            Toast.makeText(this, "Erro ao exibir informações", Toast.LENGTH_SHORT).show();
        }
    }

    private void carregarComentarios() {
        if (chamadoId > 0) {
            try {
                Log.d(TAG, "=== CARREGANDO COMENTÁRIOS ===");
                Log.d(TAG, "Chamado ID: " + chamadoId);

                listaComentarios = comentarioDAO.buscarComentariosPorChamado(chamadoId);

                if (listaComentarios == null) {
                    listaComentarios = new ArrayList<>();
                    Log.w(TAG, "⚠️ Lista de comentários retornou null, criando lista vazia");
                }

                Log.d(TAG, "Total de comentários carregados: " + listaComentarios.size());

                // Configurar adapter
                comentarioAdapter = new ComentarioAdapter(listaComentarios, this);
                recyclerViewComentarios.setAdapter(comentarioAdapter);

                if (listaComentarios.isEmpty()) {
                    Log.d(TAG, "ℹ️ Nenhum comentário encontrado para este chamado");
                } else {
                    Log.d(TAG, "✅ Comentários carregados e exibidos com sucesso");
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Erro ao carregar comentários: ", e);
                Toast.makeText(this, "Erro ao carregar comentários", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "❌ ID do chamado inválido ao carregar comentários: " + chamadoId);
        }
    }

    private void enviarComentario() {
        String textoComentario = etNovoComentario.getText().toString().trim();

        // Validar campo
        if (textoComentario.isEmpty()) {
            etNovoComentario.setError("Digite um comentário");
            etNovoComentario.requestFocus();
            return;
        }

        // Validar ID do chamado
        if (chamadoId <= 0) {
            Toast.makeText(this, "Erro: ID do chamado inválido", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ ID do chamado inválido: " + chamadoId);
            return;
        }

        try {
            Log.d(TAG, "=== ENVIANDO COMENTÁRIO ===");
            Log.d(TAG, "Chamado ID: " + chamadoId);
            Log.d(TAG, "Usuário ID: " + sessionManager.getUserId());
            Log.d(TAG, "Texto: " + textoComentario);

            // Criar novo comentário
            Comentario novoComentario = new Comentario();
            novoComentario.setChamadoId(chamadoId);
            novoComentario.setUsuarioId(sessionManager.getUserId());
            novoComentario.setTexto(textoComentario);
            novoComentario.setTipo("user");
            novoComentario.setNomeUsuario(sessionManager.getUserName());

            // Salvar no banco
            long resultado = comentarioDAO.inserirComentario(novoComentario);

            if (resultado > 0) {
                Log.d(TAG, "✅ Comentário inserido com sucesso! ID: " + resultado);

                // Limpar campo
                etNovoComentario.setText("");

                // Recarregar comentários
                carregarComentarios();

                // Scroll para o último comentário
                if (listaComentarios.size() > 0) {
                    recyclerViewComentarios.smoothScrollToPosition(listaComentarios.size() - 1);
                }

                Toast.makeText(this, "✅ Comentário adicionado!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "❌ Falha ao inserir comentário. Resultado: " + resultado);
                Toast.makeText(this, "❌ Erro ao adicionar comentário", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro crítico ao enviar comentário: ", e);
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ========== MÉTODOS DE AVALIAÇÃO ==========

    private void verificarSePodeAvaliar(Chamado chamado) {
        try {
            String status = chamado.getStatus().toLowerCase();
            boolean resolvido = status.contains("resolvido") || status.contains("fechado");
            boolean jaAvaliado = avaliacaoDAO.chamadoJaAvaliado(chamadoId);

            Log.d(TAG, "=== VERIFICAÇÃO DE AVALIAÇÃO ===");
            Log.d(TAG, "Status: " + status);
            Log.d(TAG, "Resolvido: " + resolvido);
            Log.d(TAG, "Já avaliado: " + jaAvaliado);

            if (resolvido && !jaAvaliado) {
                btnAvaliarChamado.setVisibility(View.VISIBLE);
                btnAvaliarChamado.setOnClickListener(v -> abrirTelaAvaliacao());
                Log.d(TAG, "✅ Botão de avaliar habilitado");
            } else {
                btnAvaliarChamado.setVisibility(View.GONE);
                Log.d(TAG, "ℹ️ Botão de avaliar desabilitado");
            }

            // Se já foi avaliado, mostrar avaliação existente
            if (jaAvaliado) {
                mostrarAvaliacaoExistente();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao verificar se pode avaliar: ", e);
            btnAvaliarChamado.setVisibility(View.GONE);
        }
    }

    private void mostrarBotaoAvaliar(String status) {
        if (status != null && !status.isEmpty()) {
            try {
                String statusLower = status.toLowerCase();
                boolean resolvido = statusLower.contains("resolvido") || statusLower.contains("fechado");
                boolean jaAvaliado = avaliacaoDAO.chamadoJaAvaliado(chamadoId);

                Log.d(TAG, "=== MOSTRAR BOTÃO AVALIAR ===");
                Log.d(TAG, "Status: " + status);
                Log.d(TAG, "Resolvido: " + resolvido);
                Log.d(TAG, "Já avaliado: " + jaAvaliado);

                if (resolvido && !jaAvaliado) {
                    btnAvaliarChamado.setVisibility(View.VISIBLE);
                    btnAvaliarChamado.setOnClickListener(v -> abrirTelaAvaliacao());
                    Log.d(TAG, "✅ Botão de avaliar habilitado");
                } else {
                    btnAvaliarChamado.setVisibility(View.GONE);
                    Log.d(TAG, "ℹ️ Botão de avaliar desabilitado");
                }

                // Se já foi avaliado, mostrar avaliação
                if (jaAvaliado) {
                    mostrarAvaliacaoExistente();
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Erro ao mostrar botão avaliar: ", e);
                btnAvaliarChamado.setVisibility(View.GONE);
            }
        } else {
            btnAvaliarChamado.setVisibility(View.GONE);
            Log.w(TAG, "⚠️ Status não fornecido, botão avaliar oculto");
        }
    }

    private void abrirTelaAvaliacao() {
        try {
            Log.d(TAG, "Abrindo tela de avaliação para chamado ID: " + chamadoId);

            Intent intent = new Intent(this, AvaliarChamadoActivity.class);
            intent.putExtra("chamado_id", chamadoId);
            intent.putExtra("chamado_titulo", txtTituloDetalhe.getText().toString());
            startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao abrir tela de avaliação: ", e);
            Toast.makeText(this, "Erro ao abrir avaliação", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarAvaliacaoExistente() {
        try {
            Avaliacao avaliacao = avaliacaoDAO.buscarAvaliacaoPorChamado(chamadoId);

            if (avaliacao != null) {
                String mensagem = "✅ Avaliado: " + avaliacao.getEstrelas() +
                        "\n" + avaliacao.getNotaTexto();

                if (avaliacao.getComentario() != null && !avaliacao.getComentario().isEmpty()) {
                    mensagem += "\n💬 " + avaliacao.getComentario();
                }

                Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
                Log.d(TAG, "✅ Avaliação existente exibida: " + avaliacao.getNota() + " estrelas");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao mostrar avaliação existente: ", e);
        }
    }

    // ========== LIFECYCLE METHODS ==========

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity retomada");

        // Recarregar comentários quando voltar
        if (chamadoId > 0) {
            carregarComentarios();

            // Verificar novamente se pode avaliar (caso tenha acabado de avaliar)
            if (avaliacaoDAO != null) {
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    String status = extras.getString("chamado_status", "");
                    mostrarBotaoAvaliar(status);
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "Navegação para trás");
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Activity destruída");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Activity pausada");
    }
}