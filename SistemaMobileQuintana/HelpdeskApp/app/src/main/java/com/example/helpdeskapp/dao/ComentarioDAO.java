package com.example.helpdeskapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.helpdeskapp.database.DatabaseHelper;
import com.example.helpdeskapp.models.Comentario;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ComentarioDAO {
    private static final String TAG = "ComentarioDAO";
    private DatabaseHelper databaseHelper;

    public ComentarioDAO(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
    }

    // ========== INSERIR COMENTÁRIO ==========
    public long inserirComentario(Comentario comentario) {
        SQLiteDatabase db = null;
        long id = -1;

        try {
            db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.getComentarioChamadoId(), comentario.getChamadoId());
            values.put(DatabaseHelper.getComentarioUsuarioId(), comentario.getUsuarioId());
            values.put(DatabaseHelper.getComentarioTexto(), comentario.getTexto());
            values.put(DatabaseHelper.getComentarioTipo(), comentario.getTipo());

            // Data atual se não especificada
            if (comentario.getDataCriacao() == null || comentario.getDataCriacao().isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                values.put(DatabaseHelper.getComentarioDataCriacao(), sdf.format(new Date()));
            } else {
                values.put(DatabaseHelper.getComentarioDataCriacao(), comentario.getDataCriacao());
            }

            id = db.insert(DatabaseHelper.getTableComentarios(), null, values);
            Log.d(TAG, "✅ Comentário inserido com ID: " + id);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao inserir comentário: ", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return id;
    }

    // ========== BUSCAR COMENTÁRIOS POR CHAMADO ==========
    public List<Comentario> buscarComentariosPorChamado(long chamadoId) {
        List<Comentario> comentarios = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        String query = "SELECT " +
                "c." + DatabaseHelper.getComentarioId() + ", " +
                "c." + DatabaseHelper.getComentarioChamadoId() + ", " +
                "c." + DatabaseHelper.getComentarioUsuarioId() + ", " +
                "c." + DatabaseHelper.getComentarioTexto() + ", " +
                "c." + DatabaseHelper.getComentarioDataCriacao() + ", " +
                "c." + DatabaseHelper.getComentarioTipo() + ", " +
                "u." + DatabaseHelper.COLUMN_USER_NOME + " as usuario_nome " +
                "FROM " + DatabaseHelper.getTableComentarios() + " c " +
                "LEFT JOIN " + DatabaseHelper.TABLE_USUARIOS + " u " +
                "ON c." + DatabaseHelper.getComentarioUsuarioId() + " = u." + DatabaseHelper.COLUMN_USER_ID + " " +
                "WHERE c." + DatabaseHelper.getComentarioChamadoId() + " = ? " +
                "ORDER BY c." + DatabaseHelper.getComentarioDataCriacao() + " ASC";

        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.rawQuery(query, new String[]{String.valueOf(chamadoId)});

            Log.d(TAG, "🔍 Query executada: " + query);
            Log.d(TAG, "🔍 Buscando comentários para chamado ID: " + chamadoId);
            Log.d(TAG, "🔍 Registros encontrados: " + cursor.getCount());

            while (cursor.moveToNext()) {
                Comentario comentario = new Comentario();
                comentario.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.getComentarioId())));
                comentario.setChamadoId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.getComentarioChamadoId())));
                comentario.setUsuarioId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.getComentarioUsuarioId())));
                comentario.setTexto(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getComentarioTexto())));
                comentario.setDataCriacao(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getComentarioDataCriacao())));
                comentario.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getComentarioTipo())));

                // Nome do usuário (pode ser null se LEFT JOIN não encontrar)
                int usuarioNomeIndex = cursor.getColumnIndex("usuario_nome");
                if (usuarioNomeIndex != -1 && !cursor.isNull(usuarioNomeIndex)) {
                    comentario.setNomeUsuario(cursor.getString(usuarioNomeIndex));
                } else {
                    comentario.setNomeUsuario("Usuário Desconhecido");
                }

                comentarios.add(comentario);
                Log.d(TAG, "📝 Comentário carregado: ID=" + comentario.getId() +
                        ", Texto=" + comentario.getTexto() +
                        ", Usuario=" + comentario.getNomeUsuario());
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao buscar comentários: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        Log.d(TAG, "✅ Total de comentários encontrados: " + comentarios.size());
        return comentarios;
    }

    // ========== ATUALIZAR COMENTÁRIO ==========
    public boolean atualizarComentario(Comentario comentario) {
        SQLiteDatabase db = null;
        boolean sucesso = false;

        try {
            db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.getComentarioTexto(), comentario.getTexto());
            values.put(DatabaseHelper.getComentarioTipo(), comentario.getTipo());

            int rowsAffected = db.update(
                    DatabaseHelper.getTableComentarios(),
                    values,
                    DatabaseHelper.getComentarioId() + " = ?",
                    new String[]{String.valueOf(comentario.getId())}
            );

            sucesso = rowsAffected > 0;
            Log.d(TAG, sucesso ? "✅ Comentário atualizado" : "❌ Falha ao atualizar comentário");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao atualizar comentário: ", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return sucesso;
    }

    // ========== DELETAR COMENTÁRIO ==========
    public boolean deletarComentario(long comentarioId) {
        SQLiteDatabase db = null;
        boolean sucesso = false;

        try {
            db = databaseHelper.getWritableDatabase();
            int rowsAffected = db.delete(
                    DatabaseHelper.getTableComentarios(),
                    DatabaseHelper.getComentarioId() + " = ?",
                    new String[]{String.valueOf(comentarioId)}
            );

            sucesso = rowsAffected > 0;
            Log.d(TAG, sucesso ? "✅ Comentário deletado" : "❌ Falha ao deletar comentário");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao deletar comentário: ", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return sucesso;
    }
}