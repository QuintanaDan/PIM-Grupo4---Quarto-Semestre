package com.example.helpdeskapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.helpdeskapp.models.Usuario;

public class SessionManager {
    private static final String PREF_NAME = "HelpdeskSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_TYPE = "userType";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(long userId, String email, String name, int userType) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putInt(KEY_USER_TYPE, userType);
        editor.commit();

        // DEBUG
        android.util.Log.d("SessionManager", "✅ Sessão criada:");
        android.util.Log.d("SessionManager", "   UserID: " + userId);
        android.util.Log.d("SessionManager", "   Nome: " + name);
        android.util.Log.d("SessionManager", "   Tipo: " + userType + " (" + (userType == 1 ? "ADMIN" : "CLIENTE") + ")");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public long getUserId() {
        return pref.getLong(KEY_USER_ID, -1);
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    public int getUserType() {
        int tipo = pref.getInt(KEY_USER_TYPE, 0); // ✅ 0 = Cliente por padrão
        android.util.Log.d("SessionManager", "🔍 getUserType() retornou: " + tipo);
        return tipo;
    }

    public void logout() {
        editor.clear();
        editor.commit();
        android.util.Log.d("SessionManager", "✅ Logout realizado");
    }

    // ✅ CORRIGIDO: Admin = 1
    public boolean isAdmin() {
        int tipo = getUserType();
        boolean admin = (tipo == 1);
        android.util.Log.d("SessionManager", "🔍 isAdmin() - Tipo: " + tipo + ", isAdmin: " + admin);
        return admin;
    }

    // ✅ CORRIGIDO: Cliente = 0
    public boolean isCliente() {
        return getUserType() == 0;
    }

    public String getUserTypeText() {
        return isAdmin() ? "Administrador" : "Cliente";
    }

    public void salvarSessao(Usuario usuario) {
        createLoginSession(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNome(),
                usuario.getTipo()
        );
    }
}