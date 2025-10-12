package com.example.helpdeskapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.helpdeskapp.database.DatabaseHelper;
import com.example.helpdeskapp.models.Auditoria;
import java.util.ArrayList;
import java.util.List;

public class AuditoriaDAO {
    private static final String TAG = "AuditoriaDAO";
    private DatabaseHelper databaseHelper;

    public AuditoriaDAO(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
    }

    // ========== REGISTRAR AÇÃO ==========
    public long registrarAcao(Auditoria auditoria) {
        SQLiteDatabase db = null;
        long id = -1;

        try {
            db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.getAuditUsuarioId(), auditoria.getUsuarioId());
            values.put(DatabaseHelper.getAuditAcao(), auditoria.getAcao());
            values.put(DatabaseHelper.getAuditEntidade(), auditoria.getEntidade());
            values.put(DatabaseHelper.getAuditEntidadeId(), auditoria.getEntidadeId());
            values.put(DatabaseHelper.getAuditDescricao(), auditoria.getDescricao());
            values.put(DatabaseHelper.getAuditIp(), auditoria.getIp());
            values.put(DatabaseHelper.getAuditDispositivo(), auditoria.getDispositivo());

            id = db.insert(DatabaseHelper.getTableAuditoria(), null, values);
            Log.d(TAG, "✅ Ação registrada com ID: " + id);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao registrar ação: ", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return id;
    }

    // ========== BUSCAR TODAS AS AÇÕES ==========
    public List<Auditoria> buscarTodasAcoes() {
        List<Auditoria> auditorias = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = databaseHelper.getReadableDatabase();

            String query = "SELECT a.*, u." + DatabaseHelper.COLUMN_USER_NOME + " as nome_usuario " +
                    "FROM " + DatabaseHelper.getTableAuditoria() + " a " +
                    "LEFT JOIN " + DatabaseHelper.TABLE_USUARIOS + " u " +
                    "ON a." + DatabaseHelper.getAuditUsuarioId() + " = u." + DatabaseHelper.COLUMN_USER_ID +
                    " ORDER BY a." + DatabaseHelper.getAuditData() + " DESC";

            cursor = db.rawQuery(query, null);

            while (cursor.moveToNext()) {
                Auditoria auditoria = criarAuditoriaFromCursor(cursor);
                if (auditoria != null) {
                    auditorias.add(auditoria);
                }
            }

            Log.d(TAG, "✅ " + auditorias.size() + " ações encontradas");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao buscar ações: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return auditorias;
    }

    // ========== BUSCAR AÇÕES POR USUÁRIO ==========
    public List<Auditoria> buscarAcoesPorUsuario(long usuarioId) {
        List<Auditoria> auditorias = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = databaseHelper.getReadableDatabase();

            String query = "SELECT a.*, u." + DatabaseHelper.COLUMN_USER_NOME + " as nome_usuario " +
                    "FROM " + DatabaseHelper.getTableAuditoria() + " a " +
                    "LEFT JOIN " + DatabaseHelper.TABLE_USUARIOS + " u " +
                    "ON a." + DatabaseHelper.getAuditUsuarioId() + " = u." + DatabaseHelper.COLUMN_USER_ID +
                    " WHERE a." + DatabaseHelper.getAuditUsuarioId() + " = ? " +
                    "ORDER BY a." + DatabaseHelper.getAuditData() + " DESC";

            cursor = db.rawQuery(query, new String[]{String.valueOf(usuarioId)});

            while (cursor.moveToNext()) {
                Auditoria auditoria = criarAuditoriaFromCursor(cursor);
                if (auditoria != null) {
                    auditorias.add(auditoria);
                }
            }

            Log.d(TAG, "✅ " + auditorias.size() + " ações encontradas para usuário " + usuarioId);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao buscar ações por usuário: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return auditorias;
    }

    // ========== BUSCAR AÇÕES POR ENTIDADE ==========
    public List<Auditoria> buscarAcoesPorEntidade(String entidade, long entidadeId) {
        List<Auditoria> auditorias = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = databaseHelper.getReadableDatabase();

            String query = "SELECT a.*, u." + DatabaseHelper.COLUMN_USER_NOME + " as nome_usuario " +
                    "FROM " + DatabaseHelper.getTableAuditoria() + " a " +
                    "LEFT JOIN " + DatabaseHelper.TABLE_USUARIOS + " u " +
                    "ON a." + DatabaseHelper.getAuditUsuarioId() + " = u." + DatabaseHelper.COLUMN_USER_ID +
                    " WHERE a." + DatabaseHelper.getAuditEntidade() + " = ? " +
                    "AND a." + DatabaseHelper.getAuditEntidadeId() + " = ? " +
                    "ORDER BY a." + DatabaseHelper.getAuditData() + " DESC";

            cursor = db.rawQuery(query, new String[]{entidade, String.valueOf(entidadeId)});

            while (cursor.moveToNext()) {
                Auditoria auditoria = criarAuditoriaFromCursor(cursor);
                if (auditoria != null) {
                    auditorias.add(auditoria);
                }
            }

            Log.d(TAG, "✅ " + auditorias.size() + " ações encontradas para " + entidade + " #" + entidadeId);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao buscar ações por entidade: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return auditorias;
    }

    // ========== LIMPAR LOGS ANTIGOS ==========
    public int limparLogsAntigos(int diasParaManter) {
        SQLiteDatabase db = null;
        int rowsDeleted = 0;

        try {
            db = databaseHelper.getWritableDatabase();

            String query = "DELETE FROM " + DatabaseHelper.getTableAuditoria() +
                    " WHERE " + DatabaseHelper.getAuditData() + " < datetime('now', '-" + diasParaManter + " days')";

            db.execSQL(query);

            // Contar quantos foram deletados (aproximado)
            Cursor cursor = db.rawQuery("SELECT changes()", null);
            if (cursor.moveToFirst()) {
                rowsDeleted = cursor.getInt(0);
            }
            cursor.close();

            Log.d(TAG, "✅ " + rowsDeleted + " logs antigos deletados");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao limpar logs antigos: ", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return rowsDeleted;
    }

    private Auditoria criarAuditoriaFromCursor(Cursor cursor) {
        try {
            Auditoria auditoria = new Auditoria();
            auditoria.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.getAuditId())));
            auditoria.setUsuarioId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.getAuditUsuarioId())));
            auditoria.setAcao(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getAuditAcao())));
            auditoria.setEntidade(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getAuditEntidade())));
            auditoria.setEntidadeId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.getAuditEntidadeId())));
            auditoria.setDescricao(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getAuditDescricao())));
            auditoria.setIp(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getAuditIp())));
            auditoria.setDispositivo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getAuditDispositivo())));
            auditoria.setDataAcao(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getAuditData())));

            // Nome do usuário (do JOIN)
            int nomeIndex = cursor.getColumnIndex("nome_usuario");
            if (nomeIndex != -1) {
                auditoria.setNomeUsuario(cursor.getString(nomeIndex));
            }

            return auditoria;
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao criar auditoria: ", e);
            return null;
        }
    }
}