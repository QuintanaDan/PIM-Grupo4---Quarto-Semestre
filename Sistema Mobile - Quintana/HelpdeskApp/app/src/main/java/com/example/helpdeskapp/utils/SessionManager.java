package com.example.helpdeskapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "HelpDeskSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_TOKEN = "token";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    // Criar sessão de login
    public void createLoginSession(long userId, String email, String name, int userType) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putInt(KEY_USER_TYPE, userType);
        editor.apply();
    }

    // Salvar token JWT
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    // Obter token
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    // Obter header de autenticação
    public String getAuthHeader() {
        String token = getToken();
        if (token != null) {
            return "Bearer " + token;
        }
        return null;
    }

    // Verificar se está logado
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Obter ID do usuário
    public long getUserId() {
        return sharedPreferences.getLong(KEY_USER_ID, -1);
    }

    // Obter nome do usuário
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    // Obter email do usuário
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    // Obter tipo do usuário (0 = Cliente, 1 = Admin)
    public int getUserType() {
        return sharedPreferences.getInt(KEY_USER_TYPE, 0);
    }

    // Verificar se é admin
    public boolean isAdmin() {
        return getUserType() == 1;
    }

    // Obter texto do tipo de usuário
    public String getUserTypeText() {
        return isAdmin() ? "Administrador" : "Cliente";
    }

    // Limpar sessão (logout)
    public void logout() {
        editor.clear();
        editor.apply();
    }

    // Limpar apenas o token
    public void clearToken() {
        editor.remove(KEY_TOKEN);
        editor.apply();
    }
}