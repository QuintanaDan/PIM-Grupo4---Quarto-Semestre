package com.example.helpdeskapp.database;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    // Database info
    private static final String DATABASE_NAME = "helpdesk.db";
    private static final int DATABASE_VERSION = 1;
    // ✅ CONSTANTES GERAIS
    public static final String COLUMN_ID = "_id";

    // ✅ TABELA DE USUARIOS
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_SENHA = "senha";
    public static final String COLUMN_USER_NOME = "nome";
    public static final String COLUMN_USER_TIPO = "tipo";

    // ✅ TABELA DE CHAMADOS
    public static final String TABLE_CHAMADOS = "chamados";
    // ✅ COLUNAS COM PREFIXO CHAMADO (para compatibilidade com ChamadoDAO)
    public static final String COLUMN_CHAMADO_ID = "_id";
    public static final String COLUMN_CHAMADO_NUMERO = "numero";
    public static final String COLUMN_CHAMADO_TITULO = "titulo";
    public static final String COLUMN_CHAMADO_DESCRICAO = "descricao";
    public static final String COLUMN_CHAMADO_STATUS = "status";
    public static final String COLUMN_CHAMADO_PRIORIDADE = "prioridade";
    public static final String COLUMN_CHAMADO_CLIENTE_ID = "cliente_id";
    public static final String COLUMN_CHAMADO_CREATED_AT = "created_at";
    public static final String COLUMN_CHAMADO_UPDATED_AT = "updated_at";
    // ✅ COLUNAS SEM PREFIXO (para compatibilidade com outros códigos)
    public static final String COLUMN_NUMERO = "numero";
    public static final String COLUMN_TITULO = "titulo";
    public static final String COLUMN_DESCRICAO = "descricao";
    public static final String COLUMN_CATEGORIA = "categoria";
    public static final String COLUMN_PRIORIDADE = "prioridade";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    // ✅ SQL PARA CRIAR TABELA USUARIOS
    private static final String CREATE_TABLE_USUARIOS =
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USER_EMAIL + " TEXT NOT NULL UNIQUE," +
                    COLUMN_USER_SENHA + " TEXT NOT NULL," +
                    COLUMN_USER_NOME + " TEXT NOT NULL," +
                    COLUMN_USER_TIPO + " INTEGER DEFAULT 0" +
                    ");";

    // ✅ SQL PARA CRIAR TABELA CHAMADOS
    private static final String CREATE_TABLE_CHAMADOS =
            "CREATE TABLE " + TABLE_CHAMADOS + " (" +
                    COLUMN_CHAMADO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_CHAMADO_NUMERO + " TEXT NOT NULL UNIQUE," +
                    COLUMN_CHAMADO_TITULO + " TEXT NOT NULL," +
                    COLUMN_CHAMADO_DESCRICAO + " TEXT," +
                    COLUMN_CATEGORIA + " INTEGER DEFAULT 1," +
                    COLUMN_CHAMADO_PRIORIDADE + " INTEGER DEFAULT 2," +
                    COLUMN_CHAMADO_STATUS + " INTEGER DEFAULT 1," +
                    COLUMN_CHAMADO_CLIENTE_ID + " INTEGER DEFAULT 1," +
                    COLUMN_CHAMADO_CREATED_AT + " TEXT," +
                    COLUMN_CHAMADO_UPDATED_AT + " TEXT" +
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
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao criar tabelas: ", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Atualizando banco de dados de " + oldVersion + " para " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAMADOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
    }
}
