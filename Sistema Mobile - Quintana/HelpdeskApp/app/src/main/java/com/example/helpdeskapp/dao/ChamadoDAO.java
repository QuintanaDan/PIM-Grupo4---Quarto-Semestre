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
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public ChamadoDAO(Context context) {
        Log.d(TAG, "=== INICIANDO ChamadoDAO ===");
        try {
            dbHelper = new DatabaseHelper(context);
            Log.d(TAG, "DatabaseHelper criado com sucesso");
        } catch (Exception e) {
            Log.e(TAG, "ERRO ao criar DatabaseHelper: ", e);
        }
    }

    public void open() {
        Log.d(TAG, "=== ABRINDO BANCO DE DADOS ===");
        try {
            if (dbHelper == null) {
                Log.e(TAG, "ERRO: DatabaseHelper é null!");
                throw new RuntimeException("DatabaseHelper não foi inicializado");
            }

            database = dbHelper.getWritableDatabase();

            if (database == null) {
                Log.e(TAG, "ERRO: Database retornado é null!");
                throw new RuntimeException("Não foi possível obter database");
            }

            if (!database.isOpen()) {
                Log.e(TAG, "ERRO: Database não está aberto!");
                throw new RuntimeException("Database não está aberto");
            }

            Log.d(TAG, "✅ Banco de dados aberto com sucesso");
            Log.d(TAG, "Database path: " + database.getPath());
            Log.d(TAG, "Database version: " + database.getVersion());

            verificarTabelas();
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO CRÍTICO ao abrir banco de dados: ", e);
            throw e;
        }
    }

    private void verificarTabelas() {
        try {
            Log.d(TAG, "=== VERIFICANDO TABELAS ===");

            Cursor cursor = database.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{DatabaseHelper.TABLE_CHAMADOS}
            );

            if (cursor.moveToFirst()) {
                Log.d(TAG, "✅ Tabela " + DatabaseHelper.TABLE_CHAMADOS + " existe");

                Cursor schemaCursor = database.rawQuery("PRAGMA table_info(" + DatabaseHelper.TABLE_CHAMADOS + ")", null);
                Log.d(TAG, "Estrutura da tabela " + DatabaseHelper.TABLE_CHAMADOS + ":");
                while (schemaCursor.moveToNext()) {
                    String columnName = schemaCursor.getString(1);
                    String columnType = schemaCursor.getString(2);
                    Log.d(TAG, " - " + columnName + " (" + columnType + ")");
                }
                schemaCursor.close();
            } else {
                Log.e(TAG, "❌ Tabela " + DatabaseHelper.TABLE_CHAMADOS + " NÃO existe!");
            }
            cursor.close();

            Cursor countCursor = database.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS, null);
            if (countCursor.moveToFirst()) {
                int count = countCursor.getInt(0);
                Log.d(TAG, "Total de chamados na tabela: " + count);
            }
            countCursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar tabelas: ", e);
        }
    }

    public void close() {
        Log.d(TAG, "=== FECHANDO BANCO DE DADOS ===");
        try {
            if (database != null && database.isOpen()) {
                database.close();
                Log.d(TAG, "✅ Banco de dados fechado com sucesso");
            } else {
                Log.d(TAG, "⚠️ Banco já estava fechado ou null");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao fechar banco: ", e);
        }
    }

    public long abrirChamado(Chamado chamado) {
        Log.d(TAG, "=== ABRINDO NOVO CHAMADO ===");
        try {
            if (database == null || !database.isOpen()) {
                Log.e(TAG, "❌ ERRO: Database não está aberto!");
                return -1;
            }

            if (chamado == null) {
                Log.e(TAG, "❌ ERRO: Chamado é null!");
                return -1;
            }

            Log.d(TAG, "Dados do chamado:");
            Log.d(TAG, " - Número: " + chamado.getNumero());
            Log.d(TAG, " - Título: " + chamado.getTitulo());
            Log.d(TAG, " - Descrição: " + chamado.getDescricao());
            Log.d(TAG, " - Status: " + chamado.getStatus());
            Log.d(TAG, " - Prioridade: " + chamado.getPrioridade());
            Log.d(TAG, " - Cliente ID: " + chamado.getClienteId());

            String timestamp = getCurrentTimestamp();
            Log.d(TAG, " - Timestamp: " + timestamp);

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_CHAMADO_NUMERO, chamado.getNumero());
            values.put(DatabaseHelper.COLUMN_CHAMADO_TITULO, chamado.getTitulo());
            values.put(DatabaseHelper.COLUMN_CHAMADO_DESCRICAO, chamado.getDescricao());
            values.put(DatabaseHelper.COLUMN_CHAMADO_STATUS, chamado.getStatus());
            values.put(DatabaseHelper.COLUMN_CHAMADO_PRIORIDADE, chamado.getPrioridade());
            values.put(DatabaseHelper.COLUMN_CHAMADO_CLIENTE_ID, chamado.getClienteId());
            values.put(DatabaseHelper.COLUMN_CHAMADO_CREATED_AT, timestamp);
            values.put(DatabaseHelper.COLUMN_CHAMADO_UPDATED_AT, timestamp);

            Log.d(TAG, "ContentValues criado com " + values.size() + " campos");
            Log.d(TAG, "Tentando inserir na tabela: " + DatabaseHelper.TABLE_CHAMADOS);

            long result = database.insert(DatabaseHelper.TABLE_CHAMADOS, null, values);

            if (result > 0) {
                Log.d(TAG, "✅ SUCESSO! Chamado inserido com ID: " + result);
            } else {
                Log.e(TAG, "❌ FALHA! Insert retornou: " + result);

                try {
                    Cursor checkCursor = database.rawQuery(
                            "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS +
                                    " WHERE " + DatabaseHelper.COLUMN_CHAMADO_NUMERO + "=?",
                            new String[]{chamado.getNumero()}
                    );
                    if (checkCursor.moveToFirst() && checkCursor.getInt(0) > 0) {
                        Log.e(TAG, "DIAGNÓSTICO: Número do chamado já existe!");
                    }
                    checkCursor.close();
                } catch (Exception diagEx) {
                    Log.e(TAG, "Erro no diagnóstico: ", diagEx);
                }
            }

            return result;
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO CRÍTICO ao abrir chamado: ", e);
            return -1;
        }
    }

    public List<Chamado> listarChamadosPorCliente(long clienteId) {
        Log.d(TAG, "=== LISTANDO CHAMADOS POR CLIENTE ===");
        Log.d(TAG, "Cliente ID: " + clienteId);

        List<Chamado> chamados = new ArrayList<>();

        try {
            if (database == null || !database.isOpen()) {
                Log.e(TAG, "❌ ERRO: Database não está aberto!");
                return chamados;
            }

            String query = "SELECT * FROM " + DatabaseHelper.TABLE_CHAMADOS +
                    " WHERE " + DatabaseHelper.COLUMN_CHAMADO_CLIENTE_ID + " = ? " +
                    " ORDER BY " + DatabaseHelper.COLUMN_CHAMADO_ID + " DESC";

            Log.d(TAG, "Query: " + query);
            Log.d(TAG, "Parâmetros: [" + clienteId + "]");

            Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(clienteId)});

            if (cursor == null) {
                Log.e(TAG, "❌ ERRO: Cursor é null!");
                return chamados;
            }

            Log.d(TAG, "Cursor obtido, total de registros: " + cursor.getCount());

            if (cursor.getCount() == 0) {
                Log.d(TAG, "⚠️ Nenhum chamado encontrado para o cliente " + clienteId);
                cursor.close();
                return chamados;
            }

            int contador = 0;
            while (cursor.moveToNext()) {
                contador++;
                Log.d(TAG, "Processando registro " + contador + "/" + cursor.getCount());

                try {
                    Chamado chamado = criarChamadoFromCursor(cursor);
                    if (chamado != null) {
                        chamados.add(chamado);
                        Log.d(TAG, "✅ Chamado adicionado: " + chamado.getNumero() + " - " + chamado.getTitulo());
                    } else {
                        Log.e(TAG, "❌ Chamado criado é null no registro " + contador);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Erro ao processar registro " + contador + ": ", e);
                }
            }

            cursor.close();
            Log.d(TAG, "✅ Total de chamados processados: " + chamados.size());

        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO CRÍTICO ao listar chamados: ", e);
        }

        return chamados;
    }

    public boolean atualizarStatus(long id, int novoStatus) {
        Log.d(TAG, "=== ATUALIZANDO STATUS DO CHAMADO ===");
        Log.d(TAG, "ID: " + id + ", Novo status: " + novoStatus);

        try {
            if (database == null || !database.isOpen()) {
                Log.e(TAG, "❌ ERRO: Database não está aberto!");
                return false;
            }

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_CHAMADO_STATUS, novoStatus);
            values.put(DatabaseHelper.COLUMN_CHAMADO_UPDATED_AT, getCurrentTimestamp());

            String whereClause = DatabaseHelper.COLUMN_CHAMADO_ID + " = ?";
            String[] whereArgs = {String.valueOf(id)};

            Log.d(TAG, "Executando update...");
            int rowsAffected = database.update(DatabaseHelper.TABLE_CHAMADOS, values, whereClause, whereArgs);

            if (rowsAffected > 0) {
                Log.d(TAG, "✅ Status atualizado com sucesso. Linhas afetadas: " + rowsAffected);
                return true;
            } else {
                Log.e(TAG, "❌ Nenhuma linha foi afetada na atualização");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO ao atualizar status: ", e);
            return false;
        }
    }

    public Chamado buscarChamadoPorId(long id) {
        Log.d(TAG, "=== BUSCANDO CHAMADO POR ID ===");
        Log.d(TAG, "ID: " + id);

        try {
            if (database == null || !database.isOpen()) {
                Log.e(TAG, "❌ ERRO: Database não está aberto!");
                return null;
            }

            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_CHAMADOS,
                    null,
                    DatabaseHelper.COLUMN_CHAMADO_ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null, null, null
            );

            Chamado chamado = null;
            if (cursor.moveToFirst()) {
                chamado = criarChamadoFromCursor(cursor);
                if (chamado != null) {
                    Log.d(TAG, "✅ Chamado encontrado: " + chamado.getNumero());
                }
            } else {
                Log.d(TAG, "❌ Chamado não encontrado");
            }
            cursor.close();
            return chamado;
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO ao buscar chamado por ID: ", e);
            return null;
        }
    }

    private Chamado criarChamadoFromCursor(Cursor cursor) {
        Log.d(TAG, "--- Criando chamado do cursor ---");
        try {
            if (cursor == null) {
                Log.e(TAG, "❌ Cursor é null!");
                return null;
            }

            Chamado chamado = new Chamado();

            // ID
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CHAMADO_ID);
            if (idIndex >= 0) {
                long id = cursor.getLong(idIndex);
                chamado.setId(id);
                Log.d(TAG, "ID: " + id);
            } else {
                Log.e(TAG, "❌ Coluna ID não encontrada!");
            }

            // Número
            int numeroIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CHAMADO_NUMERO);
            if (numeroIndex >= 0) {
                String numero = cursor.getString(numeroIndex);
                chamado.setNumero(numero);
                Log.d(TAG, "Número: " + numero);
            } else {
                Log.e(TAG, "❌ Coluna NUMERO não encontrada!");
            }

            // Título
            int tituloIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CHAMADO_TITULO);
            if (tituloIndex >= 0) {
                String titulo = cursor.getString(tituloIndex);
                chamado.setTitulo(titulo);
                Log.d(TAG, "Título: " + titulo);
            } else {
                Log.e(TAG, "❌ Coluna TITULO não encontrada!");
            }

            // Descrição
            int descricaoIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CHAMADO_DESCRICAO);
            if (descricaoIndex >= 0) {
                String descricao = cursor.getString(descricaoIndex);
                chamado.setDescricao(descricao);
                Log.d(TAG, "Descrição: " + descricao);
            } else {
                Log.e(TAG, "❌ Coluna DESCRICAO não encontrada!");
            }

            // Status
            int statusIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CHAMADO_STATUS);
            if (statusIndex >= 0) {
                int status = cursor.getInt(statusIndex);
                chamado.setStatus(status);
                Log.d(TAG, "Status: " + status);
            } else {
                Log.e(TAG, "❌ Coluna STATUS não encontrada!");
            }

            // Prioridade
            int prioridadeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CHAMADO_PRIORIDADE);
            if (prioridadeIndex >= 0) {
                int prioridade = cursor.getInt(prioridadeIndex);
                chamado.setPrioridade(prioridade);
                Log.d(TAG, "Prioridade: " + prioridade);
            } else {
                Log.e(TAG, "❌ Coluna PRIORIDADE não encontrada!");
            }

            // Cliente ID
            int clienteIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CHAMADO_CLIENTE_ID);
            if (clienteIdIndex >= 0) {
                long clienteId = cursor.getLong(clienteIdIndex);
                chamado.setClienteId(clienteId);
                Log.d(TAG, "Cliente ID: " + clienteId);
            } else {
                Log.e(TAG, "❌ Coluna CLIENTE_ID não encontrada!");
            }

            // Datas
            int createdAtIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CHAMADO_CREATED_AT);
            if (createdAtIndex >= 0) {
                String createdAt = cursor.getString(createdAtIndex);
                chamado.setCreatedAt(createdAt != null ? createdAt : "");
                Log.d(TAG, "Created At: " + createdAt);
            }

            int updatedAtIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CHAMADO_UPDATED_AT);
            if (updatedAtIndex >= 0) {
                String updatedAt = cursor.getString(updatedAtIndex);
                chamado.setUpdatedAt(updatedAt != null ? updatedAt : "");
                Log.d(TAG, "Updated At: " + updatedAt);
            }

            Log.d(TAG, "✅ Chamado criado com sucesso");
            return chamado;
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO CRÍTICO ao criar chamado do cursor: ", e);
            return null;
        }
    }

    private String getCurrentTimestamp() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String timestamp = sdf.format(new Date());
            Log.d(TAG, "Timestamp gerado: " + timestamp);
            return timestamp;
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao gerar timestamp: ", e);
            return "";
        }
    }
}
