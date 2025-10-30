package com.example.helpdeskapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.helpdeskapp.database.DatabaseHelper;
import com.example.helpdeskapp.models.Usuario;

import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {
    private static final String TAG = "UsuarioDAO";

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public UsuarioDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
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

    public Usuario verificarLogin(String email, String senha) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USUARIOS,
                null,
                DatabaseHelper.COLUMN_USER_EMAIL + " = ? AND " + DatabaseHelper.COLUMN_USER_SENHA + " = ?",
                new String[]{email, senha},
                null, null, null
        );

        Usuario usuario = null;
        if (cursor != null && cursor.moveToFirst()) {
            usuario = cursorToUsuario(cursor);
            cursor.close();
        }

        return usuario;
    }

    public Usuario buscarPorEmail(String email) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USUARIOS,
                null,
                DatabaseHelper.COLUMN_USER_EMAIL + " = ?",
                new String[]{email},
                null, null, null
        );

        Usuario usuario = null;
        if (cursor != null && cursor.moveToFirst()) {
            usuario = cursorToUsuario(cursor);
            cursor.close();
        }

        return usuario;
    }

    public int atualizarUsuario(Usuario usuario) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_NOME, usuario.getNome());
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, usuario.getEmail());
        values.put(DatabaseHelper.COLUMN_USER_TIPO, usuario.getTipo());

        if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) {
            values.put(DatabaseHelper.COLUMN_USER_SENHA, usuario.getSenha());
        }

        return database.update(
                DatabaseHelper.TABLE_USUARIOS,
                values,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(usuario.getId())}
        );
    }

    public int contarUsuarios() {
        Cursor cursor = database.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USUARIOS,
                null
        );

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }

        return count;
    }

    private Usuario cursorToUsuario(Cursor cursor) {
        Usuario usuario = new Usuario();
        usuario.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)));
        usuario.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)));
        usuario.setNome(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NOME)));
        usuario.setTipo(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_TIPO)));
        return usuario;
    }

    public List<Usuario> buscarTodosAdministradores() {
        List<Usuario> admins = new ArrayList<>();

        if (database == null || !database.isOpen()) {
            open();
        }

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USUARIOS,
                null,
                DatabaseHelper.COLUMN_USER_TIPO + " = ?",
                new String[]{"1"},
                null, null, null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                admins.add(cursorToUsuario(cursor));
            }
            cursor.close();
        }

        Log.d(TAG, "✅ Total de administradores: " + admins.size());
        return admins;
    }

}