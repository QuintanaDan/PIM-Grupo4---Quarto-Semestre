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
import android.widget.Toast;

import com.example.helpdeskapp.adapters.ChamadoAdapter;
import com.example.helpdeskapp.dao.ChamadoDAO;
import com.example.helpdeskapp.models.Chamado;
import com.example.helpdeskapp.utils.SessionManager;

import java.util.List;

public class MeusChamadosActivity extends AppCompatActivity {

    private RecyclerView rvChamados;
    private LinearLayout layoutSemChamados;
    private Button btnAbrirPrimeiroChamado;

    private SessionManager sessionManager;
    private ChamadoDAO chamadoDAO;
    private ChamadoAdapter adapter;

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

        sessionManager = new SessionManager(this);
        chamadoDAO = new ChamadoDAO(this);

        carregarChamados();
    }

    private void inicializarComponentes() {
        rvChamados = findViewById(R.id.rvChamados);
        layoutSemChamados = findViewById(R.id.layoutSemChamados);
        btnAbrirPrimeiroChamado = findViewById(R.id.btnAbrirPrimeiroChamado);
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

    private void carregarChamados() {
        Log.d("MeusChamados", "=== CARREGANDO CHAMADOS ===");

        try {
            long clienteId = sessionManager.getUserId();

            chamadoDAO.open();
            List<Chamado> chamados = chamadoDAO.listarChamadosPorCliente(clienteId);
            chamadoDAO.close();

            if (chamados != null && !chamados.isEmpty()) {
                Log.d("MeusChamados", "✅ " + chamados.size() + " chamados encontrados");
                mostrarListaChamados(chamados);
            } else {
                Log.d("MeusChamados", "⚠️ Nenhum chamado encontrado");
                mostrarMensagemVazia();
            }

        } catch (Exception e) {
            Log.e("MeusChamados", "❌ Erro ao carregar chamados: ", e);
            Toast.makeText(this, "Erro ao carregar chamados", Toast.LENGTH_SHORT).show();
            mostrarMensagemVazia();
        }
    }

    private void mostrarListaChamados(List<Chamado> chamados) {
        layoutSemChamados.setVisibility(View.GONE);
        rvChamados.setVisibility(View.VISIBLE);

        adapter = new ChamadoAdapter(this, chamados);
        adapter.setOnChamadoClickListener(chamado -> {
            // Por enquanto só mostra toast
            Toast.makeText(this, "Chamado: " + chamado.getNumero(), Toast.LENGTH_SHORT).show();
        });

        rvChamados.setAdapter(adapter);
    }

    private void mostrarMensagemVazia() {
        rvChamados.setVisibility(View.GONE);
        layoutSemChamados.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarChamados(); // Recarregar quando voltar
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (chamadoDAO != null) {
                chamadoDAO.close();
            }
        } catch (Exception e) {
            Log.e("MeusChamados", "Erro ao fechar DAO: ", e);
        }
    }
}
