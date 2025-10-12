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
    private static final int DATABASE_VERSION = 6;

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

    // TABELA AVALIACOES
    private static final String TABLE_AVALIACOES = "avaliacoes";
    private static final String AVALIACAO_ID = "id";
    private static final String AVALIACAO_CHAMADO_ID = "chamado_id";
    private static final String AVALIACAO_NOTA = "nota";
    private static final String AVALIACAO_COMENTARIO = "comentario";
    private static final String AVALIACAO_DATA = "data_avaliacao";

    // TABELA DE ANEXOS
    private static final String TABLE_ANEXOS = "anexos";
    private static final String ANEXO_ID = "id";
    private static final String ANEXO_CHAMADO_ID = "chamado_id";
    private static final String ANEXO_NOME_ARQUIVO = "nome_arquivo";
    private static final String ANEXO_CAMINHO = "caminho";
    private static final String ANEXO_TIPO = "tipo";
    private static final String ANEXO_TAMANHO = "tamanho";
    private static final String ANEXO_DATA_UPLOAD = "data_upload";

    // TABELA DE TAGS
    private static final String TABLE_TAGS = "tags";
    private static final String TAG_ID = "id";
    private static final String TAG_NOME = "nome";
    private static final String TAG_COR = "cor";
    private static final String TAG_CREATED_AT = "created_at";

    // TABELA DE RELAÇÃO CHAMADO-TAG
    private static final String TABLE_CHAMADO_TAGS = "chamado_tags";
    private static final String CT_ID = "id";
    private static final String CT_CHAMADO_ID = "chamado_id";
    private static final String CT_TAG_ID = "tag_id";
    private static final String CT_CREATED_AT = "created_at";

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

    // SQL CRIAR TABELA ANEXOS
    private static final String CREATE_TABLE_ANEXOS =
            "CREATE TABLE " + TABLE_ANEXOS + " (" +
                    ANEXO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ANEXO_CHAMADO_ID + " INTEGER NOT NULL," +
                    ANEXO_NOME_ARQUIVO + " TEXT NOT NULL," +
                    ANEXO_CAMINHO + " TEXT NOT NULL," +
                    ANEXO_TIPO + " TEXT," +
                    ANEXO_TAMANHO + " INTEGER," +
                    ANEXO_DATA_UPLOAD + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(" + ANEXO_CHAMADO_ID + ") REFERENCES " +
                    TABLE_CHAMADOS + "(" + COLUMN_CHAMADO_ID + ")" +
                    ");";

    // SQL CRIAR TABELA TAGS
    private static final String CREATE_TABLE_TAGS =
            "CREATE TABLE " + TABLE_TAGS + " (" +
                    TAG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    TAG_NOME + " TEXT NOT NULL UNIQUE," +
                    TAG_COR + " TEXT NOT NULL," +
                    TAG_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");";

    // SQL CRIAR TABELA CHAMADO_TAGS (Relacionamento N:N)
    private static final String CREATE_TABLE_CHAMADO_TAGS =
            "CREATE TABLE " + TABLE_CHAMADO_TAGS + " (" +
                    CT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CT_CHAMADO_ID + " INTEGER NOT NULL," +
                    CT_TAG_ID + " INTEGER NOT NULL," +
                    CT_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(" + CT_CHAMADO_ID + ") REFERENCES " +
                    TABLE_CHAMADOS + "(" + COLUMN_CHAMADO_ID + ")," +
                    "FOREIGN KEY(" + CT_TAG_ID + ") REFERENCES " +
                    TABLE_TAGS + "(" + TAG_ID + ")," +
                    "UNIQUE(" + CT_CHAMADO_ID + "," + CT_TAG_ID + ")" +
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

            Log.d(TAG, "Criando tabela avaliacoes: " + CREATE_TABLE_AVALIACOES);
            db.execSQL(CREATE_TABLE_AVALIACOES);
            Log.d(TAG, "✅ Tabela avaliacoes criada com sucesso!");

            Log.d(TAG, "Criando tabela anexos: " + CREATE_TABLE_ANEXOS);
            db.execSQL(CREATE_TABLE_ANEXOS);
            Log.d(TAG, "✅ Tabela anexos criada com sucesso!");

            Log.d(TAG, "Criando tabela tags: " + CREATE_TABLE_TAGS);
            db.execSQL(CREATE_TABLE_TAGS);
            Log.d(TAG, "✅ Tabela tags criada com sucesso!");

            Log.d(TAG, "Criando tabela chamado_tags: " + CREATE_TABLE_CHAMADO_TAGS);
            db.execSQL(CREATE_TABLE_CHAMADO_TAGS);
            Log.d(TAG, "✅ Tabela chamado_tags criada com sucesso!");

            inserirTagsPadrao(db);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao criar tabelas: ", e);
        }
    }

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

        if (oldVersion < 5) {
            try {
                Log.d(TAG, "Criando tabela anexos: " + CREATE_TABLE_ANEXOS);
                db.execSQL(CREATE_TABLE_ANEXOS);
                Log.d(TAG, "✅ Tabela anexos adicionada com sucesso!");
            } catch (Exception e) {
                Log.e(TAG, "❌ Erro ao criar tabela anexos: ", e);
            }
        }

        if (oldVersion < 6) {
            try {
                Log.d(TAG, "Criando tabela tags: " + CREATE_TABLE_TAGS);
                db.execSQL(CREATE_TABLE_TAGS);
                Log.d(TAG, "✅ Tabela tags adicionada com sucesso!");

                Log.d(TAG, "Criando tabela chamado_tags: " + CREATE_TABLE_CHAMADO_TAGS);
                db.execSQL(CREATE_TABLE_CHAMADO_TAGS);
                Log.d(TAG, "✅ Tabela chamado_tags adicionada com sucesso!");

                inserirTagsPadrao(db);
            } catch (Exception e) {
                Log.e(TAG, "❌ Erro ao criar tabelas de tags: ", e);
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void debugInfo() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Log.d(TAG, "=== DEBUG DATABASE INFO ===");

            Cursor cursorUsers = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USUARIOS, null);
            if (cursorUsers.moveToFirst()) {
                Log.d(TAG, "Total usuários: " + cursorUsers.getInt(0));
            }
            cursorUsers.close();

            Cursor cursorChamados = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CHAMADOS, null);
            if (cursorChamados.moveToFirst()) {
                Log.d(TAG, "Total chamados: " + cursorChamados.getInt(0));
            }
            cursorChamados.close();

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

    private void inserirTagsPadrao(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Inserindo tags padrão...");

            String[] tagsPadrao = {
                    "INSERT INTO " + TABLE_TAGS + " (" + TAG_NOME + "," + TAG_COR + ") VALUES ('Urgente', '#F44336');",
                    "INSERT INTO " + TABLE_TAGS + " (" + TAG_NOME + "," + TAG_COR + ") VALUES ('Importante', '#FF9800');",
                    "INSERT INTO " + TABLE_TAGS + " (" + TAG_NOME + "," + TAG_COR + ") VALUES ('Bug', '#E91E63');",
                    "INSERT INTO " + TABLE_TAGS + " (" + TAG_NOME + "," + TAG_COR + ") VALUES ('Melhoria', '#2196F3');",
                    "INSERT INTO " + TABLE_TAGS + " (" + TAG_NOME + "," + TAG_COR + ") VALUES ('Dúvida', '#9C27B0');",
                    "INSERT INTO " + TABLE_TAGS + " (" + TAG_NOME + "," + TAG_COR + ") VALUES ('Documentação', '#00BCD4');",
                    "INSERT INTO " + TABLE_TAGS + " (" + TAG_NOME + "," + TAG_COR + ") VALUES ('Treinamento', '#4CAF50');",
                    "INSERT INTO " + TABLE_TAGS + " (" + TAG_NOME + "," + TAG_COR + ") VALUES ('Atualização', '#673AB7');"
            };

            for (String sql : tagsPadrao) {
                db.execSQL(sql);
            }

            Log.d(TAG, "✅ Tags padrão inseridas com sucesso!");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao inserir tags padrão: ", e);
        }
    }

    // GETTERS
    public static String getTableComentarios() { return TABLE_COMENTARIOS; }
    public static String getComentarioId() { return COMENTARIO_ID; }
    public static String getComentarioChamadoId() { return COMENTARIO_CHAMADO_ID; }
    public static String getComentarioUsuarioId() { return COMENTARIO_USUARIO_ID; }
    public static String getComentarioTexto() { return COMENTARIO_TEXTO; }
    public static String getComentarioDataCriacao() { return COMENTARIO_DATA_CRIACAO; }
    public static String getComentarioTipo() { return COMENTARIO_TIPO; }

    public static String getTableAvaliacoes() { return TABLE_AVALIACOES; }
    public static String getAvaliacaoId() { return AVALIACAO_ID; }
    public static String getAvaliacaoChamadoId() { return AVALIACAO_CHAMADO_ID; }
    public static String getAvaliacaoNota() { return AVALIACAO_NOTA; }
    public static String getAvaliacaoComentario() { return AVALIACAO_COMENTARIO; }
    public static String getAvaliacaoData() { return AVALIACAO_DATA; }

    public static String getTableAnexos() { return TABLE_ANEXOS; }
    public static String getAnexoId() { return ANEXO_ID; }
    public static String getAnexoChamadoId() { return ANEXO_CHAMADO_ID; }
    public static String getAnexoNomeArquivo() { return ANEXO_NOME_ARQUIVO; }
    public static String getAnexoCaminho() { return ANEXO_CAMINHO; }
    public static String getAnexoTipo() { return ANEXO_TIPO; }
    public static String getAnexoTamanho() { return ANEXO_TAMANHO; }
    public static String getAnexoDataUpload() { return ANEXO_DATA_UPLOAD; }

    public static String getTableTags() { return TABLE_TAGS; }
    public static String getTagId() { return TAG_ID; }
    public static String getTagNome() { return TAG_NOME; }
    public static String getTagCor() { return TAG_COR; }
    public static String getTagCreatedAt() { return TAG_CREATED_AT; }

    public static String getTableChamadoTags() { return TABLE_CHAMADO_TAGS; }
    public static String getCtId() { return CT_ID; }
    public static String getCtChamadoId() { return CT_CHAMADO_ID; }
    public static String getCtTagId() { return CT_TAG_ID; }
    public static String getCtCreatedAt() { return CT_CREATED_AT; }

    /**
     * Conta o número de chamados com um status específico.
     * @param status O valor do status a ser contado (ex: "Aberto", "Em Andamento").
     * @return O número de chamados encontrados.
     */
    public int countChamadosByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        Cursor cursor = null;

        try {
            String countQuery = "SELECT COUNT(*) FROM " + TABLE_CHAMADOS +
                    " WHERE " + COLUMN_CHAMADO_STATUS + " = ?";

            cursor = db.rawQuery(countQuery, new String[]{status});

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao contar chamados com status '" + status + "': ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // Nota: Não feche 'db' aqui se você for reutilizá-lo em chamadas sequenciais.
            // No Android, é comum que SQLiteOpenHelper gerencie o fechamento.
        }
        return count;
    }

    /**
     * Conta o número de chamados abertos.
     * @return O número de chamados com status 'Aberto'.
     */
    public int countChamadosAbertos() {
        // Note que o valor 'Aberto' deve corresponder exatamente ao que está no CREATE_TABLE_CHAMADOS
        return countChamadosByStatus("Aberto");
    }

    /**
     * Conta o número de chamados em andamento.
     * @return O número de chamados com status 'Em Andamento'.
     */
    public int countChamadosEmAndamento() {
        // Note que o valor 'Em Andamento' deve corresponder a algum status que você usa na sua lógica
        // Vamos usar "Em Andamento" como exemplo. Se o seu status for diferente (ex: "Em Progresso"), mude aqui.
        return countChamadosByStatus("Em Andamento");
    }
}