package com.example.helpdeskapp;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.helpdeskapp.dao.NotificacaoDAO;
import com.google.android.material.navigation.NavigationView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.helpdeskapp.utils.SessionManager;
import com.example.helpdeskapp.database.DatabaseHelper;
import com.example.helpdeskapp.helpers.AuditoriaHelper;
import com.example.helpdeskapp.utils.ThemeManager;
import com.example.helpdeskapp.adapters.ChamadoRecenteAdapter;
import com.example.helpdeskapp.api.RetrofitClient;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.helpdeskapp.dao.ChamadoDAO;
import com.example.helpdeskapp.models.Chamado;
import java.util.ArrayList;
import java.util.List;
import com.example.helpdeskapp.helpers.SyncHelper;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    // ========== COMPONENTES DA INTERFACE ==========
    private TextView tvBemVindo, tvTipoUsuario;
    private TextView tvChamadosAbertos, tvChamadosProgresso;
    private TextView tvBadgeNotificacoes;
    private ImageButton btnNotifications;

    // Componentes do Menu Lateral
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Toolbar toolbar;

    // ========== MANAGERS / DAOS ==========
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private ThemeManager themeManager;
    private NotificacaoDAO notificacaoDAO;
    private int currentActivityTheme;
    private RecyclerView recyclerViewChamadosRecentes;
    private TextView tvMensagemVazio;
    private ChamadoRecenteAdapter chamadoRecenteAdapter;
    private List<Chamado> chamadosRecentes;
    private ChamadoDAO chamadoDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        themeManager = new ThemeManager(this);
        themeManager.applyTheme();
        currentActivityTheme = themeManager.getCurrentTheme();
        super.onCreate(savedInstanceState);

        RetrofitClient.init(this);

        setContentView(R.layout.activity_main);

        notificacaoDAO = new NotificacaoDAO(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("HelpDesk");
        }

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirecionarParaLogin();
            return;
        }

        inicializarComponentes();
        configurarInformacoesUsuario();
        configurarMenuPorTipo(); // ✅ CHAMAR AQUI TAMBÉM
        configurarBotaoNotificacoes();
        configurarAcoesRapidas();
        configurarChamadosRecentes();

        updateStatusCards();
        carregarChamadosRecentes();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ✅ RECONFIGURAR MENU TODA VEZ QUE VOLTAR
        configurarMenuPorTipo();

        // Verificar mudança de tema
        if (themeManager.getCurrentTheme() != currentActivityTheme) {
            recreate();
            return;
        }

        // Sincronizar com API
        sincronizarDados();

        // Atualizar dados na tela
        updateStatusCards();
        atualizarBadgeNotificacoes();
        carregarChamadosRecentes();
    }

    // ✅ MÉTODO MOVIDO PARA FORA DO onResume()
    private void configurarMenuPorTipo() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView == null) {
            Log.e(TAG, "❌ NavigationView é null!");
            return;
        }

        Menu menu = navigationView.getMenu();
        boolean isAdmin = sessionManager.isAdmin();

        Log.d(TAG, "🔐 Configurando menu para: " + (isAdmin ? "ADMIN" : "CLIENTE"));

        // ✅ USAR APENAS OS IDs QUE EXISTEM NO SEU MENU XML

        // Itens exclusivos de Admin (baseado no seu onNavigationItemSelected)
        MenuItem todosChamados = menu.findItem(R.id.nav_todos_chamados);
        MenuItem dashboard = menu.findItem(R.id.nav_dashboard);
        MenuItem gerenciarTags = menu.findItem(R.id.nav_gerenciar_tags);
        MenuItem historico = menu.findItem(R.id.nav_historico);

        // Esconder/mostrar baseado no tipo
        if (todosChamados != null) {
            todosChamados.setVisible(isAdmin);
            Log.d(TAG, "   nav_todos_chamados: " + (isAdmin ? "VISÍVEL" : "OCULTO"));
        }

        if (dashboard != null) {
            dashboard.setVisible(isAdmin);
            Log.d(TAG, "   nav_dashboard: " + (isAdmin ? "VISÍVEL" : "OCULTO"));
        }

        if (gerenciarTags != null) {
            gerenciarTags.setVisible(isAdmin);
            Log.d(TAG, "   nav_gerenciar_tags: " + (isAdmin ? "VISÍVEL" : "OCULTO"));
        }

        if (historico != null) {
            historico.setVisible(isAdmin);
            Log.d(TAG, "   nav_historico: " + (isAdmin ? "VISÍVEL" : "OCULTO"));
        }

        Log.d(TAG, "✅ Menu configurado com sucesso!");
    }

    private void sincronizarDados() {
        if (sessionManager.getToken() != null) {
            Log.d(TAG, "🔄 Sincronizando dados com API...");

            SyncHelper.sincronizarChamados(this, new SyncHelper.SyncCallback() {
                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "✅ " + message);
                    runOnUiThread(() -> {
                        updateStatusCards();
                        carregarChamadosRecentes();
                    });
                }

                @Override
                public void onError(String error) {
                    Log.w(TAG, "⚠️ " + error);
                }
            });
        } else {
            Log.d(TAG, "💾 Modo offline - usando dados locais");
        }
    }

    private void configurarBotaoNotificacoes() {
        btnNotifications = findViewById(R.id.btnNotifications);
        tvBadgeNotificacoes = findViewById(R.id.tvBadgeNotificacoes);

        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificacoesActivity.class);
            startActivity(intent);
        });

        atualizarBadgeNotificacoes();
    }

    private void atualizarBadgeNotificacoes() {
        long usuarioId = sessionManager.getUserId();

        notificacaoDAO.open();
        int naoLidas = notificacaoDAO.contarNaoLidas(usuarioId);
        notificacaoDAO.close();

        if (naoLidas > 0) {
            tvBadgeNotificacoes.setVisibility(View.VISIBLE);
            tvBadgeNotificacoes.setText(naoLidas > 99 ? "99+" : String.valueOf(naoLidas));
        } else {
            tvBadgeNotificacoes.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_abrir_chamado) {
            startActivity(new Intent(MainActivity.this, AbrirChamadoActivity.class));
        } else if (id == R.id.nav_meus_chamados) {
            startActivity(new Intent(MainActivity.this, MeusChamadosActivity.class));
        } else if (id == R.id.nav_buscar_chamado) {
            startActivity(new Intent(MainActivity.this, BuscarChamadoActivity.class));
        } else if (id == R.id.nav_todos_chamados) {
            startActivity(new Intent(MainActivity.this, AdminPanelActivity.class));
        } else if (id == R.id.nav_dashboard) {
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
        } else if (id == R.id.nav_gerenciar_tags) {
            startActivity(new Intent(MainActivity.this, GerenciarTagsActivity.class));
        } else if (id == R.id.nav_historico) {
            startActivity(new Intent(MainActivity.this, HistoricoAuditoriaActivity.class));
        } else if (id == R.id.nav_personalizar_tema) {
            startActivity(new Intent(MainActivity.this, PersonalizarTemaActivity.class));
        } else if (id == R.id.nav_diversidade) {
            startActivity(new Intent(MainActivity.this, DiversidadeActivity.class));
        } else if (id == R.id.nav_mover) {
            abrirSiteDiversidade();
        } else if (id == R.id.nav_logout) {
            mostrarConfirmacaoLogout();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void redirecionarParaLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void inicializarComponentes() {
        tvBemVindo = findViewById(R.id.tvBemVindo);
        tvTipoUsuario = findViewById(R.id.tvTipoUsuario);
        tvChamadosAbertos = findViewById(R.id.tvChamadosAbertos);
        tvChamadosProgresso = findViewById(R.id.tvChamadosProgresso);
        recyclerViewChamadosRecentes = findViewById(R.id.recyclerViewChamadosRecentes);
        tvMensagemVazio = findViewById(R.id.tvMensagemVazio);

        dbHelper = new DatabaseHelper(this);
        chamadoDAO = new ChamadoDAO(this);
        chamadosRecentes = new ArrayList<>();
    }

    private void updateStatusCards() {
        if (dbHelper == null) return;

        int abertos = dbHelper.countChamadosByStatus("Aberto");
        int emAndamento = dbHelper.countChamadosByStatus("Em Andamento");

        tvChamadosAbertos.setText(String.valueOf(abertos));
        tvChamadosProgresso.setText(String.valueOf(emAndamento));
    }

    private void configurarInformacoesUsuario() {
        String nomeUsuario = sessionManager.getUserName();
        int tipoUsuario = sessionManager.getUserType();

        Log.d(TAG, "👤 Usuário: " + nomeUsuario);
        Log.d(TAG, "🔑 Tipo: " + tipoUsuario + " (" +
                (tipoUsuario == 1 ? "ADMIN" : "CLIENTE") + ")");

        String[] frasesConsciencia = getResources().getStringArray(R.array.frases_consciencia);
        String fraseSorteada = frasesConsciencia[(int) (Math.random() * frasesConsciencia.length)];

        String textoBemVindo;
        if (nomeUsuario != null && !nomeUsuario.isEmpty()) {
            textoBemVindo = "Olá, " + nomeUsuario + "! " + fraseSorteada;
        } else {
            textoBemVindo = "Olá, Bem-vindo(a)! " + fraseSorteada;
        }

        tvBemVindo.setText(textoBemVindo);
        tvTipoUsuario.setText(sessionManager.getUserTypeText());
    }

    private void mostrarConfirmacaoLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Sair")
                .setMessage("Deseja realmente sair?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    AuditoriaHelper.registrarLogout(
                            this,
                            sessionManager.getUserId(),
                            sessionManager.getUserName()
                    );
                    sessionManager.logout();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void abrirSiteDiversidade() {
        new AlertDialog.Builder(this)
                .setTitle("🌍 MOVER - Diversidade e Inclusão")
                .setMessage("A MOVER é uma organização dedicada a promover diversidade e inclusão no mercado de tecnologia.\n\nDeseja saber mais?")
                .setPositiveButton("🌐 Abrir Site", (dialog, which) -> tentarAbrirLinkForcado())
                .setNeutralButton("📋 Ver no App", (dialog, which) -> mostrarInformacoesMover())
                .setNegativeButton("❌ Cancelar", null)
                .show();
    }

    private void tentarAbrirLinkForcado() {
        String url = "https://somosmover.org/quem-somos/";
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível abrir o navegador. Tente copiar o link.", Toast.LENGTH_LONG).show();
            mostrarOpcoesManualParaAbrirSite(url);
        }
    }

    private void mostrarOpcoesManualParaAbrirSite(String url) {
        new AlertDialog.Builder(this)
                .setTitle("📱 Abrir Site Manualmente")
                .setMessage("Não foi possível abrir o link automaticamente.\n\n🔗 LINK: " + url)
                .setPositiveButton("📋 Copiar Link", (dialog, which) -> copiarLinkParaClipboard(url))
                .setNeutralButton("📱 Ver no App", (dialog, which) -> mostrarInformacoesMover())
                .setNegativeButton("❌ Fechar", null)
                .show();
    }

    private void copiarLinkParaClipboard(String url) {
        try {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("MOVER Site", url);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "📋 Link copiado! Abra o navegador e cole.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "❌ Erro ao copiar o link.", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarInformacoesMover() {
        new AlertDialog.Builder(this)
                .setTitle("🌍 MOVER - Diversidade e Inclusão")
                .setMessage(
                        "🎯 MISSÃO: Promover diversidade e inclusão no mercado de tecnologia, com foco em pessoas negras.\n\n" +
                                "🚀 AÇÕES: Programas de capacitação, mentoria e conexão com oportunidades.\n\n" +
                                "💡 Este projeto apoia a diversidade racial na tecnologia!"
                )
                .setPositiveButton("📋 Copiar Site", (dialog, which) -> copiarLinkParaClipboard("https://somosmover.org/"))
                .setNegativeButton("✅ Entendi", null)
                .show();
    }

    private void configurarAcoesRapidas() {
        findViewById(R.id.cardAbrirChamadoRapido).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AbrirChamadoActivity.class));
        });

        findViewById(R.id.cardMeusChamadosRapido).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MeusChamadosActivity.class));
        });
    }

    private void configurarChamadosRecentes() {
        if (recyclerViewChamadosRecentes == null) {
            Log.e(TAG, "❌ RecyclerView é null!");
            return;
        }

        recyclerViewChamadosRecentes.setLayoutManager(new LinearLayoutManager(this));
        chamadoRecenteAdapter = new ChamadoRecenteAdapter(this, chamadosRecentes);
        recyclerViewChamadosRecentes.setAdapter(chamadoRecenteAdapter);
    }

    private void carregarChamadosRecentes() {
        if (recyclerViewChamadosRecentes == null || tvMensagemVazio == null) {
            return;
        }

        if (chamadoDAO == null) {
            return;
        }

        chamadoDAO.open();

        long usuarioId = sessionManager.getUserId();

        List<Chamado> todosChamados;
        if (sessionManager.isAdmin()) {
            todosChamados = chamadoDAO.buscarTodosChamados();
        } else {
            todosChamados = chamadoDAO.listarChamadosPorCliente(usuarioId);
        }

        chamadoDAO.close();

        if (todosChamados.isEmpty()) {
            recyclerViewChamadosRecentes.setVisibility(View.GONE);
            tvMensagemVazio.setVisibility(View.VISIBLE);
        } else {
            recyclerViewChamadosRecentes.setVisibility(View.VISIBLE);
            tvMensagemVazio.setVisibility(View.GONE);

            int limite = Math.min(5, todosChamados.size());
            chamadosRecentes = todosChamados.subList(0, limite);
            chamadoRecenteAdapter.updateList(chamadosRecentes);
        }
    }
}