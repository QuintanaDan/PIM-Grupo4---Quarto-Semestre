package com.example.helpdeskapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.helpdeskapp.database.DatabaseHelper;
import com.example.helpdeskapp.models.Anexo;
import java.util.ArrayList;
import java.util.List;

public class AnexoDAO {
    private static final String TAG = "AnexoDAO";
    private DatabaseHelper databaseHelper;

    public AnexoDAO(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
    }

    // ========== INSERIR ANEXO ==========
    public long inserirAnexo(Anexo anexo) {
        SQLiteDatabase db = null;
        long id = -1;

        try {
            db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.getAnexoChamadoId(), anexo.getChamadoId());
            values.put(DatabaseHelper.getAnexoNomeArquivo(), anexo.getNomeArquivo());
            values.put(DatabaseHelper.getAnexoCaminho(), anexo.getCaminho());
            values.put(DatabaseHelper.getAnexoTipo(), anexo.getTipo());
            values.put(DatabaseHelper.getAnexoTamanho(), anexo.getTamanho());

            id = db.insert(DatabaseHelper.getTableAnexos(), null, values);
            Log.d(TAG, "✅ Anexo inserido com ID: " + id);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao inserir anexo: ", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return id;
    }

    // ========== BUSCAR ANEXOS POR CHAMADO ==========
    public List<Anexo> buscarAnexosPorChamado(long chamadoId) {
        List<Anexo> anexos = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.query(
                    DatabaseHelper.getTableAnexos(),
                    null,
                    DatabaseHelper.getAnexoChamadoId() + " = ?",
                    new String[]{String.valueOf(chamadoId)},
                    null, null,
                    DatabaseHelper.getAnexoDataUpload() + " DESC"
            );

            Log.d(TAG, "🔍 Buscando anexos para chamado ID: " + chamadoId);
            Log.d(TAG, "🔍 Registros encontrados: " + cursor.getCount());

            while (cursor.moveToNext()) {
                Anexo anexo = new Anexo();
                anexo.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.getAnexoId())));
                anexo.setChamadoId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.getAnexoChamadoId())));
                anexo.setNomeArquivo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getAnexoNomeArquivo())));
                anexo.setCaminho(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getAnexoCaminho())));
                anexo.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getAnexoTipo())));
                anexo.setTamanho(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.getAnexoTamanho())));
                anexo.setDataUpload(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getAnexoDataUpload())));

                anexos.add(anexo);
                Log.d(TAG, "📎 Anexo carregado: " + anexo.getNomeArquivo());
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao buscar anexos: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        Log.d(TAG, "✅ Total de anexos encontrados: " + anexos.size());
        return anexos;
    }

    // ========== DELETAR ANEXO ==========
    public boolean deletarAnexo(long anexoId) {
        SQLiteDatabase db = null;
        boolean sucesso = false;

        try {
            db = databaseHelper.getWritableDatabase();
            int rowsAffected = db.delete(
                    DatabaseHelper.getTableAnexos(),
                    DatabaseHelper.getAnexoId() + " = ?",
                    new String[]{String.valueOf(anexoId)}
            );

            sucesso = rowsAffected > 0;
            Log.d(TAG, sucesso ? "✅ Anexo deletado" : "❌ Falha ao deletar anexo");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao deletar anexo: ", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return sucesso;
    }

    // ========== CONTAR ANEXOS POR CHAMADO ==========
    public int contarAnexosPorChamado(long chamadoId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int count = 0;

        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.getTableAnexos() +
                            " WHERE " + DatabaseHelper.getAnexoChamadoId() + " = ?",
                    new String[]{String.valueOf(chamadoId)}
            );

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao contar anexos: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return count;
    }
}