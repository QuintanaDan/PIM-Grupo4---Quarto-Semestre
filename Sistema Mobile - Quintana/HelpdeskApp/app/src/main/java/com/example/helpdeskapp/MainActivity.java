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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.helpdeskapp.utils.SessionManager;
import com.example.helpdeskapp.database.DatabaseHelper;
import com.example.helpdeskapp.utils.AuditoriaHelper;
import com.example.helpdeskapp.utils.ThemeManager;
import com.example.helpdeskapp.R;
import com.example.helpdeskapp.adapters.ChamadoRecenteAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.helpdeskapp.adapters.ChamadoRecenteAdapter;
import com.example.helpdeskapp.dao.ChamadoDAO;
import com.example.helpdeskapp.models.Chamado;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // ========== COMPONENTES DA INTERFACE ==========
    private TextView tvBemVindo, tvTipoUsuario;
    private TextView tvChamadosAbertos, tvChamadosProgresso;
    private TextView tvBadgeNotificacoes; // Declarado
    private ImageButton btnNotifications; // Declarado

    // Componentes do Menu Lateral
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Toolbar toolbar;

    // ========== MANAGERS / DAOS ==========
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private ThemeManager themeManager;
    private NotificacaoDAO notificacaoDAO; // Declarado

    private int currentActivityTheme;
    private RecyclerView recyclerViewChamadosRecentes;
    private TextView tvMensagemVazio;
    private ChamadoRecenteAdapter chamadoRecenteAdapter;
    private List<Chamado> chamadosRecentes;
    private ChamadoDAO chamadoDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Lógica do Tema
        themeManager = new ThemeManager(this);
        themeManager.applyTheme();
        currentActivityTheme = themeManager.getCurrentTheme();
        super.onCreate(savedInstanceState);

        // ========== CONFIGURAR LAYOUT ==========
        setContentView(R.layout.activity_main);

        // ✅ CORREÇÃO APLICADA: Inicializa o DAO antes de ser usado
        notificacaoDAO = new NotificacaoDAO(this);

        // 2. CONFIGURAÇÃO DO TOOLBAR/DRAWER
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("HelpDesk");
        }

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Configurar o ícone do hambúrguer e o listener do menu
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // ========== VERIFICAR SESSÃO ==========
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirecionarParaLogin();
            return;
        }

        // ========== INICIALIZAR COMPONENTES ==========
        inicializarComponentes();
        configurarInformacoesUsuario();
        configurarVisibilidadeMenu();
        configurarBotaoNotificacoes();
        configurarAcoesRapidas();
        configurarChamadosRecentes();

        // ========== ATUALIZAR DADOS DINÂMICOS ==========
        updateStatusCards();
        carregarChamadosRecentes();


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recria a Activity se o tema mudou
        if (themeManager.getCurrentTheme() != currentActivityTheme) {
            recreate();
            return;
        }
        // Atualiza dados na volta para a Activity
        updateStatusCards();
        atualizarBadgeNotificacoes();
        carregarChamadosRecentes();
    }

    private void configurarBotaoNotificacoes() {
        // Inicialização de componentes do botão de notificação (feito apenas uma vez)
        btnNotifications = findViewById(R.id.btnNotifications);
        tvBadgeNotificacoes = findViewById(R.id.tvBadgeNotificacoes);

        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificacoesActivity.class);
            startActivity(intent);
        });

        // Chama a atualização para carregar o estado inicial
        atualizarBadgeNotificacoes();
    }

    private void atualizarBadgeNotificacoes() {
        // ✅ O notificacaoDAO não é mais null aqui.
        long usuarioId = sessionManager.getUserId();
        int naoLidas = notificacaoDAO.contarNaoLidas(usuarioId);

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
            abrirSiteDiversidade(); // Mantido como link externo/modal
        } else if (id == R.id.nav_logout) {
            mostrarConfirmacaoLogout();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void redirecionarParaLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
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

        // Inicializa o DatabaseHelper aqui
        dbHelper = new DatabaseHelper(this);
        chamadoDAO = new ChamadoDAO(this);
        chamadosRecentes = new ArrayList<>();
    }

    private void configurarVisibilidadeMenu() {
        boolean isAdmin = sessionManager.isAdmin();
        Menu menu = navigationView.getMenu();

        // Itens de Administração
        menu.findItem(R.id.nav_todos_chamados).setVisible(isAdmin);
        menu.findItem(R.id.nav_dashboard).setVisible(isAdmin);
        menu.findItem(R.id.nav_gerenciar_tags).setVisible(isAdmin);
        menu.findItem(R.id.nav_historico).setVisible(isAdmin);
    }

    private void updateStatusCards() {
        // Verifica se o dbHelper foi inicializado corretamente antes de usar
        if (dbHelper == null) return;

        int abertos = dbHelper.countChamadosByStatus("Aberto");
        int emAndamento = dbHelper.countChamadosByStatus("Em Andamento");

        tvChamadosAbertos.setText(String.valueOf(abertos));
        tvChamadosProgresso.setText(String.valueOf(emAndamento));
    }


    private void configurarInformacoesUsuario() {
        String nomeUsuario = sessionManager.getUserName();
        int tipoUsuario = sessionManager.getUserType();

        // DEBUG: Verificar tipo
        android.util.Log.d("MainActivity", "👤 Usuário: " + nomeUsuario);
        android.util.Log.d("MainActivity", "🔑 Tipo: " + tipoUsuario + " (" +
                (tipoUsuario == 1 ? "ADMIN" : "CLIENTE") + ")");

        // AQUI INICIA A LÓGICA DA MENSAGEM DE CONSCIÊNCIA RACIAL

        // 1. Array de frases para sorteio
        // É essencial que você tenha o array de strings em res/values/strings.xml:
        // <string-array name="frases_consciencia">...</string-array>
        String[] frasesConsciencia = getResources().getStringArray(R.array.frases_consciencia);

        // 2. Sortear uma frase
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
        // Card: Abrir Chamado
        findViewById(R.id.cardAbrirChamadoRapido).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AbrirChamadoActivity.class));
        });

        // Card: Meus Chamados
        findViewById(R.id.cardMeusChamadosRapido).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MeusChamadosActivity.class));
        });
    }

    private void configurarChamadosRecentes() {
        if (recyclerViewChamadosRecentes == null) {
            android.util.Log.e("MainActivity", "❌ RecyclerView é null!");
            return;
        }

        android.util.Log.d("MainActivity", "✅ Configurando RecyclerView...");
        recyclerViewChamadosRecentes.setLayoutManager(new LinearLayoutManager(this));
        chamadoRecenteAdapter = new ChamadoRecenteAdapter(this, chamadosRecentes);
        recyclerViewChamadosRecentes.setAdapter(chamadoRecenteAdapter);
    }

    private void carregarChamadosRecentes() {
        if (recyclerViewChamadosRecentes == null || tvMensagemVazio == null) {
            android.util.Log.e("MainActivity", "❌ Componentes não inicializados!");
            return;
        }

        if (chamadoDAO == null) {
            android.util.Log.e("MainActivity", "❌ ChamadoDAO é null!");
            return;
        }

        long usuarioId = sessionManager.getUserId();

        // Buscar chamados do usuário (ou todos se for admin)
        List<Chamado> todosChamados;
        if (sessionManager.isAdmin()) {
            todosChamados = chamadoDAO.buscarTodosChamados();
        } else {
            todosChamados = chamadoDAO.listarChamadosPorCliente(usuarioId);
        }

        if (todosChamados.isEmpty()) {
            recyclerViewChamadosRecentes.setVisibility(View.GONE);
            tvMensagemVazio.setVisibility(View.VISIBLE);
        } else {
            recyclerViewChamadosRecentes.setVisibility(View.VISIBLE);
            tvMensagemVazio.setVisibility(View.GONE);

            // Pegar os 5 mais recentes
            int limite = Math.min(5, todosChamados.size());
            chamadosRecentes = todosChamados.subList(0, limite);
            chamadoRecenteAdapter.updateList(chamadosRecentes);
        }
    }

}