package com.example.helpdeskapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.helpdeskapp.database.DatabaseHelper;
import com.example.helpdeskapp.models.Chamado;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChamadoDAO {
    private static final String TAG = "ChamadoDAO";
    private DatabaseHelper dbHelper;

    public ChamadoDAO(Context context) {
        Log.d(TAG, "=== INICIANDO ChamadoDAO ===");
        try {
            dbHelper = new DatabaseHelper(context);
            Log.d(TAG, "✅ DatabaseHelper criado com sucesso");
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO ao criar DatabaseHelper: ", e);
        }
    }

    // MÉTODO ADICIONADO: debugInfo
    public void debugInfo() {
        if (dbHelper != null) {
            dbHelper.debugInfo();
        } else {
            Log.e(TAG, "❌ DatabaseHelper é null!");
        }
    }

    public long abrirChamado(Chamado chamado) {
        Log.d(TAG, "=== ABRINDO NOVO CHAMADO ===");
        if (chamado == null) {
            Log.e(TAG, "❌ ERRO: Chamado é null!");
            return -1;
        }

        SQLiteDatabase database = null;
        try {
            database = dbHelper.getWritableDatabase();
            logChamadoData(chamado);

            String timestamp = getCurrentTimestamp();
            ContentValues values = createContentValues(chamado, timestamp);

            Log.d(TAG, "Inserindo na tabela: " + DatabaseHelper.TABLE_CHAMADOS);
            long result = database.insert(DatabaseHelper.TABLE_CHAMADOS, null, values);

            if (result > 0) {
                Log.d(TAG, "✅ SUCESSO! Chamado inserido com ID: " + result);
                verificarInsercao(database, result);

                // Atualizar o número do chamado
                chamado.setId(result);
                chamado.setNumero(chamado.getProtocoloFormatado());
            } else {
                Log.e(TAG, "❌ FALHA! Insert retornou: " + result);
            }
            return result;
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO CRÍTICO ao abrir chamado: ", e);
            return -1;
        } finally {
            closeDatabase(database);
        }
    }

    public List<Chamado> listarChamadosPorCliente(long clienteId) {
        Log.d(TAG, "=== LISTANDO CHAMADOS POR CLIENTE ===");
        Log.d(TAG, "Cliente ID: " + clienteId);
        List<Chamado> chamados = new ArrayList<>();
        SQLiteDatabase database = null;

        try {
            database = dbHelper.getReadableDatabase();
            int totalChamados = getTotalChamadosPorCliente(database, clienteId);
            Log.d(TAG, "Total de chamados para cliente " + clienteId + ": " + totalChamados);

            if (totalChamados == 0) {
                Log.d(TAG, "⚠️ Nenhum chamado encontrado para o cliente " + clienteId);
                return chamados;
            }

            chamados = buscarChamadosDoCliente(database, clienteId);
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO CRÍTICO ao listar chamados: ", e);
        } finally {
            closeDatabase(database);
        }

        Log.d(TAG, "✅ Total de chamados retornados: " + chamados.size());
        return chamados;
    }

    public Chamado buscarChamadoPorId(long id) {
        Log.d(TAG, "=== BUSCANDO CHAMADO POR ID ===");
        Log.d(TAG, "ID: " + id);
        SQLiteDatabase database = null;
        Chamado chamado = null;

        try {
            database = dbHelper.getReadableDatabase();
            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_CHAMADOS,
                    null,
                    DatabaseHelper.COLUMN_CHAMADO_ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                chamado = criarChamadoFromCursor(cursor);
                if (chamado != null) {
                    Log.d(TAG, "✅ Chamado encontrado: " + chamado.getProtocoloFormatado() + " - " + chamado.getTitulo());
                }
            } else {
                Log.d(TAG, "❌ Chamado não encontrado");
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO ao buscar chamado por ID: ", e);
        } finally {
            closeDatabase(database);
        }

        return chamado;
    }

    public boolean atualizarStatus(long id, String novoStatus) {
        Log.d(TAG, "=== ATUALIZANDO STATUS DO CHAMADO ===");
        Log.d(TAG, "ID: " + id + ", Novo status: " + novoStatus);
        SQLiteDatabase database = null;
        boolean sucesso = false;

        try {
            database = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_CHAMADO_STATUS, novoStatus);
            values.put(DatabaseHelper.COLUMN_CHAMADO_UPDATED_AT, getCurrentTimestamp());

            int rowsAffected = database.update(
                    DatabaseHelper.TABLE_CHAMADOS,
                    values,
                    DatabaseHelper.COLUMN_CHAMADO_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );

            sucesso = rowsAffected > 0;
            Log.d(TAG, sucesso ? "✅ Status atualizado" : "❌ Falha na atualização");
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO ao atualizar status: ", e);
        } finally {
            closeDatabase(database);
        }

        return sucesso;
    }

    private void logChamadoData(Chamado chamado) {
        Log.d(TAG, "Dados do chamado:");
        Log.d(TAG, " - Protocolo: " + chamado.getProtocoloFormatado());
        Log.d(TAG, " - Título: " + chamado.getTitulo());
        Log.d(TAG, " - Descrição: " + chamado.getDescricao());
        Log.d(TAG, " - Status: " + chamado.getStatus());
        Log.d(TAG, " - Prioridade: " + chamado.getPrioridade());
        Log.d(TAG, " - Cliente ID: " + chamado.getClienteId());
    }

    private ContentValues createContentValues(Chamado chamado, String timestamp) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CHAMADO_NUMERO, chamado.getProtocoloFormatado());
        values.put(DatabaseHelper.COLUMN_CHAMADO_TITULO, chamado.getTitulo());
        values.put(DatabaseHelper.COLUMN_CHAMADO_DESCRICAO, chamado.getDescricao());
        values.put(DatabaseHelper.COLUMN_CATEGORIA, chamado.getCategoria()); // CORRIGIDO
        values.put(DatabaseHelper.COLUMN_CHAMADO_STATUS, chamado.getStatus());
        values.put(DatabaseHelper.COLUMN_CHAMADO_PRIORIDADE, chamado.getPrioridade());
        values.put(DatabaseHelper.COLUMN_CHAMADO_CLIENTE_ID, chamado.getClienteId());
        values.put(DatabaseHelper.COLUMN_CHAMADO_CREATED_AT, timestamp);
        values.put(DatabaseHelper.COLUMN_CHAMADO_UPDATED_AT, timestamp);
        return values;
    }

    private void verificarInsercao(SQLiteDatabase database, long id) {
        try {
            Cursor cursor = database.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS + " WHERE " +
                            DatabaseHelper.COLUMN_CHAMADO_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
            if (cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                Log.d(TAG, "Verificação: " + count + " registro(s) encontrado(s)");
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO na verificação: ", e);
        }
    }

    private int getTotalChamadosPorCliente(SQLiteDatabase database, long clienteId) {
        try {
            Cursor cursor = database.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS + " WHERE " +
                            DatabaseHelper.COLUMN_CHAMADO_CLIENTE_ID + " = ?",
                    new String[]{String.valueOf(clienteId)}
            );
            if (cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                cursor.close();
                return count;
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO ao contar chamados: ", e);
        }
        return 0;
    }

    private List<Chamado> buscarChamadosDoCliente(SQLiteDatabase database, long clienteId) {
        List<Chamado> chamados = new ArrayList<>();
        try {
            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_CHAMADOS,
                    null,
                    DatabaseHelper.COLUMN_CHAMADO_CLIENTE_ID + " = ?",
                    new String[]{String.valueOf(clienteId)},
                    null, null,
                    DatabaseHelper.COLUMN_CHAMADO_CREATED_AT + " DESC"
            );

            while (cursor.moveToNext()) {
                Chamado chamado = criarChamadoFromCursor(cursor);
                if (chamado != null) {
                    chamados.add(chamado);
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO ao buscar chamados do cliente: ", e);
        }
        return chamados;
    }

    private Chamado criarChamadoFromCursor(Cursor cursor) {
        try {
            Chamado chamado = new Chamado();
            chamado.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_ID)));

            // Número do chamado
            int numeroIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CHAMADO_NUMERO);
            if (numeroIndex != -1) {
                chamado.setNumero(cursor.getString(numeroIndex));
            }

            chamado.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_TITULO)));
            chamado.setDescricao(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_DESCRICAO)));

            // Categoria
            int categoriaIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORIA);
            if (categoriaIndex != -1) {
                chamado.setCategoria(cursor.getString(categoriaIndex));
            }

            chamado.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_STATUS)));
            chamado.setPrioridade(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_PRIORIDADE)));
            chamado.setClienteId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_CLIENTE_ID)));

            // Converter timestamp para Date
            String createdAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_CREATED_AT));
            if (createdAt != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    chamado.setDataCriacao(sdf.parse(createdAt));
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao converter data: " + createdAt, e);
                    chamado.setDataCriacao(new Date());
                }
            }

            return chamado;
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO ao criar chamado do cursor: ", e);
            return null;
        }
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void closeDatabase(SQLiteDatabase database) {
        if (database != null && database.isOpen()) {
            try {
                database.close();
                Log.d(TAG, "✅ Database fechado");
            } catch (Exception e) {
                Log.e(TAG, "❌ ERRO ao fechar database: ", e);
            }
        }
    }
}