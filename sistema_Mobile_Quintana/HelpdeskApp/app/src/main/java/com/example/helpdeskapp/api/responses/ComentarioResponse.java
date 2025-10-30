package com.example.helpdeskapp.api.responses;

public class ComentarioResponse {
    private long id;
    private long chamadoId;
    private long usuarioId;
    private String nomeUsuario;
    private String texto;
    private String dataHora;

    // Getters e Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getChamadoId() { return chamadoId; }
    public void setChamadoId(long chamadoId) { this.chamadoId = chamadoId; }

    public long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(long usuarioId) { this.usuarioId = usuarioId; }

    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getDataHora() { return dataHora; }
    public void setDataHora(String dataHora) { this.dataHora = dataHora; }
}