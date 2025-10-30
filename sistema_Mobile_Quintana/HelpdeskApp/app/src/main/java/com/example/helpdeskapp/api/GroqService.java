package com.example.helpdeskapp.api;

import com.example.helpdeskapp.models.groq.GroqRequest;
import com.example.helpdeskapp.models.groq.GroqResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface GroqService {
    @Headers({
            "Content-Type: application/json"
    })
    
    @POST("chat/completions")
    Call<GroqResponse> createChatCompletion(@Body GroqRequest request);
}