package com.example.helpdeskapp.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.helpdeskapp.database.DatabaseHelper;
import com.example.helpdeskapp.models.Estatistica;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EstatisticaDAO {
    private static final String TAG = "EstatisticaDAO";
    private DatabaseHelper databaseHelper;

    public EstatisticaDAO(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
    }

    public Estatistica buscarEstatisticasGerais() {
        Estatistica stats = new Estatistica();
        SQLiteDatabase db = null;

        try {
            db = databaseHelper.getReadableDatabase();

            // Total de chamados
            stats.setTotalChamados(contarTotal(db, DatabaseHelper.TABLE_CHAMADOS));

            // Chamados por status
            stats.setChamadosAbertos(contarPorStatus(db, "Aberto"));
            stats.setChamadosEmAndamento(contarPorStatus(db, "Em Andamento"));
            stats.setChamadosFechados(contarPorStatus(db, "Fechado"));
            stats.setChamadosResolvidos(contarPorStatus(db, "Resolvido"));

            // Total de usuários
            stats.setTotalUsuarios(contarTotal(db, DatabaseHelper.TABLE_USUARIOS));
            stats.setTotalClientes(contarUsuariosPorTipo(db, 0));
            stats.setTotalAdmins(contarUsuariosPorTipo(db, 1));

            // Chamados por período
            stats.setChamadosHoje(contarChamadosHoje(db));
            stats.setChamadosSemana(contarChamadosSemana(db));
            stats.setChamadosMes(contarChamadosMes(db));

            // Avaliações
            calcularEstatisticasAvaliacoes(db, stats);

            // Prioridades
            stats.setPrioridadeAlta(contarPorPrioridade(db, "Alta"));
            stats.setPrioridadeMedia(contarPorPrioridade(db, "Média"));
            stats.setPrioridadeBaixa(contarPorPrioridade(db, "Baixa"));

            Log.d(TAG, "✅ Estatísticas carregadas: " + stats.toString());

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao buscar estatísticas: ", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return stats;
    }

    private int contarTotal(SQLiteDatabase db, String tabela) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + tabela, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private int contarPorStatus(SQLiteDatabase db, String status) {
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS +
                " WHERE " + DatabaseHelper.COLUMN_CHAMADO_STATUS + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{status});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private int contarUsuariosPorTipo(SQLiteDatabase db, int tipo) {
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USUARIOS +
                " WHERE " + DatabaseHelper.COLUMN_USER_TIPO + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(tipo)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private int contarPorPrioridade(SQLiteDatabase db, String prioridade) {
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS +
                " WHERE " + DatabaseHelper.COLUMN_CHAMADO_PRIORIDADE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{prioridade});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private int contarChamadosHoje(SQLiteDatabase db) {
        String hoje = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS +
                " WHERE DATE(" + DatabaseHelper.COLUMN_CHAMADO_CREATED_AT + ") = ?";
        Cursor cursor = db.rawQuery(query, new String[]{hoje});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private int contarChamadosSemana(SQLiteDatabase db) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        String semanaAtras = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(calendar.getTime());

        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS +
                " WHERE DATE(" + DatabaseHelper.COLUMN_CHAMADO_CREATED_AT + ") >= ?";
        Cursor cursor = db.rawQuery(query, new String[]{semanaAtras});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private int contarChamadosMes(SQLiteDatabase db) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        String mesAtras = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(calendar.getTime());

        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS +
                " WHERE DATE(" + DatabaseHelper.COLUMN_CHAMADO_CREATED_AT + ") >= ?";
        Cursor cursor = db.rawQuery(query, new String[]{mesAtras});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private void calcularEstatisticasAvaliacoes(SQLiteDatabase db, Estatistica stats) {
        String query = "SELECT " + DatabaseHelper.COLUMN_AVALIACAO_NOTA +
                " FROM " + DatabaseHelper.TABLE_AVALIACOES;
        Cursor cursor = db.rawQuery(query, null);

        int positivas = 0;
        int negativas = 0;
        float somaNotas = 0;
        int totalAvaliacoes = 0;

        while (cursor.moveToNext()) {
            int nota = cursor.getInt(0);
            somaNotas += nota;
            totalAvaliacoes++;

            if (nota >= 4) {
                positivas++;
            } else if (nota <= 2) {
                negativas++;
            }
        }
        cursor.close();

        stats.setAvaliacoesPositivas(positivas);
        stats.setAvaliacoesNegativas(negativas);

        if (totalAvaliacoes > 0) {
            stats.setMediaAvaliacoes(somaNotas / totalAvaliacoes);
        } else {
            stats.setMediaAvaliacoes(0);
        }
    }
}