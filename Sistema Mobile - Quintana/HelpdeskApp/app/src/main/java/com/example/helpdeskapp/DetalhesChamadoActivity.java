package com.example.helpdeskapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.helpdeskapp.dao.ChamadoDAO;
import com.example.helpdeskapp.models.Chamado;

public class DetalhesChamadoActivity extends AppCompatActivity {

    private TextView tvNumeroChamado, tvStatusChamado, tvTitulo, tvDescricao;
    private TextView tvCategoria, tvPrioridade, tvDataAbertura, tvUltimaModificacao;
    private Button btnAdicionarComentario, btnAtualizarStatus;

    private Chamado chamado;
    private ChamadoDAO chamadoDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_chamado);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detalhes do Chamado");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inicializarComponentes();
        configurarEventos();

        chamadoDAO = new ChamadoDAO(this);

        carregarDadosChamado();
    }

    private void inicializarComponentes() {
        tvNumeroChamado = findViewById(R.id.tvNumeroChamado);
        tvStatusChamado = findViewById(R.id.tvStatusChamado);
        tvTitulo = findViewById(R.id.tvTitulo);
        tvDescricao = findViewById(R.id.tvDescricao);
        tvCategoria = findViewById(R.id.tvCategoria);
        tvPrioridade = findViewById(R.id.tvPrioridade);
        tvDataAbertura = findViewById(R.id.tvDataAbertura);
        tvUltimaModificacao = findViewById(R.id.tvUltimaModificacao);
        btnAdicionarComentario = findViewById(R.id.btnAdicionarComentario);
        btnAtualizarStatus = findViewById(R.id.btnAtualizarStatus);
    }

    private void configurarEventos() {
        btnAdicionarComentario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DetalhesChamadoActivity.this, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
            }
        });

        btnAtualizarStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DetalhesChamadoActivity.this, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void carregarDadosChamado() {
        try {
            // Pegar dados do Intent
            Intent intent = getIntent();
            long chamadoId = intent.getLongExtra("chamado_id", -1);
            String numeroChamado = intent.getStringExtra("numero_chamado");

            Log.d("DetalhesChamado", "Carregando chamado ID: " + chamadoId + ", Número: " + numeroChamado);

            if (chamadoId != -1) {
                // Buscar chamado no banco
                chamadoDAO.open();
                chamado = chamadoDAO.buscarChamadoPorId(chamadoId);
                chamadoDAO.close();

                if (chamado != null) {
                    preencherDados(chamado);
                } else {
                    Log.e("DetalhesChamado", "Chamado não encontrado!");
                    Toast.makeText(this, "Chamado não encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Log.e("DetalhesChamado", "ID do chamado inválido!");
                Toast.makeText(this, "Erro ao carregar chamado", Toast.LENGTH_SHORT).show();
                finish();
            }

        } catch (Exception e) {
            Log.e("DetalhesChamado", "Erro ao carregar dados: ", e);
            Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void preencherDados(Chamado chamado) {
        tvNumeroChamado.setText(chamado.getNumero());
        tvStatusChamado.setText("🟢 " + chamado.getStatusTexto());
        tvTitulo.setText(chamado.getTitulo());
        tvDescricao.setText(chamado.getDescricao());
        tvCategoria.setText(chamado.getCategoriaTextoCompleto());
        tvPrioridade.setText(chamado.getPrioridadeTextoCompleto());

        // Formatear datas
        if (chamado.getCreatedAt() != null && !chamado.getCreatedAt().isEmpty()) {
            String dataFormatada = formatarData(chamado.getCreatedAt());
            tvDataAbertura.setText(dataFormatada);
            tvUltimaModificacao.setText(dataFormatada); // Por enquanto é a mesma
        }

        Log.d("DetalhesChamado", "✅ Dados preenchidos com sucesso!");
    }

    private String formatarData(String dataCompleta) {
        try {
            // Formato: 2025-09-10 20:31:33 -> 10/09/2025 às 20:31
            String[] parts = dataCompleta.split(" ");
            if (parts.length >= 2) {
                String[] dateParts = parts[0].split("-");
                String[] timeParts = parts[1].split(":");

                if (dateParts.length >= 3 && timeParts.length >= 2) {
                    return dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0] +
                            " às " + timeParts[0] + ":" + timeParts[1];
                }
            }
        } catch (Exception e) {
            Log.e("DetalhesChamado", "Erro ao formatar data: ", e);
        }
        return dataCompleta;
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
            Log.e("DetalhesChamado", "Erro ao fechar DAO: ", e);
        }
    }
}
