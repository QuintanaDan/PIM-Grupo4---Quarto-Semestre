package com.example.helpdeskapp.api;

import com.example.helpdeskapp.api.requests.LoginRequest;
import com.example.helpdeskapp.api.responses.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UsuarioService {
    @POST("api/Auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
}