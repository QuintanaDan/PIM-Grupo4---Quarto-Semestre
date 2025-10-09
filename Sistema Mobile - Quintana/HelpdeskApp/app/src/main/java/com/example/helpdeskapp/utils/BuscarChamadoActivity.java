package com.example.helpdeskapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.helpdeskapp.adapters.ChamadoAdapter;
import com.example.helpdeskapp.dao.ChamadoDAO;
import com.example.helpdeskapp.models.Chamado;
import com.example.helpdeskapp.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class BuscarChamadoActivity extends AppCompatActivity {

    private EditText etBuscar;
    private RecyclerView rvResultados;
    private LinearLayout layoutSemResultados;
    private TextView tvMensagemBusca;

    private ChamadoAdapter adapter;
    private ChamadoDAO chamadoDAO;
    private SessionManager sessionManager;
    private List<Chamado> todosOsChamados;
    private List<Chamado> resultadosBusca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        etBuscar.requestFocus();
    }

    private void carregarChamados() {
        try {
            long clienteId = sessionManager.getUserId();
            todosOsChamados = chamadoDAO.listarChamadosPorCliente(clienteId);

            if (todosOsChamados == null) {
                todosOsChamados = new ArrayList<>();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao carregar chamados: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void realizarBusca(String termo) {
        resultadosBusca.clear();

        if (termo.trim().isEmpty()) {
            layoutSemResultados.setVisibility(View.VISIBLE);
            rvResultados.setVisibility(View.GONE);
            tvMensagemBusca.setText("Digite algo para buscar...");
            return;
        }

        String termoLower = termo.toLowerCase();

        for (Chamado chamado : todosOsChamados) {
            if (chamado.getTitulo().toLowerCase().contains(termoLower) ||
                    chamado.getDescricao().toLowerCase().contains(termoLower) ||
                    chamado.getNumero().toLowerCase().contains(termoLower) ||
                    chamado.getCategoria().toLowerCase().contains(termoLower)) {
                resultadosBusca.add(chamado);
            }
        }

        if (resultadosBusca.isEmpty()) {
            layoutSemResultados.setVisibility(View.VISIBLE);
            rvResultados.setVisibility(View.GONE);
            tvMensagemBusca.setText("Nenhum resultado encontrado para \"" + termo + "\"");
        } else {
            layoutSemResultados.setVisibility(View.GONE);
            rvResultados.setVisibility(View.VISIBLE);
            adapter.updateList(resultadosBusca);
        }
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
}