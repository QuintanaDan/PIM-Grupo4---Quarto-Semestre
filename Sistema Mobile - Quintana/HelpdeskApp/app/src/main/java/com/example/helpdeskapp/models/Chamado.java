package com.example.helpdeskapp.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Chamado implements Serializable {

    private long id;
    private String numero;
    private String titulo;
    private String descricao;
    private String categoria;
    private String prioridade;
    private String status;
    private Date dataCriacao;
    private String resposta;
    private String usuario;
    private String createdAt;
    private String updatedAt;

    public Chamado() {
        this.dataCriacao = new Date();
        this.status = "Aberto";
        this.resposta = "";
        this.categoria = "Geral";
        this.prioridade = "Média";
    }

    // Getters e Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNumero() {
        if (numero != null && !numero.isEmpty()) {
            return numero;
        }
        return getProtocoloFormatado();
    }
    public void setNumero(String numero) { this.numero = numero; }

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

    public Date getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(Date dataCriacao) { this.dataCriacao = dataCriacao; }

    public String getResposta() { return resposta; }
    public void setResposta(String resposta) { this.resposta = resposta; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Métodos de conversão
    public long getClienteId() {
        if (usuario != null && !usuario.isEmpty()) {
            try {
                return Long.parseLong(usuario);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public void setClienteId(long clienteId) {
        this.usuario = String.valueOf(clienteId);
    }

    public String getProtocoloFormatado() {
        if (numero != null && !numero.isEmpty()) {
            return numero;
        }
        if (id > 0) {
            return String.format("CH%06d", id);
        }
        return "CH000000";
    }

    public String getDataCriacaoFormatada() {
        if (dataCriacao == null) return "Data não informada";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(dataCriacao);
    }

    public String getDataCriacaoFormatadaComHora() {
        if (dataCriacao == null) return "Data não informada";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(dataCriacao);
    }

    public String getCorPrioridade() {
        if (prioridade == null) return "#757575";
        switch (prioridade.toLowerCase()) {
            case "alta": return "#F44336";
            case "média": return "#FF9800";
            case "baixa": return "#4CAF50";
            default: return "#757575";
        }
    }

    public String getCorStatus() {
        if (status == null) return "#757575";
        switch (status.toLowerCase()) {
            case "aberto": return "#4CAF50";
            case "em andamento": return "#FF9800";
            case "fechado": return "#2196F3";
            default: return "#757575";
        }
    }

    public boolean isValido() {
        return titulo != null && !titulo.trim().isEmpty() &&
                descricao != null && !descricao.trim().isEmpty();
    }
}