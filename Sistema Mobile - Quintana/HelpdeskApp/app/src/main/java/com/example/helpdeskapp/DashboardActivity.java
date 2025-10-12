package com.example.helpdeskapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.example.helpdeskapp.dao.AvaliacaoDAO;
import com.example.helpdeskapp.database.DatabaseHelper;
import com.example.helpdeskapp.utils.SessionManager;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "Dashboard";

    // Cards de Estatísticas
    private CardView cardTotalChamados, cardChamadosAbertos, cardChamadosResolvidos;
    private CardView cardTempMedioResolucao, cardAvaliacaoMedia, cardChamadosHoje;

    // TextViews
    private TextView tvTotalChamados, tvTotalChamadosLabel;
    private TextView tvChamadosAbertos, tvChamadosAbertosLabel;
    private TextView tvChamadosResolvidos, tvChamadosResolvidosLabel;
    private TextView tvTempoMedio, tvTempoMedioLabel;
    private TextView tvAvaliacaoMedia, tvAvaliacaoMediaLabel;
    private TextView tvChamadosHoje, tvChamadosHojeLabel;

    // Estatísticas por Categoria
    private TextView tvCategoriaTop1, tvCategoriaTop2, tvCategoriaTop3;
    private TextView tvCategoriaCount1, tvCategoriaCount2, tvCategoriaCount3;

    // Estatísticas por Prioridade
    private TextView tvPrioridadeBaixa, tvPrioridadeMedia, tvPrioridadeAlta, tvPrioridadeCritica;

    // Progress/Percentual
    private TextView tvPercentualResolvidos, tvTaxaSucesso;

    private DatabaseHelper dbHelper;
    private AvaliacaoDAO avaliacaoDAO;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar se é admin
        sessionManager = new SessionManager(this);
        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "❌ Acesso negado! Área restrita a administradores.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_dashboard);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("📊 Dashboard");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inicializarComponentes();
        carregarEstatisticas();
    }

    private void inicializarComponentes() {
        // Cards
        cardTotalChamados = findViewById(R.id.cardTotalChamados);
        cardChamadosAbertos = findViewById(R.id.cardChamadosAbertos);
        cardChamadosResolvidos = findViewById(R.id.cardChamadosResolvidos);
        cardTempMedioResolucao = findViewById(R.id.cardTempoMedio);
        cardAvaliacaoMedia = findViewById(R.id.cardAvaliacaoMedia);
        cardChamadosHoje = findViewById(R.id.cardChamadosHoje);

        // TextViews - Valores
        tvTotalChamados = findViewById(R.id.tvTotalChamados);
        tvChamadosAbertos = findViewById(R.id.tvChamadosAbertos);
        tvChamadosResolvidos = findViewById(R.id.tvChamadosResolvidos);
        tvTempoMedio = findViewById(R.id.tvTempoMedio);
        tvAvaliacaoMedia = findViewById(R.id.tvAvaliacaoMedia);
        tvChamadosHoje = findViewById(R.id.tvChamadosHoje);

        // Categorias Top
        tvCategoriaTop1 = findViewById(R.id.tvCategoriaTop1);
        tvCategoriaTop2 = findViewById(R.id.tvCategoriaTop2);
        tvCategoriaTop3 = findViewById(R.id.tvCategoriaTop3);
        tvCategoriaCount1 = findViewById(R.id.tvCategoriaCount1);
        tvCategoriaCount2 = findViewById(R.id.tvCategoriaCount2);
        tvCategoriaCount3 = findViewById(R.id.tvCategoriaCount3);

        // Prioridades
        tvPrioridadeBaixa = findViewById(R.id.tvPrioridadeBaixa);
        tvPrioridadeMedia = findViewById(R.id.tvPrioridadeMedia);
        tvPrioridadeAlta = findViewById(R.id.tvPrioridadeAlta);
        tvPrioridadeCritica = findViewById(R.id.tvPrioridadeCritica);

        // Percentuais
        tvPercentualResolvidos = findViewById(R.id.tvPercentualResolvidos);
        tvTaxaSucesso = findViewById(R.id.tvTaxaSucesso);

        // Inicializar DAOs
        dbHelper = new DatabaseHelper(this);
        avaliacaoDAO = new AvaliacaoDAO(this);

        Log.d(TAG, "Componentes inicializados");
    }

    private void carregarEstatisticas() {
        Log.d(TAG, "=== CARREGANDO ESTATÍSTICAS ===");

        try {
            // 1. Total de Chamados
            int totalChamados = contarTotalChamados();
            tvTotalChamados.setText(String.valueOf(totalChamados));

            // 2. Chamados Abertos
            int chamadosAbertos = contarChamadosPorStatus("Aberto");
            tvChamadosAbertos.setText(String.valueOf(chamadosAbertos));

            // 3. Chamados Resolvidos
            int chamadosResolvidos = contarChamadosResolvidos();
            tvChamadosResolvidos.setText(String.valueOf(chamadosResolvidos));

            // 4. Chamados de Hoje
            int chamadosHoje = contarChamadosHoje();
            tvChamadosHoje.setText(String.valueOf(chamadosHoje));

            // 5. Tempo Médio de Resolução
            String tempoMedio = calcularTempoMedioResolucao();
            tvTempoMedio.setText(tempoMedio);

            // 6. Avaliação Média
            double avaliacaoMedia = avaliacaoDAO.calcularMediaAvaliacoes();
            DecimalFormat df = new DecimalFormat("0.0");
            tvAvaliacaoMedia.setText(df.format(avaliacaoMedia) + " ⭐");

            // 7. Percentual de Resolvidos
            if (totalChamados > 0) {
                double percentual = (chamadosResolvidos * 100.0) / totalChamados;
                tvPercentualResolvidos.setText(df.format(percentual) + "%");

                // Taxa de sucesso (baseada em avaliações >= 4)
                double taxaSucesso = calcularTaxaSucesso();
                tvTaxaSucesso.setText(df.format(taxaSucesso) + "%");
            } else {
                tvPercentualResolvidos.setText("0%");
                tvTaxaSucesso.setText("0%");
            }

            // 8. Categorias Mais Frequentes
            carregarTopCategorias();

            // 9. Estatísticas por Prioridade
            carregarEstatisticasPrioridade();

            Log.d(TAG, "✅ Estatísticas carregadas com sucesso");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao carregar estatísticas: ", e);
            Toast.makeText(this, "Erro ao carregar estatísticas", Toast.LENGTH_SHORT).show();
        }
    }

    private int contarTotalChamados() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int total = 0;

        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS,
                    null
            );

            if (cursor.moveToFirst()) {
                total = cursor.getInt(0);
            }

            Log.d(TAG, "Total de chamados: " + total);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao contar total de chamados: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return total;
    }

    private int contarChamadosPorStatus(String status) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int count = 0;

        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS +
                            " WHERE " + DatabaseHelper.COLUMN_CHAMADO_STATUS + " = ?",
                    new String[]{status}
            );

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

            Log.d(TAG, "Chamados com status '" + status + "': " + count);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao contar chamados por status: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return count;
    }

    private int contarChamadosResolvidos() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int count = 0;

        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS +
                            " WHERE " + DatabaseHelper.COLUMN_CHAMADO_STATUS + " IN ('Resolvido', 'Fechado')",
                    null
            );

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

            Log.d(TAG, "Chamados resolvidos: " + count);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao contar chamados resolvidos: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return count;
    }

    private int contarChamadosHoje() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int count = 0;

        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS +
                            " WHERE DATE(" + DatabaseHelper.COLUMN_CHAMADO_CREATED_AT + ") = DATE('now')",
                    null
            );

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

            Log.d(TAG, "Chamados de hoje: " + count);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao contar chamados de hoje: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return count;
    }

    private String calcularTempoMedioResolucao() {
        // Simulação - você pode implementar cálculo real baseado em timestamps
        return "2.5 dias";
    }

    private double calcularTaxaSucesso() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double taxa = 0.0;

        try {
            db = dbHelper.getReadableDatabase();

            // Total de avaliações
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.getTableAvaliacoes(),
                    null
            );

            int totalAvaliacoes = 0;
            if (cursor.moveToFirst()) {
                totalAvaliacoes = cursor.getInt(0);
            }
            cursor.close();

            // Avaliações boas (>= 4)
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.getTableAvaliacoes() +
                            " WHERE " + DatabaseHelper.getAvaliacaoNota() + " >= 4",
                    null
            );

            int avaliacoesBoas = 0;
            if (cursor.moveToFirst()) {
                avaliacoesBoas = cursor.getInt(0);
            }

            if (totalAvaliacoes > 0) {
                taxa = (avaliacoesBoas * 100.0) / totalAvaliacoes;
            }

            Log.d(TAG, "Taxa de sucesso: " + taxa + "%");

        } catch (Exception e) {
            Log.e(TAG, "Erro ao calcular taxa de sucesso: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return taxa;
    }

    private void carregarTopCategorias() {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT " + DatabaseHelper.COLUMN_CATEGORIA + ", COUNT(*) as count " +
                            "FROM " + DatabaseHelper.TABLE_CHAMADOS +
                            " GROUP BY " + DatabaseHelper.COLUMN_CATEGORIA +
                            " ORDER BY count DESC LIMIT 3",
                    null
            );

            if (cursor.moveToFirst()) {
                tvCategoriaTop1.setText(cursor.getString(0));
                tvCategoriaCount1.setText(cursor.getInt(1) + " chamados");

                if (cursor.moveToNext()) {
                    tvCategoriaTop2.setText(cursor.getString(0));
                    tvCategoriaCount2.setText(cursor.getInt(1) + " chamados");

                    if (cursor.moveToNext()) {
                        tvCategoriaTop3.setText(cursor.getString(0));
                        tvCategoriaCount3.setText(cursor.getInt(1) + " chamados");
                    }
                }
            }

            Log.d(TAG, "✅ Top categorias carregadas");

        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar top categorias: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
    }

    private void carregarEstatisticasPrioridade() {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            // Baixa
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS +
                            " WHERE " + DatabaseHelper.COLUMN_CHAMADO_PRIORIDADE + " = 'Baixa'",
                    null
            );
            if (cursor.moveToFirst()) {
                tvPrioridadeBaixa.setText(cursor.getInt(0) + " chamados");
            }
            cursor.close();

            // Média
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS +
                            " WHERE " + DatabaseHelper.COLUMN_CHAMADO_PRIORIDADE + " = 'Média'",
                    null
            );
            if (cursor.moveToFirst()) {
                tvPrioridadeMedia.setText(cursor.getInt(0) + " chamados");
            }
            cursor.close();

            // Alta
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS +
                            " WHERE " + DatabaseHelper.COLUMN_CHAMADO_PRIORIDADE + " = 'Alta'",
                    null
            );
            if (cursor.moveToFirst()) {
                tvPrioridadeAlta.setText(cursor.getInt(0) + " chamados");
            }
            cursor.close();

            // Crítica
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS +
                            " WHERE " + DatabaseHelper.COLUMN_CHAMADO_PRIORIDADE + " = 'Crítica'",
                    null
            );
            if (cursor.moveToFirst()) {
                tvPrioridadeCritica.setText(cursor.getInt(0) + " chamados");
            }

            Log.d(TAG, "✅ Estatísticas por prioridade carregadas");

        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar estatísticas de prioridade: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarEstatisticas();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}