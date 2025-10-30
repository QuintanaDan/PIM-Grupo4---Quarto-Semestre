package com.example.helpdeskapp.models;

import java.io.Serializable;

public class Notificacao implements Serializable {
    private long id;
    private long usuarioId;
    private String tipo;          // CHAMADO_CRIADO, COMENTARIO, STATUS_ALTERADO, LEMBRETE
    private String titulo;
    private String mensagem;
    private long chamadoId;       // Referência ao chamado (se aplicável)
    private boolean lida;
    private String createdAt;

    public Notificacao() {
        this.lida = false;
    }

    // ========== GETTERS E SETTERS ==========

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public long getChamadoId() {
        return chamadoId;
    }

    public void setChamadoId(long chamadoId) {
        this.chamadoId = chamadoId;
    }

    public boolean isLida() {
        return lida;
    }

    public void setLida(boolean lida) {
        this.lida = lida;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // ========== MÉTODOS AUXILIARES ==========

    public String getIcone() {
        switch (tipo) {
            case "CHAMADO_CRIADO":
                return "🎫";
            case "COMENTARIO":
                return "💬";
            case "STATUS_ALTERADO":
                return "🔄";
            case "AVALIACAO":
                return "⭐";
            case "LEMBRETE":
                return "⏰";
            default:
                return "🔔";
        }
    }

    public int getCorTipo() {
        switch (tipo) {
            case "CHAMADO_CRIADO":
                return 0xFF2196F3; // Azul
            case "COMENTARIO":
                return 0xFF4CAF50; // Verde
            case "STATUS_ALTERADO":
                return 0xFFFF9800; // Laranja
            case "AVALIACAO":
                return 0xFFFFC107; // Amarelo
            case "LEMBRETE":
                return 0xFFF44336; // Vermelho
            default:
                return 0xFF9E9E9E; // Cinza
        }
    }

    public String getTempoDecorrido() {
        // Implementar lógica de tempo decorrido
        // Similar ao Chamado.getTempoDecorrido()
        return "Agora";
    }
}