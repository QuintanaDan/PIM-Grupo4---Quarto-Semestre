package com.example.helpdeskapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.helpdeskapp.database.DatabaseHelper;
import com.example.helpdeskapp.models.Usuario;

public class UsuarioDAO {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public UsuarioDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    public long inserirUsuario(Usuario usuario) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, usuario.getEmail());
        values.put(DatabaseHelper.COLUMN_USER_SENHA, usuario.getSenha());
        values.put(DatabaseHelper.COLUMN_USER_NOME, usuario.getNome());
        values.put(DatabaseHelper.COLUMN_USER_TIPO, usuario.getTipo());

        return database.insert(DatabaseHelper.TABLE_USUARIOS, null, values);
    }

    // NOVO: Método para verificar login
    public Usuario verificarLogin(String email, String senha) {
        String[] columns = {
                DatabaseHelper.COLUMN_USER_ID,
                DatabaseHelper.COLUMN_USER_EMAIL,
                DatabaseHelper.COLUMN_USER_NOME,
                DatabaseHelper.COLUMN_USER_TIPO
        };

        String selection = DatabaseHelper.COLUMN_USER_EMAIL + "=? AND " +
                DatabaseHelper.COLUMN_USER_SENHA + "=?";
        String[] selectionArgs = {email, senha};

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USUARIOS,
                columns,
                selection,
                selectionArgs,
                null, null, null
        );

        Usuario usuario = null;
        if (cursor != null && cursor.moveToFirst()) {
            usuario = new Usuario();
            usuario.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)));
            usuario.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)));
            usuario.setNome(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NOME)));
            usuario.setTipo(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_TIPO)));
        }

        if (cursor != null) {
            cursor.close();
        }

        return usuario;
    }

    // NOVO: Método para contar usuários
    public int contarUsuarios() {
        String countQuery = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USUARIOS;
        Cursor cursor = database.rawQuery(countQuery, null);

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }

        return count;
    }

    // NOVO: Método para buscar usuário por ID
    public Usuario buscarPorId(long id) {
        String[] columns = {
                DatabaseHelper.COLUMN_USER_ID,
                DatabaseHelper.COLUMN_USER_EMAIL,
                DatabaseHelper.COLUMN_USER_NOME,
                DatabaseHelper.COLUMN_USER_TIPO
        };

        String selection = DatabaseHelper.COLUMN_USER_ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USUARIOS,
                columns,
                selection,
                selectionArgs,
                null, null, null
        );

        Usuario usuario = null;
        if (cursor != null && cursor.moveToFirst()) {
            usuario = new Usuario();
            usuario.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)));
            usuario.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)));
            usuario.setNome(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NOME)));
            usuario.setTipo(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_TIPO)));
        }

        if (cursor != null) {
            cursor.close();
        }

        return usuario;
    }
}
