package com.example.helpdeskapp.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Chamado {
    // Constantes de Status
    public static final int STATUS_ABERTO = 1;
    public static final int STATUS_EM_ANDAMENTO = 2;
    public static final int STATUS_RESOLVIDO = 3;
    public static final int STATUS_FECHADO = 4;

    // Constantes de Prioridade
    public static final int PRIORIDADE_BAIXA = 1;
    public static final int PRIORIDADE_MEDIA = 2;
    public static final int PRIORIDADE_ALTA = 3;
    public static final int PRIORIDADE_CRITICA = 4;

    // Constantes de Categoria
    public static final int CATEGORIA_HARDWARE = 1;
    public static final int CATEGORIA_SOFTWARE = 2;
    public static final int CATEGORIA_REDE = 3;
    public static final int CATEGORIA_SISTEMA = 4;
    public static final int CATEGORIA_OUTROS = 5;

    // Atributos (adicione após os outros atributos)
    private int categoria;


    // Atributos
    private long id;
    private String numero;
    private String titulo;
    private String descricao;
    private int status;
    private int prioridade;
    private long clienteId;
    private String createdAt;
    private String updatedAt;

    /// Construtor vazio
    public Chamado() {
        this.status = STATUS_ABERTO;
        this.prioridade = PRIORIDADE_MEDIA;
        this.categoria = CATEGORIA_SISTEMA; // padrão
    }

    // Construtor principal
    public Chamado(String titulo, String descricao, long clienteId) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.clienteId = clienteId;
        this.status = STATUS_ABERTO;
        this.prioridade = PRIORIDADE_MEDIA;
        this.categoria = CATEGORIA_SISTEMA; // padrão
        this.numero = gerarNumero();
    }

    public int getCategoria() {
        return categoria;
    }

    public void setCategoria(int categoria) {
        this.categoria = categoria;
    }

    public String getCategoriaTexto() {
        switch (categoria) {
            case CATEGORIA_HARDWARE: return "Hardware";
            case CATEGORIA_SOFTWARE: return "Software";
            case CATEGORIA_REDE: return "Rede";
            case CATEGORIA_SISTEMA: return "Sistema";
            case CATEGORIA_OUTROS: return "Outros";
            default: return "Sistema";
        }
    }

    public String getCategoriaTextoCompleto() {
        switch (categoria) {
            case CATEGORIA_HARDWARE: return "🔧 HARDWARE";
            case CATEGORIA_SOFTWARE: return "💻 SOFTWARE";
            case CATEGORIA_REDE: return "🌐 REDE";
            case CATEGORIA_SISTEMA: return "⚙️ SISTEMA";
            case CATEGORIA_OUTROS: return "📋 OUTROS";
            default: return "⚙️ SISTEMA";
        }
    }


    // Getters e Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(int prioridade) {
        this.prioridade = prioridade;
    }

    public long getClienteId() {
        return clienteId;
    }

    public void setClienteId(long clienteId) {
        this.clienteId = clienteId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Métodos utilitários
    private String gerarNumero() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        return "CH" + timestamp.substring(2); // Remove os primeiros 2 dígitos do ano
    }

    public String getStatusTexto() {
        switch (status) {
            case STATUS_ABERTO: return "Aberto";
            case STATUS_EM_ANDAMENTO: return "Em Andamento";
            case STATUS_RESOLVIDO: return "Resolvido";
            case STATUS_FECHADO: return "Fechado";
            default: return "Desconhecido";
        }
    }

    public String getPrioridadeTexto() {
        switch (prioridade) {
            case PRIORIDADE_BAIXA: return "Baixa";
            case PRIORIDADE_MEDIA: return "Média";
            case PRIORIDADE_ALTA: return "Alta";
            case PRIORIDADE_CRITICA: return "Crítica";
            default: return "Média";
        }
    }

    public String getPrioridadeTextoCompleto() {
        switch (prioridade) {
            case PRIORIDADE_BAIXA: return "🟢 BAIXA";
            case PRIORIDADE_MEDIA: return "🟡 MÉDIA";
            case PRIORIDADE_ALTA: return "🟠 ALTA";
            case PRIORIDADE_CRITICA: return "🔴 CRÍTICA";
            default: return "🟡 MÉDIA";
        }
    }

    public boolean isAberto() {
        return status == STATUS_ABERTO;
    }

    public boolean isEmAndamento() {
        return status == STATUS_EM_ANDAMENTO;
    }

    public boolean isResolvido() {
        return status == STATUS_RESOLVIDO;
    }

    public boolean isFechado() {
        return status == STATUS_FECHADO;
    }

    public boolean isPrioridadeCritica() {
        return prioridade == PRIORIDADE_CRITICA;
    }

    public boolean isPrioridadeAlta() {
        return prioridade == PRIORIDADE_ALTA || prioridade == PRIORIDADE_CRITICA;
    }
}
