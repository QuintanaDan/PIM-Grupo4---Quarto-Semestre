package com.example.helpdeskapp.api.requests;

public class RegisterRequest {
    private String nome;
    private String email;
    private String senha;
    private String contato;

    public RegisterRequest(String nome, String email, String senha, String contato) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.contato = contato;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getContato() { return contato; }
    public void setContato(String contato) { this.contato = contato; }
}