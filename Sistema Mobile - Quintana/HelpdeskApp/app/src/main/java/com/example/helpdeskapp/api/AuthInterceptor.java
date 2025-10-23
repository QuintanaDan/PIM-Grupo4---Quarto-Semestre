package com.example.helpdeskapp.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private static final String TAG = "AuthInterceptor";
    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Buscar token do SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("HelpdeskPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        // Se não tem token, continua sem autorização
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "⚠️ Token não encontrado - requisição sem autorização");
            return chain.proceed(originalRequest);
        }

        // Adicionar token no header Authorization
        Request authorizedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        Log.d(TAG, "✅ Token adicionado no header: Bearer " + token.substring(0, Math.min(20, token.length())) + "...");

        return chain.proceed(authorizedRequest);
    }
}