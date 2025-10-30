package com.example.helpdeskapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.helpdeskapp.database.DatabaseHelper;
import com.example.helpdeskapp.models.Notificacao;

import java.util.ArrayList;
import java.util.List;

public class NotificacaoDAO {
    private static final String TAG = "NotificacaoDAO";

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public NotificacaoDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    public int contarNaoLidas(long usuarioId) {
        if (database == null || !database.isOpen()) {
            try {
                open();
            } catch (SQLException e) {
                Log.e(TAG, "Erro ao abrir database", e);
                return 0;
            }
        }

        Cursor cursor = database.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_NOTIFICACOES +
                        " WHERE " + DatabaseHelper.COLUMN_NOTIF_USUARIO_ID + " = ? AND " +
                        DatabaseHelper.COLUMN_NOTIF_LIDA + " = 0",
                new String[]{String.valueOf(usuarioId)}
        );

        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }

        return count;
    }

    public long criarNotificacao(Notificacao notificacao) {
        if (database == null || !database.isOpen()) {
            try {
                open();
            } catch (SQLException e) {
                Log.e(TAG, "Erro ao abrir database", e);
                return -1;
            }
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NOTIF_USUARIO_ID, notificacao.getUsuarioId());
        values.put(DatabaseHelper.COLUMN_NOTIF_TIPO, notificacao.getTipo());
        values.put(DatabaseHelper.COLUMN_NOTIF_TITULO, notificacao.getTitulo());
        values.put(DatabaseHelper.COLUMN_NOTIF_MENSAGEM, notificacao.getMensagem());

        if (notificacao.getChamadoId() > 0) {
            values.put(DatabaseHelper.COLUMN_NOTIF_CHAMADO_ID, notificacao.getChamadoId());
        }

        values.put(DatabaseHelper.COLUMN_NOTIF_LIDA, notificacao.isLida() ? 1 : 0);

        return database.insert(DatabaseHelper.TABLE_NOTIFICACOES, null, values);
    }

    public List<Notificacao> buscarNotificacoesUsuario(long usuarioId) {
        List<Notificacao> notificacoes = new ArrayList<>();

        if (database == null || !database.isOpen()) {
            try {
                open();
            } catch (SQLException e) {
                Log.e(TAG, "Erro ao abrir database", e);
                return notificacoes;
            }
        }

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_NOTIFICACOES,
                null,
                DatabaseHelper.COLUMN_NOTIF_USUARIO_ID + " = ?",
                new String[]{String.valueOf(usuarioId)},
                null, null,
                DatabaseHelper.COLUMN_NOTIF_CREATED_AT + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Notificacao notificacao = cursorToNotificacao(cursor);
                if (notificacao != null) {
                    notificacoes.add(notificacao);
                }
            }
            cursor.close();
        }

        return notificacoes;
    }

    public boolean marcarComoLida(long notificacaoId) {
        if (database == null || !database.isOpen()) {
            try {
                open();
            } catch (SQLException e) {
                Log.e(TAG, "Erro ao abrir database", e);
                return false;
            }
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NOTIF_LIDA, 1);

        int rows = database.update(
                DatabaseHelper.TABLE_NOTIFICACOES,
                values,
                DatabaseHelper.COLUMN_NOTIF_ID + " = ?",
                new String[]{String.valueOf(notificacaoId)}
        );

        return rows > 0;
    }

    public boolean marcarTodasComoLidas(long usuarioId) {
        if (database == null || !database.isOpen()) {
            try {
                open();
            } catch (SQLException e) {
                Log.e(TAG, "Erro ao abrir database", e);
                return false;
            }
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NOTIF_LIDA, 1);

        int rows = database.update(
                DatabaseHelper.TABLE_NOTIFICACOES,
                values,
                DatabaseHelper.COLUMN_NOTIF_USUARIO_ID + " = ? AND " + DatabaseHelper.COLUMN_NOTIF_LIDA + " = 0",
                new String[]{String.valueOf(usuarioId)}
        );

        return rows > 0;
    }

    public boolean deletarNotificacao(long notificacaoId) {
        if (database == null || !database.isOpen()) {
            try {
                open();
            } catch (SQLException e) {
                Log.e(TAG, "Erro ao abrir database", e);
                return false;
            }
        }

        int rows = database.delete(
                DatabaseHelper.TABLE_NOTIFICACOES,
                DatabaseHelper.COLUMN_NOTIF_ID + " = ?",
                new String[]{String.valueOf(notificacaoId)}
        );

        return rows > 0;
    }

    public int deletarNotificacoesAntigas(int diasAtras) {
        if (database == null || !database.isOpen()) {
            try {
                open();
            } catch (SQLException e) {
                Log.e(TAG, "Erro ao abrir database", e);
                return 0;
            }
        }

        String whereClause = DatabaseHelper.COLUMN_NOTIF_CREATED_AT + " < date('now', '-" + diasAtras + " days')";

        int rowsDeleted = database.delete(
                DatabaseHelper.TABLE_NOTIFICACOES,
                whereClause,
                null
        );

        Log.d(TAG, "✅ Notificações antigas deletadas: " + rowsDeleted);
        return rowsDeleted;
    }

    private Notificacao cursorToNotificacao(Cursor cursor) {
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

        int createdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTIF_CREATED_AT);
        if (createdIndex != -1) {
            notificacao.setCreatedAt(cursor.getString(createdIndex));
        }

        return notificacao;
    }
}