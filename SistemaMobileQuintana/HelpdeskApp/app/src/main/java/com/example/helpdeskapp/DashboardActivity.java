package com.example.helpdeskapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.helpdeskapp.dao.EstatisticaDAO;
import com.example.helpdeskapp.models.Estatistica;
import com.example.helpdeskapp.utils.SessionManager;
import com.example.helpdeskapp.utils.ThemeManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "Dashboard";

    // Estatísticas
    private TextView tvTotalChamados, tvTaxaResolucao, tvMediaAvaliacoes;
    private TextView tvChamadosHoje, tvChamadosSemana, tvChamadosMes;
    private TextView tvTotalUsuarios, tvClientes, tvAdmins;

    // Gráficos
    private PieChart pieChartStatus;
    private BarChart barChartPrioridade;
    private LineChart lineChartPeriodo;

    // Loading
    private ProgressBar progressBar;
    private CardView cardGraficos;

    private EstatisticaDAO estatisticaDAO;
    private SessionManager sessionManager;
    private ThemeManager themeManager;
    private Estatistica stats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        themeManager = new ThemeManager(this);
        themeManager.applyTheme();

        super.onCreate(savedInstanceState);

        // Verificar se é admin
        sessionManager = new SessionManager(this);
        if (!sessionManager.isAdmin()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_dashboard);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("📊 Dashboard");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inicializarComponentes();
        carregarDados();
    }

    private void inicializarComponentes() {
        // TextViews de Estatísticas
        tvTotalChamados = findViewById(R.id.tvTotalChamados);
        tvTaxaResolucao = findViewById(R.id.tvTaxaResolucao);
        tvMediaAvaliacoes = findViewById(R.id.tvMediaAvaliacoes);

        tvChamadosHoje = findViewById(R.id.tvChamadosHoje);
        tvChamadosSemana = findViewById(R.id.tvChamadosSemana);
        tvChamadosMes = findViewById(R.id.tvChamadosMes);

        tvTotalUsuarios = findViewById(R.id.tvTotalUsuarios);
        tvClientes = findViewById(R.id.tvClientes);
        tvAdmins = findViewById(R.id.tvAdmins);

        // Gráficos
        pieChartStatus = findViewById(R.id.pieChartStatus);
        barChartPrioridade = findViewById(R.id.barChartPrioridade);
        lineChartPeriodo = findViewById(R.id.lineChartPeriodo);

        // Loading
        progressBar = findViewById(R.id.progressBar);
        cardGraficos = findViewById(R.id.cardGraficos);

        estatisticaDAO = new EstatisticaDAO(this);
    }

    private void carregarDados() {
        progressBar.setVisibility(View.VISIBLE);
        cardGraficos.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                // Buscar estatísticas
                stats = estatisticaDAO.buscarEstatisticasGerais();

                runOnUiThread(() -> {
                    atualizarEstatisticas();
                    configurarGraficos();

                    progressBar.setVisibility(View.GONE);
                    cardGraficos.setVisibility(View.VISIBLE);
                });

            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar dados: ", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private void atualizarEstatisticas() {
        // Estatísticas principais
        tvTotalChamados.setText(String.valueOf(stats.getTotalChamados()));
        tvTaxaResolucao.setText(stats.getTaxaResolucao() + "%");
        tvMediaAvaliacoes.setText(String.format("%.1f ⭐", stats.getMediaAvaliacoes()));

        // Chamados por período
        tvChamadosHoje.setText(String.valueOf(stats.getChamadosHoje()));
        tvChamadosSemana.setText(String.valueOf(stats.getChamadosSemana()));
        tvChamadosMes.setText(String.valueOf(stats.getChamadosMes()));

        // Usuários
        tvTotalUsuarios.setText(String.valueOf(stats.getTotalUsuarios()));
        tvClientes.setText(String.valueOf(stats.getTotalClientes()));
        tvAdmins.setText(String.valueOf(stats.getTotalAdmins()));
    }

    private void configurarGraficos() {
        configurarGraficoPizza();
        configurarGraficoBarras();
        configurarGraficoLinha();
    }

    // ========== GRÁFICO DE PIZZA (Status dos Chamados) ==========
    private void configurarGraficoPizza() {
        List<PieEntry> entries = new ArrayList<>();

        if (stats.getChamadosAbertos() > 0) {
            entries.add(new PieEntry(stats.getChamadosAbertos(), "Abertos"));
        }
        if (stats.getChamadosEmAndamento() > 0) {
            entries.add(new PieEntry(stats.getChamadosEmAndamento(), "Em Andamento"));
        }
        if (stats.getChamadosResolvidos() > 0) {
            entries.add(new PieEntry(stats.getChamadosResolvidos(), "Resolvidos"));
        }
        if (stats.getChamadosFechados() > 0) {
            entries.add(new PieEntry(stats.getChamadosFechados(), "Fechados"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Status");

        // Cores personalizadas
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#F44336")); // Vermelho - Abertos
        colors.add(Color.parseColor("#FF9800")); // Laranja - Em Andamento
        colors.add(Color.parseColor("#4CAF50")); // Verde - Resolvidos
        colors.add(Color.parseColor("#9E9E9E")); // Cinza - Fechados
        dataSet.setColors(colors);

        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChartStatus.setData(data);

        // Configurações do gráfico
        pieChartStatus.setUsePercentValues(true);
        pieChartStatus.getDescription().setEnabled(false);
        pieChartStatus.setDrawHoleEnabled(true);
        pieChartStatus.setHoleColor(Color.TRANSPARENT);
        pieChartStatus.setTransparentCircleRadius(58f);
        pieChartStatus.setDrawCenterText(true);
        pieChartStatus.setCenterText("Status dos\nChamados");
        pieChartStatus.setCenterTextSize(14f);

        // Legenda
        Legend legend = pieChartStatus.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(10f);

        pieChartStatus.animateY(1000);
        pieChartStatus.invalidate();
    }

    // ========== GRÁFICO DE BARRAS (Prioridades) ==========
    private void configurarGraficoBarras() {
        if (stats == null) {
            Log.w("DashboardActivity", "Stats nulo — pulando configurarGraficoBarras()");
            return;
        }

        // Valores (y) como float — x são índices
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) stats.getPrioridadeAlta()));
        entries.add(new BarEntry(1f, (float) stats.getPrioridadeMedia()));
        entries.add(new BarEntry(2f, (float) stats.getPrioridadeBaixa()));

        BarDataSet dataSet = new BarDataSet(entries, "Prioridade");
        // Cores personalizadas
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#F44336")); // Alta
        colors.add(Color.parseColor("#FF9800")); // Média
        colors.add(Color.parseColor("#4CAF50")); // Baixa
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK); // melhor contraste

        // Remove desenho de valores se quiser menos poluição:
        // dataSet.setDrawValues(false);

        BarData data = new BarData(dataSet);
        // Ajuste de largura - tenta não ocupar 100% do espaço
        data.setBarWidth(0.6f);

        barChartPrioridade.setData(data);

        // Desliga descrição e eixo direito
        if (barChartPrioridade.getDescription() != null) {
            barChartPrioridade.getDescription().setEnabled(false);
        }
        barChartPrioridade.setFitBars(true);
        barChartPrioridade.getAxisRight().setEnabled(false);

        // Eixo X
        XAxis xAxis = barChartPrioridade.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(3, true); // forçar 3 labels
        xAxis.setDrawGridLines(false);
        xAxis.setCenterAxisLabels(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Alta", "Média", "Baixa"}));

        // Eixo Y
        barChartPrioridade.getAxisLeft().setGranularity(1f);
        barChartPrioridade.getAxisLeft().setDrawGridLines(true);

        // Legend
        if (barChartPrioridade.getLegend() != null) {
            barChartPrioridade.getLegend().setEnabled(false);
        }

        // Animação e redraw
        barChartPrioridade.animateY(800);
        barChartPrioridade.invalidate();
    }

    // ========== GRÁFICO DE LINHA (Chamados por Período) ==========
    private void configurarGraficoLinha() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0f, stats.getChamadosHoje()));
        entries.add(new Entry(1f, stats.getChamadosSemana()));
        entries.add(new Entry(2f, stats.getChamadosMes()));

        LineDataSet dataSet = new LineDataSet(entries, "Chamados");
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setCircleColor(Color.parseColor("#2196F3"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(6f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(12f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#2196F3"));
        dataSet.setFillAlpha(50);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData data = new LineData(dataSet);
        lineChartPeriodo.setData(data);

        // Configurações
        lineChartPeriodo.getDescription().setEnabled(false);
        lineChartPeriodo.getAxisRight().setEnabled(false);
        lineChartPeriodo.setDrawGridBackground(false);

        // Eixo X
        XAxis xAxis = lineChartPeriodo.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"Hoje", "7 dias", "30 dias"}
        ));

        // Legenda
        lineChartPeriodo.getLegend().setEnabled(false);

        lineChartPeriodo.animateX(1000);
        lineChartPeriodo.invalidate();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}