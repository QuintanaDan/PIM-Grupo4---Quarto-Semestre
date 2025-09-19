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
        return pref.getInt(KEY_USER_TYPE, 1); // 1 = cliente por padrão
    }

    // NOVO: Método para fazer logout
    public void logout() {
        editor.clear(); // Remove todos os dados salvos
        editor.commit();
    }

    // NOVO: Método para verificar se é admin
    public boolean isAdmin() {
        return getUserType() == 0; // 0 = admin
    }

    // NOVO: Método para obter texto do tipo de usuário
    public String getUserTypeText() {
        return isAdmin() ? "Administrador" : "Cliente";
    }

    // Método de Salvar a Sessão
    public void salvarSessao(Usuario usuario) {
        createLoginSession(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNome(),
                usuario.getTipo()
        );
    }

}
