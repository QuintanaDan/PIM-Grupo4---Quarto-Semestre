package com.example.helpdeskapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenciasNotificacaoManager {
    private static final String PREFS_NAME = "notificacao_prefs";

    private static final String KEY_NOTIF_CHAMADOS = "notif_chamados";
    private static final String KEY_NOTIF_COMENTARIOS = "notif_comentarios";
    private static final String KEY_NOTIF_STATUS = "notif_status";
    private static final String KEY_NOTIF_LEMBRETES = "notif_lembretes";
    private static final String KEY_NOTIF_RESUMO = "notif_resumo";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public PreferenciasNotificacaoManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ========== NOTIFICAÇÕES DE CHAMADOS ==========
    public boolean isNotificacaoChamadosAtiva() {
        return prefs.getBoolean(KEY_NOTIF_CHAMADOS, true);
    }

    public void setNotificacaoChamados(boolean ativa) {
        editor.putBoolean(KEY_NOTIF_CHAMADOS, ativa);
        editor.apply();
    }

    // ========== NOTIFICAÇÕES DE COMENTÁRIOS ==========
    public boolean isNotificacaoComentariosAtiva() {
        return prefs.getBoolean(KEY_NOTIF_COMENTARIOS, true);
    }

    public void setNotificacaoComentarios(boolean ativa) {
        editor.putBoolean(KEY_NOTIF_COMENTARIOS, ativa);
        editor.apply();
    }

    // ========== NOTIFICAÇÕES DE STATUS ==========
    public boolean isNotificacaoStatusAtiva() {
        return prefs.getBoolean(KEY_NOTIF_STATUS, true);
    }

    public void setNotificacaoStatus(boolean ativa) {
        editor.putBoolean(KEY_NOTIF_STATUS, ativa);
        editor.apply();
    }

    // ========== NOTIFICAÇÕES DE LEMBRETES ==========
    public boolean isNotificacaoLembretesAtiva() {
        return prefs.getBoolean(KEY_NOTIF_LEMBRETES, true);
    }

    public void setNotificacaoLembretes(boolean ativa) {
        editor.putBoolean(KEY_NOTIF_LEMBRETES, ativa);
        editor.apply();
    }

    // ========== NOTIFICAÇÕES DE RESUMO ==========
    public boolean isNotificacaoResumoAtiva() {
        return prefs.getBoolean(KEY_NOTIF_RESUMO, true);
    }

    public void setNotificacaoResumo(boolean ativa) {
        editor.putBoolean(KEY_NOTIF_RESUMO, ativa);
        editor.apply();
    }
}