package com.example.helpdeskapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.helpdeskapp.database.DatabaseHelper;
import com.example.helpdeskapp.models.Avaliacao;

public class AvaliacaoDAO {
    private static final String TAG = "AvaliacaoDAO";
    private DatabaseHelper databaseHelper;

    public AvaliacaoDAO(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
    }

    // Inserir avaliação
    public long inserirAvaliacao(Avaliacao avaliacao) {
        SQLiteDatabase db = null;
        long id = -1;

        try {
            db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.getAvaliacaoChamadoId(), avaliacao.getChamadoId());
            values.put(DatabaseHelper.getAvaliacaoNota(), avaliacao.getNota());
            values.put(DatabaseHelper.getAvaliacaoComentario(), avaliacao.getComentario());

            id = db.insert(DatabaseHelper.getTableAvaliacoes(), null, values);
            Log.d(TAG, "✅ Avaliação inserida com ID: " + id);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao inserir avaliação: ", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return id;
    }

    // Buscar avaliação por chamado
    public Avaliacao buscarAvaliacaoPorChamado(long chamadoId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        Avaliacao avaliacao = null;

        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.query(
                    DatabaseHelper.getTableAvaliacoes(),
                    null,
                    DatabaseHelper.getAvaliacaoChamadoId() + " = ?",
                    new String[]{String.valueOf(chamadoId)},
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                avaliacao = new Avaliacao();
                avaliacao.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.getAvaliacaoId())));
                avaliacao.setChamadoId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.getAvaliacaoChamadoId())));
                avaliacao.setNota(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.getAvaliacaoNota())));
                avaliacao.setComentario(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getAvaliacaoComentario())));
                avaliacao.setDataAvaliacao(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getAvaliacaoData())));

                Log.d(TAG, "✅ Avaliação encontrada: " + avaliacao.getNota() + " estrelas");
            } else {
                Log.d(TAG, "❌ Nenhuma avaliação encontrada para o chamado " + chamadoId);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao buscar avaliação: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return avaliacao;
    }

    // Verificar se chamado já foi avaliado
    public boolean chamadoJaAvaliado(long chamadoId) {
        return buscarAvaliacaoPorChamado(chamadoId) != null;
    }

    // Calcular média de avaliações
    public double calcularMediaAvaliacoes() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double media = 0.0;

        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT AVG(" + DatabaseHelper.getAvaliacaoNota() + ") as media FROM " +
                            DatabaseHelper.getTableAvaliacoes(), null
            );

            if (cursor.moveToFirst()) {
                media = cursor.getDouble(cursor.getColumnIndexOrThrow("media"));
                Log.d(TAG, "✅ Média de avaliações: " + media);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao calcular média: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return media;
    }
}