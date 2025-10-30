package com.example.helpdeskapp.api;

import com.example.helpdeskapp.api.requests.*;
import com.example.helpdeskapp.api.responses.*;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ══════════════════════════════════════
    // AUTH
    // ══════════════════════════════════════

    @POST("api/Auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/Auth/register")
    Call<LoginResponse> register(@Body RegisterRequest request);

    // ══════════════════════════════════════
    // CHAMADOS
    // ══════════════════════════════════════

    @GET("api/Chamados")
    Call<List<ChamadoResponse>> getChamados(
            @Header("Authorization") String token
    );

    @GET("api/Chamados")
    Call<List<ChamadoResponse>> getChamadosByUsuario(
            @Header("Authorization") String token,
            @Query("usuarioId") long usuarioId
    );

    @GET("api/Chamados/{id}")
    Call<ChamadoResponse> getChamadoById(
            @Header("Authorization") String token,
            @Path("id") long id
    );

    @POST("api/Chamados")
    Call<ChamadoResponse> createChamado(
            @Header("Authorization") String token,
            @Body ChamadoRequest request
    );

    @PUT("api/Chamados/{id}")
    Call<Void> updateChamado(
            @Header("Authorization") String token,
            @Path("id") long id,
            @Body ChamadoUpdateRequest request
    );

    @DELETE("api/Chamados/{id}")
    Call<Void> deleteChamado(
            @Header("Authorization") String token,
            @Path("id") long id
    );

    // ══════════════════════════════════════
    // COMENTÁRIOS
    // ══════════════════════════════════════

    @GET("api/Comentarios/chamado/{chamadoId}")
    Call<List<ComentarioResponse>> getComentarios(
            @Header("Authorization") String token,
            @Path("chamadoId") long chamadoId
    );

    @POST("api/Comentarios")
    Call<ComentarioResponse> createComentario(
            @Header("Authorization") String token,
            @Body ComentarioRequest request
    );
}