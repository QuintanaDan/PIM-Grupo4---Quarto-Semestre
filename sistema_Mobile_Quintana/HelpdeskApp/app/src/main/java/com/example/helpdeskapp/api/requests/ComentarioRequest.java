package com.example.helpdeskapp.api.requests;

public class ComentarioRequest {
    private long chamadoId;
    private long usuarioId;
    private String texto;

    public ComentarioRequest(long chamadoId, long usuarioId, String texto) {
        this.chamadoId = chamadoId;
        this.usuarioId = usuarioId;
        this.texto = texto;
    }

    // Getters e Setters
    public long getChamadoId() { return chamadoId; }
    public void setChamadoId(long chamadoId) { this.chamadoId = chamadoId; }

    public long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(long usuarioId) { this.usuarioId = usuarioId; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
}