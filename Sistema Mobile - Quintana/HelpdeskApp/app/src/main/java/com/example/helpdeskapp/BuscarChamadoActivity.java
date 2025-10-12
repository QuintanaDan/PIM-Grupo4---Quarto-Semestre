package com.example.helpdeskapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.helpdeskapp.adapters.ChamadoAdapter;
import com.example.helpdeskapp.dao.ChamadoDAO;
import com.example.helpdeskapp.models.Chamado;
import com.example.helpdeskapp.utils.SessionManager;
import com.example.helpdeskapp.utils.ThemeManager;
import java.util.ArrayList;
import java.util.List;

public class BuscarChamadoActivity extends AppCompatActivity {
    private static final String TAG = "BuscarChamado";

    private EditText etBuscar;
    private RecyclerView rvResultados;
    private LinearLayout layoutSemResultados;
    private TextView tvMensagemBusca;
    private TextView tvContadorResultados;

    private ChamadoAdapter adapter;
    private ChamadoDAO chamadoDAO;
    private SessionManager sessionManager;
    private List<Chamado> todosOsChamados;
    private List<Chamado> resultadosBusca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ThemeManager(this).applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_chamado);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("🔍 Buscar Chamados");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inicializarComponentes();
        configurarRecyclerView();
        configurarBusca();
        carregarChamados();
    }

    private void inicializarComponentes() {
        etBuscar = findViewById(R.id.etBuscar);
        rvResultados = findViewById(R.id.rvResultados);
        layoutSemResultados = findViewById(R.id.layoutSemResultados);
        tvMensagemBusca = findViewById(R.id.tvMensagemBusca);
        tvContadorResultados = findViewById(R.id.tvContadorResultados);

        chamadoDAO = new ChamadoDAO(this);
        sessionManager = new SessionManager(this);
        todosOsChamados = new ArrayList<>();
        resultadosBusca = new ArrayList<>();
    }

    private void configurarRecyclerView() {
        rvResultados.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChamadoAdapter(this, resultadosBusca);

        adapter.setOnChamadoClickListener(new ChamadoAdapter.OnChamadoClickListener() {
            @Override
            public void onChamadoClick(Chamado chamado, int position) {
                abrirDetalhes(chamado);
            }

            @Override
            public void onChamadoLongClick(Chamado chamado, int position) {
                Toast.makeText(BuscarChamadoActivity.this,
                        "Chamado: " + chamado.getNumero(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        rvResultados.setAdapter(adapter);
    }

    private void configurarBusca() {
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                realizarBusca(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Foco automático no campo de busca
        etBuscar.requestFocus();
    }

    private void carregarChamados() {
        try {
            long clienteId = sessionManager.getUserId();
            todosOsChamados = chamadoDAO.listarChamadosPorCliente(clienteId);

            if (todosOsChamados == null) {
                todosOsChamados = new ArrayList<>();
            }

            Log.d(TAG, "Total de chamados carregados: " + todosOsChamados.size());

        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar chamados: ", e);
            Toast.makeText(this, "Erro ao carregar chamados: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void realizarBusca(String termo) {
        resultadosBusca.clear();

        if (termo.trim().isEmpty()) {
            mostrarMensagemInicial();
            return;
        }

        String termoLower = termo.toLowerCase().trim();

        // Buscar em múltiplos campos
        for (Chamado chamado : todosOsChamados) {
            if (contemTermo(chamado, termoLower)) {
                resultadosBusca.add(chamado);
            }
        }

        atualizarResultados(termo);
    }

    private boolean contemTermo(Chamado chamado, String termo) {
        // Buscar no número
        if (chamado.getNumero() != null &&
                chamado.getNumero().toLowerCase().contains(termo)) {
            return true;
        }

        // Buscar no título
        if (chamado.getTitulo() != null &&
                chamado.getTitulo().toLowerCase().contains(termo)) {
            return true;
        }

        // Buscar na descrição
        if (chamado.getDescricao() != null &&
                chamado.getDescricao().toLowerCase().contains(termo)) {
            return true;
        }

        // Buscar na categoria
        if (chamado.getCategoria() != null &&
                chamado.getCategoria().toLowerCase().contains(termo)) {
            return true;
        }

        // Buscar no status
        if (chamado.getStatus() != null &&
                chamado.getStatus().toLowerCase().contains(termo)) {
            return true;
        }

        // Buscar na prioridade
        if (chamado.getPrioridade() != null &&
                chamado.getPrioridade().toLowerCase().contains(termo)) {
            return true;
        }

        return false;
    }

    private void atualizarResultados(String termo) {
        if (resultadosBusca.isEmpty()) {
            mostrarSemResultados(termo);
        } else {
            mostrarResultados();
        }
    }

    private void mostrarMensagemInicial() {
        layoutSemResultados.setVisibility(View.VISIBLE);
        rvResultados.setVisibility(View.GONE);
        tvContadorResultados.setVisibility(View.GONE);
        tvMensagemBusca.setText("💡 Digite algo para buscar...\n\nVocê pode buscar por:\n• Número do chamado\n• Título\n• Descrição\n• Categoria\n• Status\n• Prioridade");
    }

    private void mostrarSemResultados(String termo) {
        layoutSemResultados.setVisibility(View.VISIBLE);
        rvResultados.setVisibility(View.GONE);
        tvContadorResultados.setVisibility(View.GONE);
        tvMensagemBusca.setText("😕 Nenhum resultado encontrado para:\n\"" + termo + "\"\n\nTente buscar por outra palavra-chave.");
    }

    private void mostrarResultados() {
        layoutSemResultados.setVisibility(View.GONE);
        rvResultados.setVisibility(View.VISIBLE);
        tvContadorResultados.setVisibility(View.VISIBLE);

        String texto = resultadosBusca.size() == 1 ?
                "✅ 1 chamado encontrado" :
                "✅ " + resultadosBusca.size() + " chamados encontrados";
        tvContadorResultados.setText(texto);

        adapter.updateList(resultadosBusca);
    }

    private void abrirDetalhes(Chamado chamado) {
        Intent intent = new Intent(this, DetalheChamadoActivity.class);
        intent.putExtra("chamado", chamado);
        intent.putExtra("chamado_id", chamado.getId());
        intent.putExtra("chamado_titulo", chamado.getTitulo());
        intent.putExtra("chamado_descricao", chamado.getDescricao());
        intent.putExtra("chamado_categoria", chamado.getCategoria());
        intent.putExtra("chamado_prioridade", chamado.getPrioridade());
        intent.putExtra("chamado_status", chamado.getStatus());
        intent.putExtra("chamado_data", chamado.getDataCriacaoFormatada());
        intent.putExtra("chamado_resposta", chamado.getResposta());
        startActivity(intent);
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