package com.example.helpdeskapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.helpdeskapp.R;

public class ThemeManager {
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "selected_theme";

    // Constantes dos temas
    public static final int THEME_AMANHECER = 0;  // Light
    public static final int THEME_ANOITECER = 1;  // Dark
    public static final int THEME_ANCESTRAL = 2;  // Afrocêntrico

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public ThemeManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Obter tema atual
    public int getCurrentTheme() {
        return prefs.getInt(KEY_THEME, THEME_AMANHECER);
    }

    // Definir tema
    public void setTheme(int theme) {
        editor.putInt(KEY_THEME, theme);
        editor.apply();
    }

    // Aplicar tema
    public void applyTheme() {
        int theme = getCurrentTheme();
        context.setTheme(getThemeResId(theme));
    }

    // Obter Resource ID do tema
    private int getThemeResId(int theme) {
        switch (theme) {
            case THEME_ANOITECER:
                return R.style.Theme_HelpdeskApp_Anoitecer;
            case THEME_ANCESTRAL:
                return R.style.Theme_HelpdeskApp_Ancestral;
            case THEME_AMANHECER:
            default:
                return R.style.Theme_HelpdeskApp_Amanhecer;
        }
    }

    // Obter nome do tema
    public String getThemeName(int theme) {
        switch (theme) {
            case THEME_ANOITECER:
                return "Anoitecer";
            case THEME_ANCESTRAL:
                return "Ancestral";
            case THEME_AMANHECER:
            default:
                return "Amanhecer";
        }
    }

    // Obter descrição do tema
    public String getThemeDescription(int theme) {
        switch (theme) {
            case THEME_ANOITECER:
                return "Tema escuro moderno";
            case THEME_ANCESTRAL:
                return "Cores da terra e ancestralidade";
            case THEME_AMANHECER:
            default:
                return "Tema claro e suave";
        }
    }

    // Obter emoji do tema
    public String getThemeEmoji(int theme) {
        switch (theme) {
            case THEME_ANOITECER:
                return "🌙";
            case THEME_ANCESTRAL:
                return "👑";
            case THEME_AMANHECER:
            default:
                return "☀️";
        }
    }
}