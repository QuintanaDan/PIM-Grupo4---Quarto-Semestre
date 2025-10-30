package com.example.helpdeskapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.helpdeskapp.adapters.NotificacaoAdapter;
import com.example.helpdeskapp.dao.NotificacaoDAO;
import com.example.helpdeskapp.models.Notificacao;
import com.example.helpdeskapp.utils.SessionManager;
import com.example.helpdeskapp.utils.ThemeManager;
import java.util.ArrayList;
import java.util.List;

public class NotificacoesActivity extends AppCompatActivity {
    private static final String TAG = "Notificacoes";

    private RecyclerView recyclerView;
    private TextView tvMensagemVazio;
    private NotificacaoAdapter adapter;
    private List<Notificacao> notificacoes;

    private NotificacaoDAO notificacaoDAO;
    private SessionManager sessionManager;
    private ThemeManager themeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        themeManager = new ThemeManager(this);
        themeManager.applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacoes);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("🔔 Notificações");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inicializarComponentes();
        configurarRecyclerView();
        carregarNotificacoes();
    }

    private void inicializarComponentes() {
        recyclerView = findViewById(R.id.recyclerViewNotificacoes);
        tvMensagemVazio = findViewById(R.id.tvMensagemVazio);

        sessionManager = new SessionManager(this);
        notificacaoDAO = new NotificacaoDAO(this);
        notificacoes = new ArrayList<>();
    }

    private void configurarRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificacaoAdapter(this, notificacoes);

        adapter.setOnNotificacaoClickListener(new NotificacaoAdapter.OnNotificacaoClickListener() {
            @Override
            public void onNotificacaoClick(Notificacao notificacao) {
                // Click já é tratado no adapter
            }

            @Override
            public void onNotificacaoLongClick(Notificacao notificacao) {
                mostrarOpcoesNotificacao(notificacao);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void carregarNotificacoes() {
        long usuarioId = sessionManager.getUserId();
        notificacoes = notificacaoDAO.buscarNotificacoesUsuario(usuarioId);

        if (notificacoes.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvMensagemVazio.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvMensagemVazio.setVisibility(View.GONE);
            adapter.updateList(notificacoes);
        }
    }

    private void mostrarOpcoesNotificacao(Notificacao notificacao) {
        String[] opcoes = {"Deletar"};

        new AlertDialog.Builder(this)
                .setTitle("Opções")
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) {
                        deletarNotificacao(notificacao);
                    }
                })
                .show();
    }

    private void deletarNotificacao(Notificacao notificacao) {
        new AlertDialog.Builder(this)
                .setTitle("Deletar Notificação")
                .setMessage("Deseja realmente deletar esta notificação?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    boolean sucesso = notificacaoDAO.deletarNotificacao(notificacao.getId());

                    if (sucesso) {
                        Toast.makeText(this, "Notificação deletada", Toast.LENGTH_SHORT).show();
                        carregarNotificacoes();
                    } else {
                        Toast.makeText(this, "Erro ao deletar", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void marcarTodasComoLidas() {
        long usuarioId = sessionManager.getUserId();
        boolean sucesso = notificacaoDAO.marcarTodasComoLidas(usuarioId);

        if (sucesso) {
            Toast.makeText(this, "Todas marcadas como lidas", Toast.LENGTH_SHORT).show();
            carregarNotificacoes();
        }
    }

    private void limparNotificacoesAntigas() {
        new AlertDialog.Builder(this)
                .setTitle("Limpar Notificações Antigas")
                .setMessage("Deletar notificações com mais de 30 dias?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    int deletadas = notificacaoDAO.deletarNotificacoesAntigas(30);
                    Toast.makeText(this,
                            deletadas + " notificações deletadas",
                            Toast.LENGTH_SHORT).show();
                    carregarNotificacoes();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notificacoes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_marcar_todas_lidas) {
            marcarTodasComoLidas();
            return true;
        } else if (id == R.id.action_limpar_antigas) {
            limparNotificacoesAntigas();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarNotificacoes();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}