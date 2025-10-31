package com.example.helpdeskapp.api;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    // ✅ URL do Render
    private static final String BASE_URL = "https://helpdeskapi-final.onrender.com/";

    private static Retrofit retrofit = null;
    private static Context appContext = null;

    // ✅ Inicializar contexto (chamar no Application ou na primeira tela)
    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            // ✅ Logging para debug
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // ✅ Configurar OkHttpClient com interceptor de autenticação
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor);

            // ✅ Adicionar interceptor de autenticação se o contexto foi inicializado
            if (appContext != null) {
                httpClient.addInterceptor(new AuthInterceptor(appContext));
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getRetrofit().create(ApiService.class);
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }
}