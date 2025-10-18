package com.example.helpdeskapp.api.responses;

public class LoginResponse {
    private long id;
    private String nome;
    private String email;
    private int tipo;
    private String token;

    // Getters e Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getTipo() { return tipo; }
    public void setTipo(int tipo) { this.tipo = tipo; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}