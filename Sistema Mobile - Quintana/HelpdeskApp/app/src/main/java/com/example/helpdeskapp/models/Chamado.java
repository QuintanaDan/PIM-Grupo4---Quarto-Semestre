package com.example.helpdeskapp.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Chamado implements Serializable {
    private long id;
    private String titulo;
    private String descricao;
    private String categoria;
    private String prioridade;
    private String status;
    private Date dataCriacao;
    private String resposta;
    private String usuario;
    private String numero;

    // =========================================================
    // ⬇️ NOVOS CAMPOS ADICIONADOS PARA RESOLVER OS ERROS ⬇️
    // =========================================================
    private String createdAt;
    private String updatedAt;
    // =========================================================

    public Chamado() {
        this.dataCriacao = new Date();
        this.status = "Aberto";
        this.resposta = "";
        this.categoria = "Geral";
        this.prioridade = "Média";
    }

    public Chamado(String titulo, String descricao, long clienteId) {
        this();
        this.titulo = titulo;
        this.descricao = descricao;
        this.usuario = String.valueOf(clienteId);
    }

    // Getters e Setters
    // ... (Mantive seus métodos existentes) ...

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

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

    public String getNumero() {
        if (numero != null && !numero.isEmpty()) {
            return numero;
        }
        return getProtocoloFormatado();
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

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

    // =========================================================
    // ⬇️ MÉTODOS CORRIGIDOS PARA O ERRO "cannot find symbol" ⬇️
    // =========================================================

    // Resolvem os erros de setCreatedAt/setUpdatedAt no ChamadoDAO.java
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Resolvem os erros de getCreatedAt/getUpdatedAt no LembreteHelper.java
    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    // =========================================================

    public String getProtocoloFormatado() {
        if (numero != null && !numero.isEmpty()) {
            return numero;
        }
        if (id > 0) {
            return String.format("CH%06d", id);
        } else {
            long timestamp = dataCriacao != null ? dataCriacao.getTime() : System.currentTimeMillis();
            return String.format("CH%06d", (int)(timestamp % 1000000));
        }
    }

    public String getDataCriacaoFormatada() {
        if (dataCriacao == null) {
            return "Data não informada";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(dataCriacao);
    }

    public String getDataAberturaFormatada() {
        return getDataCriacaoFormatada();
    }

    public String getDataCriacaoFormatadaComHora() {
        if (dataCriacao == null) {
            return "Data não informada";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(dataCriacao);
    }

    public String getDataCriacaoFormatadaCompleta() {
        if (dataCriacao == null) {
            return "Data não informada";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy 'às' HH:mm",
                new Locale("pt", "BR"));
        return sdf.format(dataCriacao);
    }

    public String getCorPrioridade() {
        if (prioridade == null) return "#757575";
        switch (prioridade.toLowerCase()) {
            case "alta":
            case "high":
            case "crítica":
                return "#F44336";
            case "média":
            case "media":
            case "medium":
                return "#FF9800";
            case "baixa":
            case "low":
                return "#4CAF50";
            default:
                return "#757575";
        }
    }

    public String getCorStatus() {
        if (status == null) return "#757575";
        switch (status.toLowerCase()) {
            case "aberto":
            case "open":
            case "novo":
                return "#4CAF50";
            case "em andamento":
            case "em_andamento":
            case "progress":
            case "progresso":
                return "#FF9800";
            case "fechado":
            case "closed":
            case "resolvido":
            case "resolved":
                return "#2196F3";
            case "pendente":
            case "pending":
                return "#9C27B0";
            default:
                return "#757575";
        }
    }

    public boolean isValido() {
        return titulo != null && !titulo.trim().isEmpty() &&
                descricao != null && !descricao.trim().isEmpty() &&
                categoria != null && !categoria.trim().isEmpty() &&
                prioridade != null && !prioridade.trim().isEmpty();
    }

    public boolean isAberto() {
        return status != null && (status.equalsIgnoreCase("aberto") ||
                status.equalsIgnoreCase("open") ||
                status.equalsIgnoreCase("novo"));
    }

    public boolean isFechado() {
        return status != null && (status.equalsIgnoreCase("fechado") ||
                status.equalsIgnoreCase("closed") ||
                status.equalsIgnoreCase("resolvido") ||
                status.equalsIgnoreCase("resolved"));
    }

    public boolean isEmAndamento() {
        return status != null && (status.equalsIgnoreCase("em andamento") ||
                status.equalsIgnoreCase("progress") ||
                status.equalsIgnoreCase("progresso"));
    }

    public boolean isPrioridadeAlta() {
        return prioridade != null && (prioridade.equalsIgnoreCase("alta") ||
                prioridade.equalsIgnoreCase("high") ||
                prioridade.equalsIgnoreCase("crítica"));
    }

    @Override
    public String toString() {
        return "Chamado{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", categoria='" + categoria + '\'' +
                ", prioridade='" + prioridade + '\'' +
                ", status='" + status + '\'' +
                ", dataCriacao=" + getDataCriacaoFormatada() +
                ", usuario='" + usuario + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Chamado chamado = (Chamado) obj;
        return id == chamado.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    public Chamado clone() {
        Chamado clone = new Chamado();
        clone.setId(this.id);
        clone.setTitulo(this.titulo);
        clone.setDescricao(this.descricao);
        clone.setCategoria(this.categoria);
        clone.setPrioridade(this.prioridade);
        clone.setStatus(this.status);
        clone.setDataCriacao(this.dataCriacao != null ? new Date(this.dataCriacao.getTime()) : null);
        clone.setResposta(this.resposta);
        clone.setUsuario(this.usuario);
        clone.setNumero(this.numero);

        // MÉTODOS DE CLONE DOS NOVOS CAMPOS
        clone.setCreatedAt(this.createdAt);
        clone.setUpdatedAt(this.updatedAt);

        return clone;
    }
}