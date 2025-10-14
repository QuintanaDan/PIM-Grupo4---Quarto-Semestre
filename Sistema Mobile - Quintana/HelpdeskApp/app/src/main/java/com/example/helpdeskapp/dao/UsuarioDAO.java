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
    private Context context;

    public UsuarioDAO(Context context) {
        this.context = context;
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

    // ========== BUSCAR TODOS OS ADMINISTRADORES ==========
    public List<Usuario> buscarTodosAdministradores() {
        List<Usuario> admins = new ArrayList<>();
        SQLiteDatabase db = null;

        try {
            db = dbHelper.getReadableDatabase();

            String query = "SELECT * FROM " + DatabaseHelper.TABLE_USUARIOS +
                    " WHERE " + DatabaseHelper.COLUMN_USER_TIPO + " = 1";

            Cursor cursor = db.rawQuery(query, null);

            while (cursor.moveToNext()) {
                Usuario usuario = new Usuario();
                usuario.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)));
                usuario.setNome(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NOME)));
                usuario.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)));
                usuario.setTipo(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_TIPO)));
                admins.add(usuario);
            }
            cursor.close();

            Log.d(TAG, "✅ Total de administradores: " + admins.size());

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao buscar administradores: ", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return admins;
    }
}