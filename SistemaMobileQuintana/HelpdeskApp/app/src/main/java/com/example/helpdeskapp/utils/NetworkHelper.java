package com.example.helpdeskapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.concurrent.TimeUnit;

public class NetworkHelper {
    private static final String TAG = "NetworkHelper";

    /**
     * Verifica se tem conexão com internet
     */
    public static boolean temInternet(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean conectado = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                Log.d(TAG, "📡 Internet: " + (conectado ? "SIM" : "NÃO"));
                return conectado;
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar internet", e);
        }

        return false;
    }

    /**
     * Testa se a API está acessível
     */
    public static boolean testarConexaoAPI(String apiUrl) {
        Log.d(TAG, "🔍 Testando API: " + apiUrl);

        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .build();

            // ✅ TENTAR ACESSAR O SWAGGER (que sabemos que existe)
            String urlTeste = apiUrl;

            // Se terminar com /, remover para não ficar //
            if (urlTeste.endsWith("/")) {
                urlTeste = urlTeste.substring(0, urlTeste.length() - 1);
            }

            // Testar Swagger
            urlTeste = urlTeste + "/swagger/index.html";

            Log.d(TAG, "📤 Requisição para: " + urlTeste);

            Request request = new Request.Builder()
                    .url(urlTeste)
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            int codigo = response.code();
            response.close();

            Log.d(TAG, "📥 Resposta: " + codigo);

            // 200 ou 404 significam que a API está acessível
            // (404 pode aparecer se o Swagger estiver em outra rota)
            boolean sucesso = (codigo >= 200 && codigo < 500);

            Log.d(TAG, sucesso ? "✅ API ONLINE" : "❌ API OFFLINE");

            return sucesso;

        } catch (java.net.UnknownHostException e) {
            Log.e(TAG, "❌ Host não encontrado: " + e.getMessage());
            return false;
        } catch (java.net.SocketTimeoutException e) {
            Log.e(TAG, "❌ Timeout (5s)");
            return false;
        } catch (java.net.ConnectException e) {
            Log.e(TAG, "❌ Não conectou: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica disponibilidade completa (internet + API)
     */
    public static boolean apiDisponivel(Context context, String apiUrl) {
        Log.d(TAG, "=== VERIFICANDO API ===");
        Log.d(TAG, "URL: " + apiUrl);

        // Primeiro verifica internet
        if (!temInternet(context)) {
            Log.d(TAG, "❌ Sem internet");
            return false;
        }

        // Depois testa a API
        boolean apiOk = testarConexaoAPI(apiUrl);

        Log.d(TAG, "=== RESULTADO: " + (apiOk ? "ONLINE ✅" : "OFFLINE ❌") + " ===");

        return apiOk;
    }
}