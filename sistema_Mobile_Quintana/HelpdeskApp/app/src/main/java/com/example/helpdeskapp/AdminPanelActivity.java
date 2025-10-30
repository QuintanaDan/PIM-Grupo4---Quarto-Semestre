package com.example.helpdeskapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.helpdeskapp.adapters.ChamadoAdapter;
import com.example.helpdeskapp.dao.ChamadoDAO;
import com.example.helpdeskapp.database.DatabaseHelper;
import com.example.helpdeskapp.models.Chamado;
import com.example.helpdeskapp.utils.SessionManager;
import com.example.helpdeskapp.utils.PDFHelper;
import com.example.helpdeskapp.helpers.AuditoriaHelper;
import com.example.helpdeskapp.utils.ThemeManager;
import com.example.helpdeskapp.utils.NotificationHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdminPanelActivity extends AppCompatActivity {
    private static final String TAG = "AdminPanel";

    // Estatísticas
    private TextView tvTotalChamados, tvChamadosAbertos, tvChamadosAndamento, tvChamadosFechados;

    // Filtros
    private Button btnFiltroTodos, btnFiltroAbertos, btnFiltroAndamento, btnFiltroFechados;

    // Lista
    private RecyclerView rvTodosChamados;
    private TextView tvContadorResultados;
    private Button btnGerarRelatorio;

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private ChamadoAdapter adapter;
    private List<Chamado> todosChamados;
    private List<Chamado> chamadosFiltrados;
    private String filtroAtual = "TODOS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ThemeManager(this).applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        // Verificar se é admin
        sessionManager = new SessionManager(this);
        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "❌ Acesso negado! Área restrita a administradores.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_panel);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("👨‍💼 Painel Administrativo");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inicializarComponentes();
        configurarRecyclerView();
        configurarFiltros();
        carregarDados();
    }

    private void inicializarComponentes() {
        // Estatísticas
        tvTotalChamados = findViewById(R.id.tvTotalChamados);
        tvChamadosAbertos = findViewById(R.id.tvChamadosAbertos);
        tvChamadosAndamento = findViewById(R.id.tvChamadosAndamento);
        tvChamadosFechados = findViewById(R.id.tvChamadosFechados);

        // Filtros
        btnFiltroTodos = findViewById(R.id.btnFiltroTodos);
        btnFiltroAbertos = findViewById(R.id.btnFiltroAbertos);
        btnFiltroAndamento = findViewById(R.id.btnFiltroAndamento);
        btnFiltroFechados = findViewById(R.id.btnFiltroFechados);
        btnGerarRelatorio = findViewById(R.id.btnGerarRelatorio);

        // Lista
        rvTodosChamados = findViewById(R.id.rvTodosChamados);
        tvContadorResultados = findViewById(R.id.tvContadorResultados);

        dbHelper = new DatabaseHelper(this);
        todosChamados = new ArrayList<>();
        chamadosFiltrados = new ArrayList<>();
    }

    private void configurarRecyclerView() {
        rvTodosChamados.setLayoutManager(new LinearLayoutManager(this));
    }

    private void configurarFiltros() {
        btnFiltroTodos.setOnClickListener(v -> aplicarFiltro("TODOS"));
        btnFiltroAbertos.setOnClickListener(v -> aplicarFiltro("ABERTO"));
        btnFiltroAndamento.setOnClickListener(v -> aplicarFiltro("EM ANDAMENTO"));
        btnFiltroFechados.setOnClickListener(v -> aplicarFiltro("FECHADO"));
        btnGerarRelatorio.setOnClickListener(v -> mostrarOpcoesRelatorio());
    }

    private void carregarDados() {
        try {
            // Buscar TODOS os chamados do sistema
            todosChamados = buscarTodosChamados();

            // Calcular estatísticas
            calcularEstatisticas();

            // Aplicar filtro inicial
            aplicarFiltro(filtroAtual);

            Log.d(TAG, "✅ Dados carregados: " + todosChamados.size() + " chamados");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao carregar dados: ", e);
            Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
        }
    }

    private List<Chamado> buscarTodosChamados() {
        List<Chamado> chamados = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_CHAMADOS,
                    null, null, null, null, null,
                    DatabaseHelper.COLUMN_CHAMADO_CREATED_AT + " DESC"
            );

            while (cursor.moveToNext()) {
                Chamado chamado = criarChamadoFromCursor(cursor);
                if (chamado != null) {
                    chamados.add(chamado);
                }
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao buscar chamados: ", e);
        }

        return chamados;
    }

    private Chamado criarChamadoFromCursor(Cursor cursor) {
        try {
            Chamado chamado = new Chamado();
            chamado.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_ID)));

            int numeroIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CHAMADO_NUMERO);
            if (numeroIndex != -1) {
                chamado.setNumero(cursor.getString(numeroIndex));
            }

            chamado.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_TITULO)));
            chamado.setDescricao(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_DESCRICAO)));

            int categoriaIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORIA);
            if (categoriaIndex != -1) {
                chamado.setCategoria(cursor.getString(categoriaIndex));
            }

            chamado.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_STATUS)));
            chamado.setPrioridade(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_PRIORIDADE)));
            chamado.setClienteId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_CLIENTE_ID)));

            return chamado;
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao criar chamado: ", e);
            return null;
        }
    }

    private void calcularEstatisticas() {
        int total = todosChamados.size();
        int abertos = 0;
        int andamento = 0;
        int fechados = 0;

        for (Chamado chamado : todosChamados) {
            String status = chamado.getStatus().toUpperCase();

            if (status.contains("ABERTO") || status.equals("NOVO")) {
                abertos++;
            } else if (status.contains("ANDAMENTO") || status.contains("PROGRESS")) {
                andamento++;
            } else if (status.contains("FECHADO") || status.contains("RESOLVIDO")) {
                fechados++;
            }
        }

        tvTotalChamados.setText(String.valueOf(total));
        tvChamadosAbertos.setText(String.valueOf(abertos));
        tvChamadosAndamento.setText(String.valueOf(andamento));
        tvChamadosFechados.setText(String.valueOf(fechados));
    }

    private void aplicarFiltro(String filtro) {
        filtroAtual = filtro;
        atualizarBotoesFiltro();

        chamadosFiltrados.clear();

        if (filtro.equals("TODOS")) {
            chamadosFiltrados.addAll(todosChamados);
        } else {
            for (Chamado chamado : todosChamados) {
                String status = chamado.getStatus().toUpperCase();
                boolean corresponde = false;

                switch (filtro) {
                    case "ABERTO":
                        corresponde = status.contains("ABERTO") || status.equals("NOVO");
                        break;
                    case "EM ANDAMENTO":
                        corresponde = status.contains("ANDAMENTO") || status.contains("PROGRESS");
                        break;
                    case "FECHADO":
                        corresponde = status.contains("FECHADO") || status.contains("RESOLVIDO");
                        break;
                }

                if (corresponde) {
                    chamadosFiltrados.add(chamado);
                }
            }
        }

        atualizarLista();
    }

    private void atualizarBotoesFiltro() {
        // Resetar todos
        btnFiltroTodos.setBackgroundResource(R.drawable.botao_filtro_inativo);
        btnFiltroTodos.setTextColor(getResources().getColor(R.color.text_secondary));

        btnFiltroAbertos.setBackgroundResource(R.drawable.botao_filtro_inativo);
        btnFiltroAbertos.setTextColor(getResources().getColor(R.color.text_secondary));

        btnFiltroAndamento.setBackgroundResource(R.drawable.botao_filtro_inativo);
        btnFiltroAndamento.setTextColor(getResources().getColor(R.color.text_secondary));

        btnFiltroFechados.setBackgroundResource(R.drawable.botao_filtro_inativo);
        btnFiltroFechados.setTextColor(getResources().getColor(R.color.text_secondary));

        // Ativar o selecionado
        switch (filtroAtual) {
            case "TODOS":
                btnFiltroTodos.setBackgroundResource(R.drawable.botao_filtro_ativo);
                btnFiltroTodos.setTextColor(getResources().getColor(R.color.white));
                break;
            case "ABERTO":
                btnFiltroAbertos.setBackgroundResource(R.drawable.botao_filtro_ativo);
                btnFiltroAbertos.setTextColor(getResources().getColor(R.color.white));
                break;
            case "EM ANDAMENTO":
                btnFiltroAndamento.setBackgroundResource(R.drawable.botao_filtro_ativo);
                btnFiltroAndamento.setTextColor(getResources().getColor(R.color.white));
                break;
            case "FECHADO":
                btnFiltroFechados.setBackgroundResource(R.drawable.botao_filtro_ativo);
                btnFiltroFechados.setTextColor(getResources().getColor(R.color.white));
                break;
        }
    }

    private void atualizarLista() {
        if (adapter == null) {
            adapter = new ChamadoAdapter(this, chamadosFiltrados);

            adapter.setOnChamadoClickListener(new ChamadoAdapter.OnChamadoClickListener() {
                @Override
                public void onChamadoClick(Chamado chamado, int position) {
                    abrirDetalhesChamado(chamado);
                }

                @Override
                public void onChamadoLongClick(Chamado chamado, int position) {
                    mostrarOpcoesAdmin(chamado);
                }
            });

            rvTodosChamados.setAdapter(adapter);
        } else {
            adapter.updateList(chamadosFiltrados);
        }

        String texto = chamadosFiltrados.size() == 1 ?
                "1 chamado" :
                chamadosFiltrados.size() + " chamados";
        tvContadorResultados.setText(texto);
    }

    private void abrirDetalhesChamado(Chamado chamado) {
        Intent intent = new Intent(this, DetalheChamadoActivity.class);
        intent.putExtra("chamado", chamado);
        intent.putExtra("chamado_id", chamado.getId());
        intent.putExtra("chamado_titulo", chamado.getTitulo());
        intent.putExtra("chamado_descricao", chamado.getDescricao());
        intent.putExtra("chamado_categoria", chamado.getCategoria());
        intent.putExtra("chamado_prioridade", chamado.getPrioridade());
        intent.putExtra("chamado_status", chamado.getStatus());
        startActivity(intent);
    }

    private void mostrarOpcoesAdmin(Chamado chamado) {
        String[] opcoes = {
                "📋 Ver Detalhes",
                "✏️ Alterar Status",
                "❌ Cancelar"
        };

        new AlertDialog.Builder(this)
                .setTitle("Gerenciar Chamado #" + chamado.getNumero())
                .setItems(opcoes, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            abrirDetalhesChamado(chamado);
                            break;
                        case 1:
                            mostrarDialogoAlterarStatus(chamado);
                            break;
                    }
                })
                .show();
    }

    private void mostrarDialogoAlterarStatus(Chamado chamado) {
        String[] status = {"Aberto", "Em Andamento", "Resolvido", "Fechado"};

        new AlertDialog.Builder(this)
                .setTitle("Alterar Status")
                .setItems(status, (dialog, which) -> {
                    String novoStatus = status[which];
                    alterarStatus(chamado, novoStatus);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void alterarStatus(Chamado chamado, String novoStatus) {
        ChamadoDAO chamadoDAO = new ChamadoDAO(this);
        String statusAntigo = chamado.getStatus();
        boolean sucesso = chamadoDAO.atualizarStatus(chamado.getId(), novoStatus);

        if (sucesso) {
            // Registrar auditoria
            AuditoriaHelper.registrarAlteracaoStatus(
                    this,
                    sessionManager.getUserId(),
                    chamado.getId(),
                    statusAntigo,
                    novoStatus
            );

            // ATUALIZADO: Notificar cliente sobre mudança de status
            NotificationHelper notificationHelper = new NotificationHelper(this);
            notificationHelper.enviarNotificacaoMudancaStatus(
                    chamado.getClienteId(),
                    chamado.getId(),
                    chamado.getTitulo(),
                    novoStatus
            );

            Toast.makeText(this, "✅ Status alterado para: " + novoStatus,
                    Toast.LENGTH_SHORT).show();
            carregarDados();
        } else {
            Toast.makeText(this, "❌ Erro ao alterar status",
                    Toast.LENGTH_SHORT).show();
        }
    }

// ========== NOVO: FUNCIONALIDADES DE PDF ==========

    private void mostrarOpcoesRelatorio() {
        String[] opcoes = {
                "📄 Relatório de Todos os Chamados",
                "📊 Relatório de Chamados Abertos",
                "✅ Relatório de Chamados Resolvidos",
                "⏳ Relatório de Chamados em Andamento",
                "❌ Cancelar"
        };

        new AlertDialog.Builder(this)
                .setTitle("Gerar Relatório")
                .setItems(opcoes, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            gerarRelatorioPorFiltro("Todos", null);
                            break;
                        case 1:
                            gerarRelatorioPorFiltro("Abertos", "Aberto");
                            break;
                        case 2:
                            gerarRelatorioPorFiltro("Resolvidos", "Resolvido");
                            break;
                        case 3:
                            gerarRelatorioPorFiltro("Em Andamento", "Em Andamento");
                            break;
                    }
                })
                .show();
    }

    private void gerarRelatorioPorFiltro(String tipoRelatorio, String filtroStatus) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("📄 Gerando relatório...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                // Filtrar chamados
                List<Chamado> chamadosParaRelatorio;

                if (filtroStatus == null) {
                    chamadosParaRelatorio = todosChamados;
                } else {
                    chamadosParaRelatorio = new ArrayList<>();
                    for (Chamado chamado : todosChamados) {
                        if (chamado.getStatus().equalsIgnoreCase(filtroStatus) ||
                                chamado.getStatus().toLowerCase().contains(filtroStatus.toLowerCase())) {
                            chamadosParaRelatorio.add(chamado);
                        }
                    }
                }

                // Verificar se há chamados
                if (chamadosParaRelatorio.isEmpty()) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this,
                                "⚠️ Nenhum chamado encontrado para este filtro",
                                Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Gerar PDF
                String titulo = "RELATÓRIO DE CHAMADOS - " + tipoRelatorio.toUpperCase();
                File pdfFile = PDFHelper.gerarRelatorioGeral(
                        this,
                        chamadosParaRelatorio,
                        titulo
                );

                // Atualizar UI
                runOnUiThread(() -> {
                    progressDialog.dismiss();

                    if (pdfFile != null) {
                        mostrarDialogoSucessoRelatorio(pdfFile, chamadosParaRelatorio.size());
                    } else {
                        Toast.makeText(this, "❌ Erro ao gerar relatório",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "❌ Erro ao gerar relatório: ", e);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Erro: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void mostrarDialogoSucessoRelatorio(File pdfFile, int totalChamados) {
        new AlertDialog.Builder(this)
                .setTitle("✅ Relatório Gerado!")
                .setMessage("Relatório criado com sucesso!\n\n" +
                        "📊 Total de chamados: " + totalChamados + "\n" +
                        "📁 Arquivo: " + pdfFile.getName())
                .setPositiveButton("📂 Abrir", (dialog, which) -> {
                    PDFHelper.abrirPDF(this, pdfFile);
                })
                .setNeutralButton("📤 Compartilhar", (dialog, which) -> {
                    PDFHelper.compartilharPDF(this, pdfFile);
                })
                .setNegativeButton("OK", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDados();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}