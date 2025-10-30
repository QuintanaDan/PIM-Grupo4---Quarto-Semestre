package com.example.helpdeskapp.models;

public class Usuario {
    private long id;
    private String email;
    private String senha;
    private String nome;
    private int tipo; // 0 = admin, 1 = cliente

    // Construtor vazio (NECESSÁRIO)
    public Usuario() {}

    // Construtor com parâmetros
    public Usuario(String email, String senha, String nome, int tipo) {
        this.email = email;
        this.senha = senha;
        this.nome = nome;
        this.tipo = tipo;
    }

    // Getters e Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getTipo() { return tipo; }
    public void setTipo(int tipo) { this.tipo = tipo; }

    // Método para verificar se é admin
    public boolean isAdmin() {
        return tipo == 0;
    }

    public String getTipoTexto() {
        return isAdmin() ? "Administrador" : "Cliente";
    }
}
