package com.example.helpdeskapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.helpdeskapp.database.DatabaseHelper;
import com.example.helpdeskapp.models.Notificacao;
import java.util.ArrayList;
import java.util.List;

public class NotificacaoDAO {
    private static final String TAG = "NotificacaoDAO";
    private DatabaseHelper databaseHelper;

    public NotificacaoDAO(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
    }

    // ========== CRIAR NOTIFICAÇÃO ==========
    public long criarNotificacao(Notificacao notificacao) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        long id = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_NOTIF_USUARIO_ID, notificacao.getUsuarioId());
            values.put(DatabaseHelper.COLUMN_NOTIF_TIPO, notificacao.getTipo());
            values.put(DatabaseHelper.COLUMN_NOTIF_TITULO, notificacao.getTitulo());
            values.put(DatabaseHelper.COLUMN_NOTIF_MENSAGEM, notificacao.getMensagem());
            values.put(DatabaseHelper.COLUMN_NOTIF_CHAMADO_ID, notificacao.getChamadoId());
            values.put(DatabaseHelper.COLUMN_NOTIF_LIDA, notificacao.isLida() ? 1 : 0);

            id = db.insert(DatabaseHelper.TABLE_NOTIFICACOES, null, values);

            Log.d(TAG, "✅ Notificação criada: ID " + id);
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao criar notificação: ", e);
        } finally {
            db.close();
        }

        return id;
    }

    // ========== BUSCAR NOTIFICAÇÕES DO USUÁRIO ==========
    public List<Notificacao> buscarNotificacoesUsuario(long usuarioId) {
        List<Notificacao> notificacoes = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try {
            String query = "SELECT * FROM " + DatabaseHelper.TABLE_NOTIFICACOES +
                    " WHERE " + DatabaseHelper.COLUMN_NOTIF_USUARIO_ID + " = ?" +
                    " ORDER BY " + DatabaseHelper.COLUMN_NOTIF_CREATED_AT + " DESC";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(usuarioId)});

            while (cursor.moveToNext()) {
                Notificacao notificacao = criarNotificacaoFromCursor(cursor);
                if (notificacao != null) {
                    notificacoes.add(notificacao);
                }
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao buscar notificações: ", e);
        } finally {
            db.close();
        }

        return notificacoes;
    }

    // ========== BUSCAR NOTIFICAÇÕES NÃO LIDAS ==========
    public List<Notificacao> buscarNotificacoesNaoLidas(long usuarioId) {
        List<Notificacao> notificacoes = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try {
            String query = "SELECT * FROM " + DatabaseHelper.TABLE_NOTIFICACOES +
                    " WHERE " + DatabaseHelper.COLUMN_NOTIF_USUARIO_ID + " = ?" +
                    " AND " + DatabaseHelper.COLUMN_NOTIF_LIDA + " = 0" +
                    " ORDER BY " + DatabaseHelper.COLUMN_NOTIF_CREATED_AT + " DESC";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(usuarioId)});

            while (cursor.moveToNext()) {
                Notificacao notificacao = criarNotificacaoFromCursor(cursor);
                if (notificacao != null) {
                    notificacoes.add(notificacao);
                }
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao buscar notificações não lidas: ", e);
        } finally {
            db.close();
        }

        return notificacoes;
    }

    // ========== CONTAR NOTIFICAÇÕES NÃO LIDAS ==========
    public int contarNaoLidas(long usuarioId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        int count = 0;

        try {
            String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_NOTIFICACOES +
                    " WHERE " + DatabaseHelper.COLUMN_NOTIF_USUARIO_ID + " = ?" +
                    " AND " + DatabaseHelper.COLUMN_NOTIF_LIDA + " = 0";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(usuarioId)});

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao contar notificações não lidas: ", e);
        } finally {
            db.close();
        }

        return count;
    }

    // ========== MARCAR COMO LIDA ==========
    public boolean marcarComoLida(long notificacaoId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        boolean sucesso = false;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_NOTIF_LIDA, 1);

            int rows = db.update(
                    DatabaseHelper.TABLE_NOTIFICACOES,
                    values,
                    DatabaseHelper.COLUMN_NOTIF_ID + " = ?",
                    new String[]{String.valueOf(notificacaoId)}
            );

            sucesso = rows > 0;
            Log.d(TAG, "✅ Notificação marcada como lida: " + notificacaoId);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao marcar como lida: ", e);
        } finally {
            db.close();
        }

        return sucesso;
    }

    // ========== MARCAR TODAS COMO LIDAS ==========
    public boolean marcarTodasComoLidas(long usuarioId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        boolean sucesso = false;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_NOTIF_LIDA, 1);

            int rows = db.update(
                    DatabaseHelper.TABLE_NOTIFICACOES,
                    values,
                    DatabaseHelper.COLUMN_NOTIF_USUARIO_ID + " = ? AND " +
                            DatabaseHelper.COLUMN_NOTIF_LIDA + " = 0",
                    new String[]{String.valueOf(usuarioId)}
            );

            sucesso = rows > 0;
            Log.d(TAG, "✅ Todas notificações marcadas como lidas: " + rows);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao marcar todas como lidas: ", e);
        } finally {
            db.close();
        }

        return sucesso;
    }

    // ========== DELETAR NOTIFICAÇÃO ==========
    public boolean deletarNotificacao(long notificacaoId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        boolean sucesso = false;

        try {
            int rows = db.delete(
                    DatabaseHelper.TABLE_NOTIFICACOES,
                    DatabaseHelper.COLUMN_NOTIF_ID + " = ?",
                    new String[]{String.valueOf(notificacaoId)}
            );

            sucesso = rows > 0;
            Log.d(TAG, "✅ Notificação deletada: " + notificacaoId);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao deletar notificação: ", e);
        } finally {
            db.close();
        }

        return sucesso;
    }

    // ========== DELETAR NOTIFICAÇÕES ANTIGAS ==========
    public int deletarNotificacoesAntigas(int diasAtras) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int rowsDeleted = 0;

        try {
            String whereClause = DatabaseHelper.COLUMN_NOTIF_CREATED_AT +
                    " < date('now', '-" + diasAtras + " days')";

            // CORRIGIDO: Usar delete ao invés de execSQL
            rowsDeleted = db.delete(
                    DatabaseHelper.TABLE_NOTIFICACOES,
                    whereClause,
                    null
            );

            Log.d(TAG, "✅ Notificações antigas deletadas: " + rowsDeleted);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao deletar notificações antigas: ", e);
        } finally {
            db.close();
        }

        return rowsDeleted;
    }

    // ========== CRIAR NOTIFICACAO FROM CURSOR ==========
    private Notificacao criarNotificacaoFromCursor(Cursor cursor) {
        try {
            Notificacao notificacao = new Notificacao();

            notificacao.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIF_ID)));
            notificacao.setUsuarioId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIF_USUARIO_ID)));
            notificacao.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIF_TIPO)));
            notificacao.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIF_TITULO)));
            notificacao.setMensagem(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIF_MENSAGEM)));

            int chamadoIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTIF_CHAMADO_ID);
            if (chamadoIdIndex != -1 && !cursor.isNull(chamadoIdIndex)) {
                notificacao.setChamadoId(cursor.getLong(chamadoIdIndex));
            }

            notificacao.setLida(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIF_LIDA)) == 1);
            notificacao.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIF_CREATED_AT)));

            return notificacao;

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao criar notificação do cursor: ", e);
            return null;
        }
    }
}