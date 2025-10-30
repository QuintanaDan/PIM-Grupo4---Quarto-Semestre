package com.example.helpdeskapp.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Auditoria implements Serializable {
    private long id;
    private long usuarioId;
    private String nomeUsuario; // Preenchido via JOIN
    private String acao;
    private String entidade;
    private long entidadeId;
    private String descricao;
    private String ip;
    private String dispositivo;
    private String dataAcao;

    public Auditoria() {}

    public Auditoria(long usuarioId, String acao, String entidade, long entidadeId, String descricao) {
        this.usuarioId = usuarioId;
        this.acao = acao;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.descricao = descricao;
    }

    // Getters e Setters
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

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getEntidade() {
        return entidade;
    }

    public void setEntidade(String entidade) {
        this.entidade = entidade;
    }

    public long getEntidadeId() {
        return entidadeId;
    }

    public void setEntidadeId(long entidadeId) {
        this.entidadeId = entidadeId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }

    public String getDataAcao() {
        return dataAcao;
    }

    public void setDataAcao(String dataAcao) {
        this.dataAcao = dataAcao;
    }

    // Métodos auxiliares
    public String getDataFormatada() {
        if (dataAcao == null || dataAcao.isEmpty()) {
            return "Data não disponível";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dataAcao);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dataAcao;
        }
    }

    public String getIconeAcao() {
        if (acao == null) return "📋";

        switch (acao.toUpperCase()) {
            case "CRIAR":
            case "CRIOU":
                return "➕";
            case "EDITAR":
            case "EDITOU":
            case "ATUALIZAR":
            case "ATUALIZOU":
                return "✏️";
            case "DELETAR":
            case "DELETOU":
            case "REMOVER":
            case "REMOVEU":
                return "🗑️";
            case "LOGIN":
                return "🔐";
            case "LOGOUT":
                return "🚪";
            case "VISUALIZAR":
            case "VISUALIZOU":
                return "👁️";
            case "COMENTAR":
            case "COMENTOU":
                return "💬";
            case "AVALIAR":
            case "AVALIOU":
                return "⭐";
            case "ANEXAR":
            case "ANEXOU":
                return "📎";
            case "ALTERAR STATUS":
                return "🔄";
            default:
                return "📋";
        }
    }

    public String getCorAcao() {
        if (acao == null) return "#808080";

        switch (acao.toUpperCase()) {
            case "CRIAR":
            case "CRIOU":
                return "#4CAF50"; // Verde
            case "EDITAR":
            case "EDITOU":
            case "ATUALIZAR":
            case "ATUALIZOU":
                return "#2196F3"; // Azul
            case "DELETAR":
            case "DELETOU":
            case "REMOVER":
            case "REMOVEU":
                return "#F44336"; // Vermelho
            case "LOGIN":
                return "#4CAF50"; // Verde
            case "LOGOUT":
                return "#FF9800"; // Laranja
            default:
                return "#808080"; // Cinza
        }
    }

    @Override
    public String toString() {
        return "Auditoria{" +
                "id=" + id +
                ", usuarioId=" + usuarioId +
                ", acao='" + acao + '\'' +
                ", entidade='" + entidade + '\'' +
                ", entidadeId=" + entidadeId +
                ", descricao='" + descricao + '\'' +
                '}';
    }
}