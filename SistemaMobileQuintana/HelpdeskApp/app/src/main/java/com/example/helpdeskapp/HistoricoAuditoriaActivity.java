package com.example.helpdeskapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.helpdeskapp.adapters.AuditoriaAdapter;
import com.example.helpdeskapp.dao.AuditoriaDAO;
import com.example.helpdeskapp.models.Auditoria;
import com.example.helpdeskapp.utils.SessionManager;
import com.example.helpdeskapp.utils.ThemeManager;
import java.util.ArrayList;
import java.util.List;

public class HistoricoAuditoriaActivity extends AppCompatActivity {
    private static final String TAG = "HistoricoAuditoria";

    private RecyclerView recyclerViewAuditoria;
    private TextView tvContadorAcoes;
    private Button btnFiltrar, btnLimparLogs;
    private AuditoriaAdapter auditoriaAdapter;
    private AuditoriaDAO auditoriaDAO;
    private SessionManager sessionManager;
    private List<Auditoria> listaAuditorias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ThemeManager(this).applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_auditoria);

        // Verificar se é admin
        sessionManager = new SessionManager(this);
        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "❌ Acesso negado! Área restrita a administradores.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("📜 Histórico de Auditoria");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inicializarComponentes();
        carregarAuditorias();
    }

    private void inicializarComponentes() {
        recyclerViewAuditoria = findViewById(R.id.recyclerViewAuditoria);
        tvContadorAcoes = findViewById(R.id.tvContadorAcoes);
        btnFiltrar = findViewById(R.id.btnFiltrar);
        btnLimparLogs = findViewById(R.id.btnLimparLogs);

        auditoriaDAO = new AuditoriaDAO(this);
        listaAuditorias = new ArrayList<>();

        recyclerViewAuditoria.setLayoutManager(new LinearLayoutManager(this));

        btnFiltrar.setOnClickListener(v -> mostrarDialogoFiltros());
        btnLimparLogs.setOnClickListener(v -> confirmarLimparLogs());
    }

    private void carregarAuditorias() {
        try {
            Log.d(TAG, "=== CARREGANDO AUDITORIAS ===");
            listaAuditorias = auditoriaDAO.buscarTodasAcoes();

            if (listaAuditorias == null) {
                listaAuditorias = new ArrayList<>();
            }

            Log.d(TAG, "Total de ações: " + listaAuditorias.size());

            // Atualizar contador
            String texto = listaAuditorias.size() == 1 ?
                    "1 ação registrada" : listaAuditorias.size() + " ações registradas";
            tvContadorAcoes.setText(texto);

            // Configurar adapter
            if (auditoriaAdapter == null) {
                auditoriaAdapter = new AuditoriaAdapter(listaAuditorias, this,
                        auditoria -> mostrarDetalhesAuditoria(auditoria));
                recyclerViewAuditoria.setAdapter(auditoriaAdapter);
            } else {
                auditoriaAdapter.atualizarLista(listaAuditorias);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao carregar auditorias: ", e);
            Toast.makeText(this, "Erro ao carregar histórico", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoFiltros() {
        String[] opcoes = {
                "📋 Todas as Ações",
                "👤 Minhas Ações",
                "➕ Apenas Criações",
                "✏️ Apenas Edições",
                "🗑️ Apenas Exclusões",
                "❌ Cancelar"
        };

        new AlertDialog.Builder(this)
                .setTitle("Filtrar Histórico")
                .setItems(opcoes, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            carregarAuditorias();
                            break;
                        case 1:
                            carregarMinhasAcoes();
                            break;
                        case 2:
                            filtrarPorAcao("CRIOU");
                            break;
                        case 3:
                            filtrarPorAcao("EDITOU");
                            break;
                        case 4:
                            filtrarPorAcao("DELETOU");
                            break;
                    }
                })
                .show();
    }

    private void carregarMinhasAcoes() {
        try {
            long meuId = sessionManager.getUserId();
            listaAuditorias = auditoriaDAO.buscarAcoesPorUsuario(meuId);

            String texto = listaAuditorias.size() == 1 ?
                    "1 ação registrada" : listaAuditorias.size() + " ações registradas";
            tvContadorAcoes.setText(texto);

            auditoriaAdapter.atualizarLista(listaAuditorias);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao carregar minhas ações: ", e);
            Toast.makeText(this, "Erro ao filtrar", Toast.LENGTH_SHORT).show();
        }
    }

    private void filtrarPorAcao(String acao) {
        try {
            List<Auditoria> todasAcoes = auditoriaDAO.buscarTodasAcoes();
            List<Auditoria> acoesFiltradas = new ArrayList<>();

            for (Auditoria auditoria : todasAcoes) {
                if (auditoria.getAcao().toUpperCase().contains(acao.toUpperCase())) {
                    acoesFiltradas.add(auditoria);
                }
            }

            listaAuditorias = acoesFiltradas;

            String texto = listaAuditorias.size() == 1 ?
                    "1 ação encontrada" : listaAuditorias.size() + " ações encontradas";
            tvContadorAcoes.setText(texto);

            auditoriaAdapter.atualizarLista(listaAuditorias);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao filtrar por ação: ", e);
            Toast.makeText(this, "Erro ao filtrar", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDetalhesAuditoria(Auditoria auditoria) {
        String mensagem = "📋 Ação: " + auditoria.getAcao() + "\n\n" +
                "👤 Usuário: " + auditoria.getNomeUsuario() + "\n\n" +
                "📦 Entidade: " + auditoria.getEntidade() + " #" + auditoria.getEntidadeId() + "\n\n" +
                "📝 Descrição: " + auditoria.getDescricao() + "\n\n" +
                "📅 Data: " + auditoria.getDataFormatada();

        if (auditoria.getDispositivo() != null) {
            mensagem += "\n\n📱 Dispositivo: " + auditoria.getDispositivo();
        }

        new AlertDialog.Builder(this)
                .setTitle("Detalhes da Ação")
                .setMessage(mensagem)
                .setPositiveButton("OK", null)
                .show();
    }

    private void confirmarLimparLogs() {
        String[] opcoes = {
                "🗑️ Limpar logs com mais de 30 dias",
                "🗑️ Limpar logs com mais de 60 dias",
                "🗑️ Limpar logs com mais de 90 dias",
                "❌ Cancelar"
        };

        new AlertDialog.Builder(this)
                .setTitle("Limpar Logs Antigos")
                .setItems(opcoes, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            limparLogsAntigos(30);
                            break;
                        case 1:
                            limparLogsAntigos(60);
                            break;
                        case 2:
                            limparLogsAntigos(90);
                            break;
                    }
                })
                .show();
    }

    private void limparLogsAntigos(int dias) {
        try {
            int deletados = auditoriaDAO.limparLogsAntigos(dias);

            Toast.makeText(this, "✅ " + deletados + " logs antigos deletados",
                    Toast.LENGTH_SHORT).show();

            carregarAuditorias();

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao limpar logs: ", e);
            Toast.makeText(this, "Erro ao limpar logs", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}