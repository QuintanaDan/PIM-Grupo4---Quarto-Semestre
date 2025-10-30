package com.example.helpdeskapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.helpdeskapp.database.DatabaseHelper;
import com.example.helpdeskapp.models.Tag;
import java.util.ArrayList;
import java.util.List;

public class TagDAO {
    private static final String TAG = "TagDAO";
    private DatabaseHelper databaseHelper;

    public TagDAO(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
    }

    // ========== CRIAR TAG ==========
    public long criarTag(Tag tag) {
        SQLiteDatabase db = null;
        long id = -1;

        try {
            db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.getTagNome(), tag.getNome());
            values.put(DatabaseHelper.getTagCor(), tag.getCor());

            id = db.insert(DatabaseHelper.getTableTags(), null, values);
            Log.d(TAG, "✅ Tag criada com ID: " + id);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao criar tag: ", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return id;
    }

    // ========== BUSCAR TODAS AS TAGS ==========
    public List<Tag> buscarTodasTags() {
        List<Tag> tags = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.query(
                    DatabaseHelper.getTableTags(),
                    null, null, null, null, null,
                    DatabaseHelper.getTagNome() + " ASC"
            );

            while (cursor.moveToNext()) {
                Tag tag = new Tag();
                tag.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.getTagId())));
                tag.setNome(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getTagNome())));
                tag.setCor(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getTagCor())));
                tags.add(tag);
            }

            Log.d(TAG, "✅ " + tags.size() + " tags encontradas");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao buscar tags: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return tags;
    }

    // ========== BUSCAR TAG POR ID ==========
    public Tag buscarTagPorId(long tagId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        Tag tag = null;

        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.query(
                    DatabaseHelper.getTableTags(),
                    null,
                    DatabaseHelper.getTagId() + " = ?",
                    new String[]{String.valueOf(tagId)},
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                tag = new Tag();
                tag.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.getTagId())));
                tag.setNome(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getTagNome())));
                tag.setCor(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getTagCor())));
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao buscar tag por ID: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return tag;
    }

    // ========== ADICIONAR TAG AO CHAMADO ==========
    public boolean adicionarTagAoChamado(long chamadoId, long tagId) {
        SQLiteDatabase db = null;
        boolean sucesso = false;

        try {
            db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.getCtChamadoId(), chamadoId);
            values.put(DatabaseHelper.getCtTagId(), tagId);

            long result = db.insert(DatabaseHelper.getTableChamadoTags(), null, values);
            sucesso = result != -1;

            Log.d(TAG, sucesso ? "✅ Tag adicionada ao chamado" : "❌ Falha ao adicionar tag");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao adicionar tag ao chamado: ", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return sucesso;
    }

    // ========== REMOVER TAG DO CHAMADO ==========
    public boolean removerTagDoChamado(long chamadoId, long tagId) {
        SQLiteDatabase db = null;
        boolean sucesso = false;

        try {
            db = databaseHelper.getWritableDatabase();
            int rows = db.delete(
                    DatabaseHelper.getTableChamadoTags(),
                    DatabaseHelper.getCtChamadoId() + " = ? AND " +
                            DatabaseHelper.getCtTagId() + " = ?",
                    new String[]{String.valueOf(chamadoId), String.valueOf(tagId)}
            );

            sucesso = rows > 0;
            Log.d(TAG, sucesso ? "✅ Tag removida do chamado" : "❌ Falha ao remover tag");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao remover tag do chamado: ", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return sucesso;
    }

    // ========== BUSCAR TAGS DE UM CHAMADO ==========
    public List<Tag> buscarTagsDoChamado(long chamadoId) {
        List<Tag> tags = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = databaseHelper.getReadableDatabase();

            String query = "SELECT t.* FROM " + DatabaseHelper.getTableTags() + " t " +
                    "INNER JOIN " + DatabaseHelper.getTableChamadoTags() + " ct " +
                    "ON t." + DatabaseHelper.getTagId() + " = ct." + DatabaseHelper.getCtTagId() +
                    " WHERE ct." + DatabaseHelper.getCtChamadoId() + " = ? " +
                    "ORDER BY t." + DatabaseHelper.getTagNome() + " ASC";

            cursor = db.rawQuery(query, new String[]{String.valueOf(chamadoId)});

            while (cursor.moveToNext()) {
                Tag tag = new Tag();
                tag.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.getTagId())));
                tag.setNome(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getTagNome())));
                tag.setCor(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.getTagCor())));
                tags.add(tag);
            }

            Log.d(TAG, "✅ " + tags.size() + " tags encontradas para o chamado " + chamadoId);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao buscar tags do chamado: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return tags;
    }

    // ========== ATUALIZAR TAG ==========
    public boolean atualizarTag(Tag tag) {
        SQLiteDatabase db = null;
        boolean sucesso = false;

        try {
            db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.getTagNome(), tag.getNome());
            values.put(DatabaseHelper.getTagCor(), tag.getCor());

            int rows = db.update(
                    DatabaseHelper.getTableTags(),
                    values,
                    DatabaseHelper.getTagId() + " = ?",
                    new String[]{String.valueOf(tag.getId())}
            );

            sucesso = rows > 0;
            Log.d(TAG, sucesso ? "✅ Tag atualizada" : "❌ Falha ao atualizar tag");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao atualizar tag: ", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return sucesso;
    }

    // ========== DELETAR TAG ==========
    public boolean deletarTag(long tagId) {
        SQLiteDatabase db = null;
        boolean sucesso = false;

        try {
            db = databaseHelper.getWritableDatabase();

            // Primeiro, remover todos os relacionamentos
            db.delete(
                    DatabaseHelper.getTableChamadoTags(),
                    DatabaseHelper.getCtTagId() + " = ?",
                    new String[]{String.valueOf(tagId)}
            );

            // Depois, deletar a tag
            int rows = db.delete(
                    DatabaseHelper.getTableTags(),
                    DatabaseHelper.getTagId() + " = ?",
                    new String[]{String.valueOf(tagId)}
            );

            sucesso = rows > 0;
            Log.d(TAG, sucesso ? "✅ Tag deletada" : "❌ Falha ao deletar tag");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao deletar tag: ", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return sucesso;
    }

    // ========== CONTAR CHAMADOS COM TAG ==========
    public int contarChamadosComTag(long tagId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int count = 0;

        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.getTableChamadoTags() +
                            " WHERE " + DatabaseHelper.getCtTagId() + " = ?",
                    new String[]{String.valueOf(tagId)}
            );

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao contar chamados com tag: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return count;
    }
}