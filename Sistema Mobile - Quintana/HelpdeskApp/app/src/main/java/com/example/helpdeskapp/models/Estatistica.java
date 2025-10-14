package com.example.helpdeskapp.models;

import java.io.Serializable;

public class Estatistica implements Serializable {
    private int totalChamados;
    private int chamadosAbertos;
    private int chamadosEmAndamento;
    private int chamadosFechados;
    private int chamadosResolvidos;

    private int totalUsuarios;
    private int totalClientes;
    private int totalAdmins;

    private int chamadosHoje;
    private int chamadosSemana;
    private int chamadosMes;

    private int avaliacoesPositivas;
    private int avaliacoesNegativas;
    private float mediaAvaliacoes;

    private int prioridadeAlta;
    private int prioridadeMedia;
    private int prioridadeBaixa;

    public Estatistica() {
        // Construtor vazio
    }

    // ========== GETTERS E SETTERS ==========

    public int getTotalChamados() {
        return totalChamados;
    }

    public void setTotalChamados(int totalChamados) {
        this.totalChamados = totalChamados;
    }

    public int getChamadosAbertos() {
        return chamadosAbertos;
    }

    public void setChamadosAbertos(int chamadosAbertos) {
        this.chamadosAbertos = chamadosAbertos;
    }

    public int getChamadosEmAndamento() {
        return chamadosEmAndamento;
    }

    public void setChamadosEmAndamento(int chamadosEmAndamento) {
        this.chamadosEmAndamento = chamadosEmAndamento;
    }

    public int getChamadosFechados() {
        return chamadosFechados;
    }

    public void setChamadosFechados(int chamadosFechados) {
        this.chamadosFechados = chamadosFechados;
    }

    public int getChamadosResolvidos() {
        return chamadosResolvidos;
    }

    public void setChamadosResolvidos(int chamadosResolvidos) {
        this.chamadosResolvidos = chamadosResolvidos;
    }

    public int getTotalUsuarios() {
        return totalUsuarios;
    }

    public void setTotalUsuarios(int totalUsuarios) {
        this.totalUsuarios = totalUsuarios;
    }

    public int getTotalClientes() {
        return totalClientes;
    }

    public void setTotalClientes(int totalClientes) {
        this.totalClientes = totalClientes;
    }

    public int getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(int totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public int getChamadosHoje() {
        return chamadosHoje;
    }

    public void setChamadosHoje(int chamadosHoje) {
        this.chamadosHoje = chamadosHoje;
    }

    public int getChamadosSemana() {
        return chamadosSemana;
    }

    public void setChamadosSemana(int chamadosSemana) {
        this.chamadosSemana = chamadosSemana;
    }

    public int getChamadosMes() {
        return chamadosMes;
    }

    public void setChamadosMes(int chamadosMes) {
        this.chamadosMes = chamadosMes;
    }

    public int getAvaliacoesPositivas() {
        return avaliacoesPositivas;
    }

    public void setAvaliacoesPositivas(int avaliacoesPositivas) {
        this.avaliacoesPositivas = avaliacoesPositivas;
    }

    public int getAvaliacoesNegativas() {
        return avaliacoesNegativas;
    }

    public void setAvaliacoesNegativas(int avaliacoesNegativas) {
        this.avaliacoesNegativas = avaliacoesNegativas;
    }

    public float getMediaAvaliacoes() {
        return mediaAvaliacoes;
    }

    public void setMediaAvaliacoes(float mediaAvaliacoes) {
        this.mediaAvaliacoes = mediaAvaliacoes;
    }

    public int getPrioridadeAlta() {
        return prioridadeAlta;
    }

    public void setPrioridadeAlta(int prioridadeAlta) {
        this.prioridadeAlta = prioridadeAlta;
    }

    public int getPrioridadeMedia() {
        return prioridadeMedia;
    }

    public void setPrioridadeMedia(int prioridadeMedia) {
        this.prioridadeMedia = prioridadeMedia;
    }

    public int getPrioridadeBaixa() {
        return prioridadeBaixa;
    }

    public void setPrioridadeBaixa(int prioridadeBaixa) {
        this.prioridadeBaixa = prioridadeBaixa;
    }

    // ========== MÉTODOS AUXILIARES ==========

    public int getTaxaResolucao() {
        if (totalChamados == 0) return 0;
        return (int) ((chamadosResolvidos * 100.0f) / totalChamados);
    }

    public int getPorcentagemAbertos() {
        if (totalChamados == 0) return 0;
        return (int) ((chamadosAbertos * 100.0f) / totalChamados);
    }

    public int getPorcentagemAndamento() {
        if (totalChamados == 0) return 0;
        return (int) ((chamadosEmAndamento * 100.0f) / totalChamados);
    }

    public int getPorcentagemFechados() {
        if (totalChamados == 0) return 0;
        return (int) ((chamadosFechados * 100.0f) / totalChamados);
    }

    public String getEstrelasMedio() {
        int estrelas = Math.round(mediaAvaliacoes);
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < estrelas) {
                resultado.append("⭐");
            } else {
                resultado.append("☆");
            }
        }
        return resultado.toString();
    }

    @Override
    public String toString() {
        return "Estatistica{" +
                "totalChamados=" + totalChamados +
                ", abertos=" + chamadosAbertos +
                ", andamento=" + chamadosEmAndamento +
                ", fechados=" + chamadosFechados +
                ", resolvidos=" + chamadosResolvidos +
                '}';
    }
}