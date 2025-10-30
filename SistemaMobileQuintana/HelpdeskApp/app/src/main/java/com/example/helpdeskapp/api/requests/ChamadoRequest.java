package com.example.helpdeskapp.api.requests;

public class ChamadoRequest {
    private String titulo;
    private String descricao;
    private long usuarioId;
    private String categoria;
    private String prioridade;
    private String status;

    // Construtor vazio
    public ChamadoRequest() {
    }

    // Construtor completo
    public ChamadoRequest(String titulo, String descricao, long usuarioId, String categoria, String prioridade) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.usuarioId = usuarioId;
        this.categoria = categoria;
        this.prioridade = prioridade;
        this.status = "Aberto";
    }

    // Getters e Setters
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}