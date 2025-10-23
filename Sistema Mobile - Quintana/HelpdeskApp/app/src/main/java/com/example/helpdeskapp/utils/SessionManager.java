package com.example.helpdeskapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "HelpdeskPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_TOKEN = "token";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ========================================
    // MÉTODOS DE TOKEN JWT
    // ========================================

    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
        Log.d(TAG, "✅ Token JWT salvo");
    }

    public String getToken() {
        String token = prefs.getString(KEY_TOKEN, null);
        if (token != null) {
            Log.d(TAG, "✅ Token recuperado: " + token.substring(0, Math.min(20, token.length())) + "...");
        } else {
            Log.w(TAG, "⚠️ Token não encontrado no SharedPreferences");
        }
        return token;
    }

    public String getAuthHeader() {
        String token = getToken();
        if (token != null && !token.isEmpty()) {
            return "Bearer " + token;
        }
        Log.w(TAG, "⚠️ Token não disponível para header de autorização");
        return null;
    }

    public void clearToken() {
        editor.remove(KEY_TOKEN);
        editor.apply();
        Log.d(TAG, "✅ Token removido");
    }

    // ========================================
    // MÉTODOS DE SESSÃO DO USUÁRIO
    // ========================================

    public void saveUserData(long userId, String userName, String userEmail, int userType) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putInt(KEY_USER_TYPE, userType);
        editor.apply();

        Log.d(TAG, "✅ Dados do usuário salvos:");
        Log.d(TAG, "   ID: " + userId);
        Log.d(TAG, "   Nome: " + userName);
        Log.d(TAG, "   Email: " + userEmail);
        Log.d(TAG, "   Tipo: " + userType);
    }

    public void createLoginSession(long userId, String userEmail, String userName, int userType) {
        saveUserData(userId, userName, userEmail, userType);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, 0);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public int getUserType() {
        return prefs.getInt(KEY_USER_TYPE, 0);
    }

    public String getUserTypeText() {
        int tipo = getUserType();
        return tipo == 1 ? "Administrador" : "Cliente";
    }

    public boolean isAdmin() {
        return getUserType() == 1;
    }

    public String getUserInitials() {
        String nome = getUserName();
        if (nome == null || nome.isEmpty()) {
            return "?";
        }

        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) {
            return partes[0].substring(0, 1).toUpperCase();
        } else {
            return (partes[0].substring(0, 1) + partes[partes.length - 1].substring(0, 1)).toUpperCase();
        }
    }

    // ========================================
    // MÉTODOS INDIVIDUAIS (para compatibilidade)
    // ========================================

    public void saveUserId(long userId) {
        editor.putLong(KEY_USER_ID, userId);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void saveUserName(String userName) {
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    public void saveUserEmail(String email) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public void saveUserType(int userType) {
        editor.putInt(KEY_USER_TYPE, userType);
        editor.apply();
    }

    // ========================================
    // LOGOUT
    // ========================================

    public void logout() {
        editor.clear();
        editor.apply();
        Log.d(TAG, "✅ Logout realizado - todos os dados limpos (incluindo token)");
    }

    // ========================================
    // MÉTODOS DE PREFERÊNCIAS (TEMA, ETC)
    // ========================================

    public void savePreference(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public String getPreference(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public void savePreference(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getPreference(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    public void savePreference(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public int getPreference(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }
}