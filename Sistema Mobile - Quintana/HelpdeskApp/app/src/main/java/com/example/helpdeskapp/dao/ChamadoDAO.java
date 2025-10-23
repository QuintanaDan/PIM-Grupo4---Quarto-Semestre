package com.example.helpdeskapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.helpdeskapp.database.DatabaseHelper;
import com.example.helpdeskapp.models.Chamado;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ChamadoDAO {
    private static final String TAG = "ChamadoDAO";

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private SimpleDateFormat dateFormat;

    public ChamadoDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    /**
     * ✅ INSERIR CHAMADO NORMAL (gera número único)
     * Usado ao criar chamado manualmente
     */
    public long inserir(Chamado chamado) throws SQLException {
        ContentValues values = new ContentValues();

        // ✅ GERAR NÚMERO ÚNICO
        String numeroUnico = gerarNumeroUnico();
        chamado.setNumero(numeroUnico);

        values.put(DatabaseHelper.COLUMN_CHAMADO_NUMERO, numeroUnico);
        values.put(DatabaseHelper.COLUMN_CHAMADO_TITULO, chamado.getTitulo());
        values.put(DatabaseHelper.COLUMN_CHAMADO_DESCRICAO, chamado.getDescricao());
        values.put(DatabaseHelper.COLUMN_CHAMADO_CLIENTE_ID, chamado.getClienteId());
        values.put(DatabaseHelper.COLUMN_CATEGORIA, chamado.getCategoria());
        values.put(DatabaseHelper.COLUMN_CHAMADO_PRIORIDADE, chamado.getPrioridade());
        values.put(DatabaseHelper.COLUMN_CHAMADO_STATUS, chamado.getStatus());
        values.put(DatabaseHelper.COLUMN_CHAMADO_CREATED_AT, dateFormat.format(new Date()));
        values.put(DatabaseHelper.COLUMN_CHAMADO_UPDATED_AT, dateFormat.format(new Date()));

        long id = database.insert(DatabaseHelper.TABLE_CHAMADOS, null, values);

        Log.d(TAG, "✅ Chamado inserido com ID: " + id + " e Número: " + numeroUnico);

        return id;
    }

    /**
     * ✅ INSERIR COM ID ESPECÍFICO (para sincronização com API)
     * Usado quando vem da API e precisa manter o ID
     */
    public long inserirComId(Chamado chamado) {
        ContentValues values = new ContentValues();

        // ✅ USAR O ID DA API
        if (chamado.getId() > 0) {
            values.put(DatabaseHelper.COLUMN_CHAMADO_ID, chamado.getId());
            Log.d(TAG, "📥 Inserindo com ID da API: " + chamado.getId());
        }

        // Número/protocolo
        if (chamado.getNumero() == null || chamado.getNumero().isEmpty()) {
            chamado.setNumero(gerarProtocolo());
        }
        values.put(DatabaseHelper.COLUMN_CHAMADO_NUMERO, chamado.getNumero());

        values.put(DatabaseHelper.COLUMN_CHAMADO_TITULO, chamado.getTitulo());
        values.put(DatabaseHelper.COLUMN_CHAMADO_DESCRICAO, chamado.getDescricao());
        values.put(DatabaseHelper.COLUMN_CHAMADO_CLIENTE_ID, chamado.getClienteId());
        values.put(DatabaseHelper.COLUMN_CATEGORIA, chamado.getCategoria());
        values.put(DatabaseHelper.COLUMN_CHAMADO_PRIORIDADE, chamado.getPrioridade());
        values.put(DatabaseHelper.COLUMN_CHAMADO_STATUS, chamado.getStatus());

        // Manter datas da API se existirem
        if (chamado.getCreatedAt() != null && !chamado.getCreatedAt().isEmpty()) {
            values.put(DatabaseHelper.COLUMN_CHAMADO_CREATED_AT, chamado.getCreatedAt());
        } else {
            values.put(DatabaseHelper.COLUMN_CHAMADO_CREATED_AT, dateFormat.format(new Date()));
        }

        if (chamado.getUpdatedAt() != null && !chamado.getUpdatedAt().isEmpty()) {
            values.put(DatabaseHelper.COLUMN_CHAMADO_UPDATED_AT, chamado.getUpdatedAt());
        } else {
            values.put(DatabaseHelper.COLUMN_CHAMADO_UPDATED_AT, dateFormat.format(new Date()));
        }

        // ✅ REPLACE: Se ID já existe, substitui (evita duplicação)
        long resultado = database.insertWithOnConflict(
                DatabaseHelper.TABLE_CHAMADOS,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );

        if (resultado > 0) {
            Log.d(TAG, "✅ Chamado inserido/atualizado com sucesso: ID=" + chamado.getId());
        } else {
            Log.e(TAG, "❌ Erro ao inserir chamado com ID: " + chamado.getId());
        }

        return resultado;
    }

    /**
     * ✅ GERAR NÚMERO ÚNICO
     */
    private String gerarNumeroUnico() {
        long timestamp = System.currentTimeMillis();
        int random = new Random().nextInt(1000);
        String numero = String.format("CH%d%03d", timestamp % 1000000, random);

        // Verificar se já existe
        Cursor cursor = database.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAMADOS +
                        " WHERE " + DatabaseHelper.COLUMN_CHAMADO_NUMERO + " = ?",
                new String[]{numero}
        );

        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count > 0) {
            return gerarNumeroUnico(); // Tentar novamente
        }

        return numero;
    }

    /**
     * ✅ GERAR PROTOCOLO
     */
    private String gerarProtocolo() {
        long timestamp = System.currentTimeMillis();
        int numeroProtocolo = (int)(timestamp % 1000000);
        return String.format("CH%06d", numeroProtocolo);
    }

    /**
     * ✅ ATUALIZAR CHAMADO
     */
    public int atualizar(Chamado chamado) {
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_CHAMADO_TITULO, chamado.getTitulo());
        values.put(DatabaseHelper.COLUMN_CHAMADO_DESCRICAO, chamado.getDescricao());
        values.put(DatabaseHelper.COLUMN_CATEGORIA, chamado.getCategoria());
        values.put(DatabaseHelper.COLUMN_CHAMADO_PRIORIDADE, chamado.getPrioridade());
        values.put(DatabaseHelper.COLUMN_CHAMADO_STATUS, chamado.getStatus());
        values.put(DatabaseHelper.COLUMN_CHAMADO_UPDATED_AT, dateFormat.format(new Date()));

        int rows = database.update(
                DatabaseHelper.TABLE_CHAMADOS,
                values,
                DatabaseHelper.COLUMN_CHAMADO_ID + " = ?",
                new String[]{String.valueOf(chamado.getId())}
        );

        if (rows > 0) {
            Log.d(TAG, "✅ Chamado atualizado: ID=" + chamado.getId());
        } else {
            Log.w(TAG, "⚠️ Nenhum chamado atualizado com ID: " + chamado.getId());
        }

        return rows;
    }

    /**
     * ✅ BUSCAR POR PROTOCOLO/NÚMERO
     */
    public Chamado buscarPorProtocolo(String numero) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_CHAMADOS,
                null,
                DatabaseHelper.COLUMN_CHAMADO_NUMERO + " = ?",
                new String[]{numero},
                null, null, null
        );

        Chamado chamado = null;
        if (cursor != null && cursor.moveToFirst()) {
            chamado = cursorToChamado(cursor);
            cursor.close();
        }

        return chamado;
    }

    /**
     * ✅ BUSCAR POR ID
     */
    public Chamado buscarPorId(long id) {
        Chamado chamado = null;

        try {
            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_CHAMADOS,
                    null,
                    DatabaseHelper.COLUMN_CHAMADO_ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                chamado = cursorToChamado(cursor);
                cursor.close();
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar chamado por ID: " + id, e);
        }

        return chamado;
    }

    /**
     * ✅ LISTAR CHAMADOS POR CLIENTE
     */
    public List<Chamado> listarChamadosPorCliente(long clienteId) {
        List<Chamado> chamados = new ArrayList<>();

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_CHAMADOS,
                null,
                DatabaseHelper.COLUMN_CHAMADO_CLIENTE_ID + " = ?",
                new String[]{String.valueOf(clienteId)},
                null, null,
                DatabaseHelper.COLUMN_CHAMADO_CREATED_AT + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                chamados.add(cursorToChamado(cursor));
            }
            cursor.close();
        }

        return chamados;
    }

    /**
     * ✅ BUSCAR TODOS OS CHAMADOS
     */
    public List<Chamado> buscarTodosChamados() {
        List<Chamado> chamados = new ArrayList<>();

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_CHAMADOS,
                null, null, null, null, null,
                DatabaseHelper.COLUMN_CHAMADO_CREATED_AT + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                chamados.add(cursorToChamado(cursor));
            }
            cursor.close();
        }

        return chamados;
    }

    /**
     * ✅ CONVERTER CURSOR PARA CHAMADO
     */
    private Chamado cursorToChamado(Cursor cursor) {
        Chamado chamado = new Chamado();

        chamado.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_ID)));

        int numeroIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CHAMADO_NUMERO);
        if (numeroIndex != -1) {
            chamado.setNumero(cursor.getString(numeroIndex));
        }

        chamado.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_TITULO)));
        chamado.setDescricao(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_DESCRICAO)));

        int categoriaIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORIA);
        if (categoriaIndex != -1) {
            chamado.setCategoria(cursor.getString(categoriaIndex));
        }

        chamado.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_STATUS)));
        chamado.setPrioridade(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_PRIORIDADE)));
        chamado.setClienteId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAMADO_CLIENTE_ID)));

        // Datas
        int createdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CHAMADO_CREATED_AT);
        if (createdIndex != -1) {
            String createdAtString = cursor.getString(createdIndex);
            chamado.setCreatedAt(createdAtString);

            try {
                chamado.setDataCriacao(dateFormat.parse(createdAtString));
            } catch (ParseException e) {
                Log.e(TAG, "Erro ao converter data: " + createdAtString);
            }
        }

        int updatedIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CHAMADO_UPDATED_AT);
        if (updatedIndex != -1) {
            chamado.setUpdatedAt(cursor.getString(updatedIndex));
        }

        return chamado;
    }

    public long abrirChamado(Chamado chamado) {
        return inserir(chamado);
    }

    public boolean atualizarStatus(long id, String novoStatus) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CHAMADO_STATUS, novoStatus);
        values.put(DatabaseHelper.COLUMN_CHAMADO_UPDATED_AT, dateFormat.format(new Date()));

        int rowsAffected = database.update(
                DatabaseHelper.TABLE_CHAMADOS,
                values,
                DatabaseHelper.COLUMN_CHAMADO_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        return rowsAffected > 0;
    }

    public void debugInfo() {
        if (dbHelper != null) {
            Log.d(TAG, "✅ DatabaseHelper inicializado");
        } else {
            Log.e(TAG, "❌ DatabaseHelper é null!");
        }
    }
}