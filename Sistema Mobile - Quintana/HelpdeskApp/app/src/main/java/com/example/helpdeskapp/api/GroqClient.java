package com.example.helpdeskapp.api;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import com.example.helpdeskapp.BuildConfig;
import okhttp3.MediaType;

public class GroqClient {
    private static final String TAG = "GroqClient";

    // ✅ USAR BuildConfig AO INVÉS DE HARDCODE
    private static final String API_KEY = BuildConfig.GROQ_API_KEY;

    private static final String BASE_URL = "https://api.groq.com/openai/v1/";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofit() {
        android.util.Log.d(TAG, "🔑 === INICIALIZANDO GROQ CLIENT ===");
        android.util.Log.d(TAG, "   API_KEY existe: " + (API_KEY != null && !API_KEY.isEmpty()));
        android.util.Log.d(TAG, "   API_KEY tamanho: " + (API_KEY != null ? API_KEY.length() : 0));
        android.util.Log.d(TAG, "   API_KEY começa com 'gsk_': " + (API_KEY != null && API_KEY.startsWith("gsk_")));
        android.util.Log.d(TAG, "   BASE_URL: " + BASE_URL);
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            Interceptor authInterceptor = new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request originalRequest = chain.request();
                    Request.Builder builder = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + API_KEY);
                    Request newRequest = builder.build();
                    return chain.proceed(newRequest);
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}