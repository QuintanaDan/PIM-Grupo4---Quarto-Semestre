package com.example.helpdeskapp.helpers;

import android.content.Context;
import android.util.Log;

import com.example.helpdeskapp.api.RetrofitClient;
import com.example.helpdeskapp.api.responses.ChamadoResponse;
import com.example.helpdeskapp.dao.ChamadoDAO;
import com.example.helpdeskapp.models.Chamado;
import com.example.helpdeskapp.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.helpdeskapp.api.ApiService;


public class SyncHelper {

    private static final String TAG = "SyncHelper";

    public interface SyncCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    /**
     * Sincronizar chamados da API para o banco local
     */
    public static void sincronizarChamados(Context context, SyncCallback callback) {
        SessionManager sessionManager = new SessionManager(context);
        String token = sessionManager.getAuthHeader();

        if (token == null) {
            Log.w(TAG, "⚠️ Sem token, pulando sincronização");
            if (callback != null) {
                callback.onError("Sem conexão com API (modo offline)");
            }
            return;
        }

        Log.d(TAG, "🔄 Iniciando sincronização de chamados...");

        RetrofitClient.getApiService().getChamados(token).enqueue(new Callback<List<ChamadoResponse>>() {
            @Override
            public void onResponse(Call<List<ChamadoResponse>> call, Response<List<ChamadoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChamadoResponse> chamadosAPI = response.body();

                    Log.d(TAG, "✅ Recebidos " + chamadosAPI.size() + " chamados da API");

                    salvarChamadosLocalmente(context, chamadosAPI);

                    if (callback != null) {
                        callback.onSuccess("Sincronizados " + chamadosAPI.size() + " chamados");
                    }
                } else {
                    Log.e(TAG, "❌ Erro na resposta: " + response.code());
                    if (callback != null) {
                        callback.onError("Erro: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ChamadoResponse>> call, Throwable t) {
                Log.e(TAG, "❌ Falha na sincronização: " + t.getMessage());
                if (callback != null) {
                    callback.onError("Falha na conexão: " + t.getMessage());
                }
            }
        });
    }

    private static void salvarChamadosLocalmente(Context context, List<ChamadoResponse> chamadosAPI) {
        ChamadoDAO dao = new ChamadoDAO(context);
        dao.open();

        for (ChamadoResponse chamadoAPI : chamadosAPI) {
            // Verificar se já existe localmente (por protocolo)
            Chamado existente = dao.buscarPorProtocolo(chamadoAPI.getProtocolo());

            if (existente == null) {
                // Inserir novo
                Chamado novoChamado = converterParaChamado(chamadoAPI);
                long id = dao.inserir(novoChamado);
                Log.d(TAG, "💾 Chamado inserido localmente: " + chamadoAPI.getProtocolo() + " (ID: " + id + ")");
            } else {
                // Atualizar existente
                Chamado atualizado = converterParaChamado(chamadoAPI);
                atualizado.setId(existente.getId());
                dao.atualizar(atualizado);
                Log.d(TAG, "🔄 Chamado atualizado localmente: " + chamadoAPI.getProtocolo());
            }
        }

        dao.close();
    }

    private static Chamado converterParaChamado(ChamadoResponse response) {
        Chamado chamado = new Chamado();

        chamado.setNumero(response.getProtocolo());
        chamado.setTitulo(response.getTitulo());
        chamado.setDescricao(response.getDescricao());
        chamado.setClienteId(response.getUsuarioId());
        chamado.setCategoria(response.getCategoria());
        chamado.setPrioridade(response.getPrioridade());
        chamado.setStatus(response.getStatus());

        // Converter data (simplificado)
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

            if (response.getDataAbertura() != null && !response.getDataAbertura().isEmpty()) {
                Date dataAbertura = sdf.parse(response.getDataAbertura());
                chamado.setDataCriacao(dataAbertura);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao converter data: " + e.getMessage());
            chamado.setDataCriacao(new Date());
        }

        return chamado;
    }

    ApiService service = RetrofitClient.getRetrofit().create(ApiService.class);
}