package com.example.helpdeskapp.api;

import com.example.helpdeskapp.api.requests.ChamadoRequest;
import com.example.helpdeskapp.models.Chamado;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChamadoService {

    @GET("api/Chamados")
    Call<List<Chamado>> listarChamados(@Header("Authorization") String token);

    @POST("api/Chamados")
    Call<Chamado> criarChamado(
            @Header("Authorization") String token,
            @Body ChamadoRequest request
    );

    @GET("api/Chamados/{id}")
    Call<Chamado> buscarChamado(
            @Header("Authorization") String token,
            @Path("id") long id
    );
}