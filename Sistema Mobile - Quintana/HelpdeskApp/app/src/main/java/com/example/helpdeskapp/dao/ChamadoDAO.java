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

    // Inserir chamado
    public long inserir(Chamado chamado) throws SQLException {
        ContentValues values = new ContentValues();

        // ✅ SEMPRE GERAR NÚMERO ÚNICO
        String numeroUnico = gerarNumeroUnico();
        chamado.setNumero(numeroUnico);

        values.put("numero", numeroUnico);
        values.put("titulo", chamado.getTitulo());
        values.put("descricao", chamado.getDescricao());
        values.put("cliente_id", chamado.getClienteId());
        values.put("categoria", chamado.getCategoria());
        values.put("prioridade", chamado.getPrioridade());
        values.put("status", chamado.getStatus());
        values.put("created_at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        values.put("updated_at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        return database.insert("chamados", null, values);
    }

    // ✅ GERAR NÚMERO VERDADEIRAMENTE ÚNICO
    private String gerarNumeroUnico() {
        // Usar timestamp + número aleatório para garantir unicidade
        long timestamp = System.currentTimeMillis();
        int random = new Random().nextInt(1000);
        String numero = String.format("CH%d%03d", timestamp % 1000000, random);

        // Verificar se já existe (improvável, mas por segurança)
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM chamados WHERE numero = ?", new String[]{numero});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        // Se existir (muito improvável), tentar novamente
        if (count > 0) {
            return gerarNumeroUnico(); // Recursão
        }

        return numero;
    }

    // ✅ ADICIONAR ESTE MÉTODO PARA GERAR PROTOCOLO
    private String gerarProtocolo() {
        // Formato: CH + timestamp de 6 dígitos
        long timestamp = System.currentTimeMillis();
        int numeroProtocolo = (int)(timestamp % 1000000);
        return String.format("CH%06d", numeroProtocolo);
    }

    // Atualizar chamado
    public int atualizar(Chamado chamado) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CHAMADO_TITULO, chamado.getTitulo());
        values.put(DatabaseHelper.COLUMN_CHAMADO_DESCRICAO, chamado.getDescricao());
        values.put(DatabaseHelper.COLUMN_CATEGORIA, chamado.getCategoria());
        values.put(DatabaseHelper.COLUMN_CHAMADO_PRIORIDADE, chamado.getPrioridade());
        values.put(DatabaseHelper.COLUMN_CHAMADO_STATUS, chamado.getStatus());
        values.put(DatabaseHelper.COLUMN_CHAMADO_UPDATED_AT, dateFormat.format(new Date()));

        return database.update(
                DatabaseHelper.TABLE_CHAMADOS,
                values,
                DatabaseHelper.COLUMN_CHAMADO_ID + " = ?",
                new String[]{String.valueOf(chamado.getId())}
        );
    }

    // Buscar por número/protocolo
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

    // Buscar por ID
    public Chamado buscarPorId(long id) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_CHAMADOS,
                null,
                DatabaseHelper.COLUMN_CHAMADO_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        Chamado chamado = null;
        if (cursor != null && cursor.moveToFirst()) {
            chamado = cursorToChamado(cursor);
            cursor.close();
        }

        return chamado;
    }

    // Listar chamados por cliente
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

    // Buscar todos os chamados
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

    // Converter cursor para Chamado
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