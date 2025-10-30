package com.example.helpdeskapp.api.requests;

public class ChamadoUpdateRequest {
    private String titulo;
    private String descricao;
    private String categoria;
    private String prioridade;
    private String status;

    public ChamadoUpdateRequest() {
        // Construtor vazio
    }

    // Getters e Setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getPrioridade() { return prioridade; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}