package com.example.helpdeskapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.helpdeskapp.adapters.ChamadoAdapter;
import com.example.helpdeskapp.dao.ChamadoDAO;
import com.example.helpdeskapp.models.Chamado;
import com.example.helpdeskapp.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class MeusChamadosActivity extends AppCompatActivity {
    private static final String TAG = "MeusChamados";

    private RecyclerView rvChamados;
    private LinearLayout layoutSemChamados;
    private Button btnAbrirPrimeiroChamado;
    private TextView tvContadorChamados;

    // BOTÕES DE FILTRO
    private Button btnFiltroTodos, btnFiltroAbertos, btnFiltroAndamento, btnFiltroFechados;

    private SessionManager sessionManager;
    private ChamadoDAO chamadoDAO;
    private ChamadoAdapter adapter;
    private List<Chamado> todosOsChamados;
    private List<Chamado> chamadosFiltrados;
    private String filtroAtual = "TODOS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_chamados);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Meus Chamados");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inicializarComponentes();
        configurarRecyclerView();
        configurarEventos();
        configurarFiltros();

        sessionManager = new SessionManager(this);
        chamadoDAO = new ChamadoDAO(this);

        carregarChamados();
    }

    private void inicializarComponentes() {
        rvChamados = findViewById(R.id.rvChamados);
        layoutSemChamados = findViewById(R.id.layoutSemChamados);
        btnAbrirPrimeiroChamado = findViewById(R.id.btnAbrirPrimeiroChamado);
        tvContadorChamados = findViewById(R.id.tvContadorChamados);

        // BOTÕES DE FILTRO
        btnFiltroTodos = findViewById(R.id.btnFiltroTodos);
        btnFiltroAbertos = findViewById(R.id.btnFiltroAbertos);
        btnFiltroAndamento = findViewById(R.id.btnFiltroAndamento);
        btnFiltroFechados = findViewById(R.id.btnFiltroFechados);

        todosOsChamados = new ArrayList<>();
        chamadosFiltrados = new ArrayList<>();
    }

    private void configurarRecyclerView() {
        rvChamados.setLayoutManager(new LinearLayoutManager(this));
    }

    private void configurarEventos() {
        btnAbrirPrimeiroChamado.setOnClickListener(v -> {
            Intent intent = new Intent(this, AbrirChamadoActivity.class);
            startActivity(intent);
        });
    }

    private void configurarFiltros() {
        btnFiltroTodos.setOnClickListener(v -> aplicarFiltro("TODOS"));
        btnFiltroAbertos.setOnClickListener(v -> aplicarFiltro("ABERTO"));
        btnFiltroAndamento.setOnClickListener(v -> aplicarFiltro("EM ANDAMENTO"));
        btnFiltroFechados.setOnClickListener(v -> aplicarFiltro("FECHADO"));
    }

    private void aplicarFiltro(String filtro) {
        filtroAtual = filtro;
        atualizarBotoesFiltro();

        chamadosFiltrados.clear();

        if (filtro.equals("TODOS")) {
            chamadosFiltrados.addAll(todosOsChamados);
        } else {
            for (Chamado chamado : todosOsChamados) {
                String status = chamado.getStatus().toUpperCase();

                boolean corresponde = false;

                switch (filtro) {
                    case "ABERTO":
                        corresponde = status.contains("ABERTO") || status.equals("NOVO") || status.equals("OPEN");
                        break;
                    case "EM ANDAMENTO":
                        corresponde = status.contains("ANDAMENTO") || status.contains("PROGRESS") || status.equals("PROGRESSO");
                        break;
                    case "FECHADO":
                        corresponde = status.contains("FECHADO") || status.contains("RESOLVIDO") ||
                                status.equals("CLOSED") || status.equals("RESOLVED");
                        break;
                }

                if (corresponde) {
                    chamadosFiltrados.add(chamado);
                }
            }
        }

        atualizarContador();

        if (adapter != null) {
            adapter.updateList(chamadosFiltrados);
        }

        String mensagem = chamadosFiltrados.size() == 1 ?
                "1 chamado encontrado" :
                chamadosFiltrados.size() + " chamados encontrados";
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();

        Log.d(TAG, "Filtro aplicado: " + filtro + " - Resultados: " + chamadosFiltrados.size());
    }

    private void atualizarBotoesFiltro() {
        // Resetar todos para inativo
        btnFiltroTodos.setBackgroundResource(R.drawable.botao_filtro_inativo);
        btnFiltroAbertos.setBackgroundResource(R.drawable.botao_filtro_inativo);
        btnFiltroAndamento.setBackgroundResource(R.drawable.botao_filtro_inativo);
        btnFiltroFechados.setBackgroundResource(R.drawable.botao_filtro_inativo);

        // Ativar o selecionado
        switch (filtroAtual) {
            case "TODOS":
                btnFiltroTodos.setBackgroundResource(R.drawable.botao_filtro_ativo);
                break;
            case "ABERTO":
                btnFiltroAbertos.setBackgroundResource(R.drawable.botao_filtro_ativo);
                break;
            case "EM ANDAMENTO":
                btnFiltroAndamento.setBackgroundResource(R.drawable.botao_filtro_ativo);
                break;
            case "FECHADO":
                btnFiltroFechados.setBackgroundResource(R.drawable.botao_filtro_ativo);
                break;
        }
    }

    private void atualizarContador() {
        if (tvContadorChamados != null) {
            String texto = chamadosFiltrados.size() == 1 ?
                    "1 chamado" :
                    chamadosFiltrados.size() + " chamados";
            tvContadorChamados.setText(texto);
        }
    }

    private void carregarChamados() {
        Log.d(TAG, "=== INICIANDO CARREGAMENTO ===");

        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            Log.e(TAG, "❌ SessionManager inválido ou usuário não logado!");
            mostrarMensagemVazia();
            return;
        }

        long clienteId = sessionManager.getUserId();
        Log.d(TAG, "Cliente ID obtido: " + clienteId);

        if (clienteId <= 0) {
            Log.e(TAG, "❌ Cliente ID inválido: " + clienteId);
            mostrarMensagemVazia();
            return;
        }

        if (chamadoDAO == null) {
            Log.e(TAG, "❌ ChamadoDAO é null!");
            mostrarMensagemVazia();
            return;
        }

        try {
            chamadoDAO.debugInfo();
            todosOsChamados = chamadoDAO.listarChamadosPorCliente(clienteId);

            if (todosOsChamados != null && !todosOsChamados.isEmpty()) {
                Log.d(TAG, "✅ CHAMADOS ENCONTRADOS: " + todosOsChamados.size());
                aplicarFiltro(filtroAtual);
                mostrarListaChamados(chamadosFiltrados);
            } else {
                Log.w(TAG, "❌ NENHUM CHAMADO ENCONTRADO");
                mostrarMensagemVazia();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO AO CARREGAR CHAMADOS: ", e);
            Toast.makeText(this, "Erro ao carregar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            mostrarMensagemVazia();
        }
    }

    private void mostrarListaChamados(List<Chamado> chamados) {
        Log.d(TAG, "=== MOSTRAR LISTA CHAMADOS ===");
        Log.d(TAG, "Recebeu " + chamados.size() + " chamados");

        try {
            layoutSemChamados.setVisibility(View.GONE);
            rvChamados.setVisibility(View.VISIBLE);

            adapter = new ChamadoAdapter(this, chamados);

            adapter.setOnChamadoClickListener(new ChamadoAdapter.OnChamadoClickListener() {
                @Override
                public void onChamadoClick(Chamado chamado, int position) {
                    abrirDetalhesChamado(chamado);
                }

                @Override
                public void onChamadoLongClick(Chamado chamado, int position) {
                    Toast.makeText(MeusChamadosActivity.this,
                            "Chamado: " + chamado.getNumero(),
                            Toast.LENGTH_SHORT).show();
                }
            });

            rvChamados.setAdapter(adapter);
            atualizarContador();
            Log.d(TAG, "✅ RecyclerView configurado com sucesso!");

        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO ao configurar RecyclerView: ", e);
        }
    }

    private void abrirDetalhesChamado(Chamado chamado) {
        Intent intent = new Intent(this, DetalheChamadoActivity.class);
        intent.putExtra("chamado", chamado);
        intent.putExtra("chamado_id", chamado.getId());
        intent.putExtra("chamado_protocolo", chamado.getProtocoloFormatado());
        intent.putExtra("chamado_titulo", chamado.getTitulo());
        intent.putExtra("chamado_descricao", chamado.getDescricao());
        intent.putExtra("chamado_categoria", chamado.getCategoria());
        intent.putExtra("chamado_prioridade", chamado.getPrioridade());
        intent.putExtra("chamado_status", chamado.getStatus());
        intent.putExtra("chamado_data", chamado.getDataCriacaoFormatada());
        intent.putExtra("chamado_resposta", chamado.getResposta());
        startActivity(intent);
    }

    private void mostrarMensagemVazia() {
        Log.d(TAG, "Mostrando mensagem de lista vazia");
        rvChamados.setVisibility(View.GONE);
        layoutSemChamados.setVisibility(View.VISIBLE);
        if (tvContadorChamados != null) {
            tvContadorChamados.setText("0 chamados");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarChamados();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Activity destruída");
    }
}