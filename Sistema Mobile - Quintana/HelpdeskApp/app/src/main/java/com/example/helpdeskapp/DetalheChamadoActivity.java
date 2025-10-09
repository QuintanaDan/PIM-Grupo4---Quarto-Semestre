package com.example.helpdeskapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.helpdeskapp.adapters.ComentarioAdapter;
import com.example.helpdeskapp.dao.ComentarioDAO;
import com.example.helpdeskapp.models.Chamado;
import com.example.helpdeskapp.models.Comentario;
import com.example.helpdeskapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class DetalheChamadoActivity extends AppCompatActivity {
    private static final String TAG = "DetalheChamadoActivity";

    private TextView txtTituloDetalhe;
    private TextView txtDescricaoDetalhe;
    private TextView txtCategoriaDetalhe;
    private TextView txtPrioridadeDetalhe;
    private TextView txtStatusDetalhe;
    private TextView txtDataDetalhe;
    private TextView txtRespostaDetalhe;
    private Button btnVoltar;
    private RecyclerView recyclerViewComentarios;

    // NOVOS COMPONENTES
    private EditText etNovoComentario;
    private Button btnEnviarComentario;

    private ComentarioAdapter comentarioAdapter;
    private ComentarioDAO comentarioDAO;
    private SessionManager sessionManager;
    private long chamadoId;
    private List<Comentario> listaComentarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_chamado);

        comentarioDAO = new ComentarioDAO(this);
        sessionManager = new SessionManager(this);
        listaComentarios = new ArrayList<>();

        inicializarViews();
        receberDadosChamado();
        configurarEventos();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detalhes do Chamado");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        carregarComentarios();
    }

    private void inicializarViews() {
        txtTituloDetalhe = findViewById(R.id.txtTituloDetalhe);
        txtDescricaoDetalhe = findViewById(R.id.txtDescricaoDetalhe);
        txtCategoriaDetalhe = findViewById(R.id.txtCategoriaDetalhe);
        txtPrioridadeDetalhe = findViewById(R.id.txtPrioridadeDetalhe);
        txtStatusDetalhe = findViewById(R.id.txtStatusDetalhe);
        txtDataDetalhe = findViewById(R.id.txtDataDetalhe);
        txtRespostaDetalhe = findViewById(R.id.txtRespostaDetalhe);
        btnVoltar = findViewById(R.id.btnVoltar);
        recyclerViewComentarios = findViewById(R.id.recyclerViewComentarios);

        // NOVOS COMPONENTES
        etNovoComentario = findViewById(R.id.etNovoComentario);
        btnEnviarComentario = findViewById(R.id.btnEnviarComentario);

        recyclerViewComentarios.setLayoutManager(new LinearLayoutManager(this));
    }

    private void configurarEventos() {
        btnVoltar.setOnClickListener(v -> finish());

        // NOVO: Enviar comentário
        btnEnviarComentario.setOnClickListener(v -> enviarComentario());
    }

    private void receberDadosChamado() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Chamado chamado = (Chamado) extras.getSerializable("chamado");

            if (chamado != null) {
                preencherCamposChamado(chamado);
                chamadoId = chamado.getId();
            } else {
                preencherCamposIndividuais(extras);
                chamadoId = extras.getLong("chamado_id", -1);
            }
        } else {
            Toast.makeText(this, "Erro ao carregar detalhes do chamado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void preencherCamposChamado(Chamado chamado) {
        txtTituloDetalhe.setText(chamado.getTitulo());
        txtDescricaoDetalhe.setText(chamado.getDescricao());
        txtCategoriaDetalhe.setText(chamado.getCategoria() != null ? chamado.getCategoria() : "Não especificada");
        txtPrioridadeDetalhe.setText(chamado.getPrioridade());
        txtStatusDetalhe.setText(chamado.getStatus());
        txtDataDetalhe.setText(chamado.getDataCriacaoFormatada());

        if (chamado.getResposta() != null && !chamado.getResposta().isEmpty()) {
            txtRespostaDetalhe.setText(chamado.getResposta());
        } else {
            txtRespostaDetalhe.setText("Aguardando resposta...");
        }
    }

    private void preencherCamposIndividuais(Bundle extras) {
        String titulo = extras.getString("chamado_titulo", "");
        String descricao = extras.getString("chamado_descricao", "");
        String categoria = extras.getString("chamado_categoria", "Não especificada");
        String prioridade = extras.getString("chamado_prioridade", "");
        String status = extras.getString("chamado_status", "");
        String data = extras.getString("chamado_data", "");
        String resposta = extras.getString("chamado_resposta", "");

        txtTituloDetalhe.setText(titulo);
        txtDescricaoDetalhe.setText(descricao);
        txtCategoriaDetalhe.setText(categoria);
        txtPrioridadeDetalhe.setText(prioridade);
        txtStatusDetalhe.setText(status);
        txtDataDetalhe.setText(data);

        if (resposta != null && !resposta.isEmpty()) {
            txtRespostaDetalhe.setText(resposta);
        } else {
            txtRespostaDetalhe.setText("Aguardando resposta...");
        }
    }

    private void carregarComentarios() {
        if (chamadoId > 0) {
            try {
                listaComentarios = comentarioDAO.buscarComentariosPorChamado(chamadoId);

                if (listaComentarios == null) {
                    listaComentarios = new ArrayList<>();
                }

                comentarioAdapter = new ComentarioAdapter(listaComentarios, this);
                recyclerViewComentarios.setAdapter(comentarioAdapter);

                Log.d(TAG, "Comentários carregados: " + listaComentarios.size());

            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar comentários: ", e);
                Toast.makeText(this, "Erro ao carregar comentários", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // NOVO MÉTODO: Enviar comentário
    private void enviarComentario() {
        String textoComentario = etNovoComentario.getText().toString().trim();

        if (textoComentario.isEmpty()) {
            etNovoComentario.setError("Digite um comentário");
            etNovoComentario.requestFocus();
            return;
        }

        if (chamadoId <= 0) {
            Toast.makeText(this, "Erro: ID do chamado inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
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
                Log.d(TAG, "✅ Comentário inserido com ID: " + resultado);

                // Limpar campo
                etNovoComentario.setText("");

                // Recarregar comentários
                carregarComentarios();

                // Scroll para o último comentário
                recyclerViewComentarios.smoothScrollToPosition(listaComentarios.size() - 1);

                Toast.makeText(this, "✅ Comentário adicionado!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Erro ao adicionar comentário", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao enviar comentário: ", e);
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}