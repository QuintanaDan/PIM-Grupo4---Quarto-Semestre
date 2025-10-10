package com.example.helpdeskapp.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    // Database info
    private static final String DATABASE_NAME = "helpdesk.db";
    private static final int DATABASE_VERSION = 4;

    // CONSTANTES GERAIS
    public static final String COLUMN_ID = "_id";

    // TABELA DE USUARIOS
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_SENHA = "senha";
    public static final String COLUMN_USER_NOME = "nome";
    public static final String COLUMN_USER_TIPO = "tipo";

    // TABELA DE CHAMADOS
    public static final String TABLE_CHAMADOS = "chamados";
    public static final String COLUMN_CHAMADO_ID = "_id";
    public static final String COLUMN_CHAMADO_NUMERO = "numero";
    public static final String COLUMN_CHAMADO_TITULO = "titulo";
    public static final String COLUMN_CHAMADO_DESCRICAO = "descricao";
    public static final String COLUMN_CHAMADO_STATUS = "status";
    public static final String COLUMN_CHAMADO_PRIORIDADE = "prioridade";
    public static final String COLUMN_CHAMADO_CLIENTE_ID = "cliente_id";
    public static final String COLUMN_CHAMADO_CREATED_AT = "created_at";
    public static final String COLUMN_CHAMADO_UPDATED_AT = "updated_at";

    // Colunas sem prefixo (compatibilidade)
    public static final String COLUMN_NUMERO = "numero";
    public static final String COLUMN_TITULO = "titulo";
    public static final String COLUMN_DESCRICAO = "descricao";
    public static final String COLUMN_CATEGORIA = "categoria";
    public static final String COLUMN_PRIORIDADE = "prioridade";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    // TABELA COMENTARIOS
    private static final String TABLE_COMENTARIOS = "comentarios";
    private static final String COMENTARIO_ID = "id";
    private static final String COMENTARIO_CHAMADO_ID = "chamado_id";
    private static final String COMENTARIO_USUARIO_ID = "usuario_id";
    private static final String COMENTARIO_TEXTO = "texto";
    private static final String COMENTARIO_DATA_CRIACAO = "data_criacao";
    private static final String COMENTARIO_TIPO = "tipo";

    public static final String CHAMADO_ID = COLUMN_CHAMADO_ID;
    public static final String USUARIO_ID = COLUMN_USER_ID;

    // SQL CRIAR TABELA USUARIOS
    private static final String CREATE_TABLE_USUARIOS =
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USER_EMAIL + " TEXT NOT NULL UNIQUE," +
                    COLUMN_USER_SENHA + " TEXT NOT NULL," +
                    COLUMN_USER_NOME + " TEXT NOT NULL," +
                    COLUMN_USER_TIPO + " INTEGER DEFAULT 0" +
                    ");";

    // SQL CRIAR TABELA CHAMADOS
    private static final String CREATE_TABLE_CHAMADOS =
            "CREATE TABLE " + TABLE_CHAMADOS + " (" +
                    COLUMN_CHAMADO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_CHAMADO_NUMERO + " TEXT NOT NULL UNIQUE," +
                    COLUMN_CHAMADO_TITULO + " TEXT NOT NULL," +
                    COLUMN_CHAMADO_DESCRICAO + " TEXT," +
                    COLUMN_CATEGORIA + " TEXT DEFAULT 'Geral'," +
                    COLUMN_CHAMADO_PRIORIDADE + " TEXT DEFAULT 'Média'," +
                    COLUMN_CHAMADO_STATUS + " TEXT DEFAULT 'Aberto'," +
                    COLUMN_CHAMADO_CLIENTE_ID + " INTEGER DEFAULT 1," +
                    COLUMN_CHAMADO_CREATED_AT + " TEXT," +
                    COLUMN_CHAMADO_UPDATED_AT + " TEXT" +
                    ");";

    // SQL CRIAR TABELA COMENTARIOS
    private static final String CREATE_TABLE_COMENTARIOS =
            "CREATE TABLE " + TABLE_COMENTARIOS + "("
                    + COMENTARIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COMENTARIO_CHAMADO_ID + " INTEGER NOT NULL,"
                    + COMENTARIO_USUARIO_ID + " INTEGER NOT NULL,"
                    + COMENTARIO_TEXTO + " TEXT NOT NULL,"
                    + COMENTARIO_DATA_CRIACAO + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + COMENTARIO_TIPO + " TEXT DEFAULT 'user',"
                    + "FOREIGN KEY(" + COMENTARIO_CHAMADO_ID + ") REFERENCES " + TABLE_CHAMADOS + "(" + COLUMN_CHAMADO_ID + "),"
                    + "FOREIGN KEY(" + COMENTARIO_USUARIO_ID + ") REFERENCES " + TABLE_USUARIOS + "(" + COLUMN_USER_ID + ")"
                    + ")";
    // TABELA AVALIACOES
    private static final String TABLE_AVALIACOES = "avaliacoes";
    private static final String AVALIACAO_ID = "id";
    private static final String AVALIACAO_CHAMADO_ID = "chamado_id";
    private static final String AVALIACAO_NOTA = "nota";
    private static final String AVALIACAO_COMENTARIO = "comentario";
    private static final String AVALIACAO_DATA = "data_avaliacao";

    // SQL CRIAR TABELA AVALIACOES
    private static final String CREATE_TABLE_AVALIACOES =
            "CREATE TABLE " + TABLE_AVALIACOES + " (" +
                    AVALIACAO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    AVALIACAO_CHAMADO_ID + " INTEGER NOT NULL," +
                    AVALIACAO_NOTA + " INTEGER NOT NULL," +
                    AVALIACAO_COMENTARIO + " TEXT," +
                    AVALIACAO_DATA + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(" + AVALIACAO_CHAMADO_ID + ") REFERENCES " +
                    TABLE_CHAMADOS + "(" + COLUMN_CHAMADO_ID + ")" +
                    ");";



    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Criando tabela usuarios: " + CREATE_TABLE_USUARIOS);
            db.execSQL(CREATE_TABLE_USUARIOS);
            Log.d(TAG, "✅ Tabela usuarios criada com sucesso!");

            Log.d(TAG, "Criando tabela chamados: " + CREATE_TABLE_CHAMADOS);
            db.execSQL(CREATE_TABLE_CHAMADOS);
            Log.d(TAG, "✅ Tabela chamados criada com sucesso!");

            Log.d(TAG, "Criando tabela comentarios: " + CREATE_TABLE_COMENTARIOS);
            db.execSQL(CREATE_TABLE_COMENTARIOS);
            Log.d(TAG, "✅ Tabela comentarios criada com sucesso!");

            // NOVO: Tabela de avaliações
            Log.d(TAG, "Criando tabela avaliacoes: " + CREATE_TABLE_AVALIACOES);
            db.execSQL(CREATE_TABLE_AVALIACOES);
            Log.d(TAG, "✅ Tabela avaliacoes criada com sucesso!");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao criar tabelas: ", e);
        }
    }

    // Atualizar onUpgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Atualizando banco de dados de " + oldVersion + " para " + newVersion);

        if (oldVersion < 3) {
            try {
                Log.d(TAG, "Criando tabela comentarios: " + CREATE_TABLE_COMENTARIOS);
                db.execSQL(CREATE_TABLE_COMENTARIOS);
                Log.d(TAG, "✅ Tabela comentarios adicionada com sucesso!");
            } catch (Exception e) {
                Log.e(TAG, "❌ Erro ao criar tabela comentarios: ", e);
            }
        }

        if (oldVersion < 4) {
            try {
                Log.d(TAG, "Criando tabela avaliacoes: " + CREATE_TABLE_AVALIACOES);
                db.execSQL(CREATE_TABLE_AVALIACOES);
                Log.d(TAG, "✅ Tabela avaliacoes adicionada com sucesso!");
            } catch (Exception e) {
                Log.e(TAG, "❌ Erro ao criar tabela avaliacoes: ", e);
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    // MÉTODO debugInfo (ADICIONADO)
    public void debugInfo() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Log.d(TAG, "=== DEBUG DATABASE INFO ===");

            // Contar usuários
            Cursor cursorUsers = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USUARIOS, null);
            if (cursorUsers.moveToFirst()) {
                Log.d(TAG, "Total usuários: " + cursorUsers.getInt(0));
            }
            cursorUsers.close();

            // Contar chamados
            Cursor cursorChamados = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CHAMADOS, null);
            if (cursorChamados.moveToFirst()) {
                Log.d(TAG, "Total chamados: " + cursorChamados.getInt(0));
            }
            cursorChamados.close();

            // Listar todos os chamados
            Cursor cursorAll = db.rawQuery("SELECT * FROM " + TABLE_CHAMADOS, null);
            Log.d(TAG, "Listando todos os chamados:");
            while (cursorAll.moveToNext()) {
                long id = cursorAll.getLong(cursorAll.getColumnIndexOrThrow(COLUMN_CHAMADO_ID));
                String numero = cursorAll.getString(cursorAll.getColumnIndexOrThrow(COLUMN_CHAMADO_NUMERO));
                String titulo = cursorAll.getString(cursorAll.getColumnIndexOrThrow(COLUMN_CHAMADO_TITULO));
                long clienteId = cursorAll.getLong(cursorAll.getColumnIndexOrThrow(COLUMN_CHAMADO_CLIENTE_ID));
                Log.d(TAG, "  - ID: " + id + ", Número: " + numero + ", Título: " + titulo + ", Cliente: " + clienteId);
            }
            cursorAll.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao fazer debug: ", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // GETTERS PARA COMENTÁRIOS (CORRIGIDOS)
    public static String getTableComentarios() {
        return TABLE_COMENTARIOS;
    }

    public static String getComentarioId() {
        return COMENTARIO_ID;
    }

    public static String getComentarioChamadoId() {
        return COMENTARIO_CHAMADO_ID;
    }

    public static String getComentarioUsuarioId() {
        return COMENTARIO_USUARIO_ID;
    }

    public static String getComentarioTexto() {
        return COMENTARIO_TEXTO;
    }

    public static String getComentarioDataCriacao() {
        return COMENTARIO_DATA_CRIACAO;
    }

    public static String getComentarioTipo() {
        return COMENTARIO_TIPO;
    }

    public static String getTableAvaliacoes() { return TABLE_AVALIACOES; }
    public static String getAvaliacaoId() { return AVALIACAO_ID; }
    public static String getAvaliacaoChamadoId() { return AVALIACAO_CHAMADO_ID; }
    public static String getAvaliacaoNota() { return AVALIACAO_NOTA; }
    public static String getAvaliacaoComentario() { return AVALIACAO_COMENTARIO; }
    public static String getAvaliacaoData() { return AVALIACAO_DATA; }
}