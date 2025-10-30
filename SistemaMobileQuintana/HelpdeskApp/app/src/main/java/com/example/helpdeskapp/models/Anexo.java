package com.example.helpdeskapp.models;

import java.io.Serializable;

public class Anexo implements Serializable {
    private long id;
    private long chamadoId;
    private String nomeArquivo;
    private String caminho;
    private String tipo; // image/jpeg, image/png, application/pdf
    private long tamanho; // em bytes
    private String dataUpload;

    public Anexo() {}

    public Anexo(long chamadoId, String nomeArquivo, String caminho, String tipo, long tamanho) {
        this.chamadoId = chamadoId;
        this.nomeArquivo = nomeArquivo;
        this.caminho = caminho;
        this.tipo = tipo;
        this.tamanho = tamanho;
    }

    // Getters e Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getChamadoId() {
        return chamadoId;
    }

    public void setChamadoId(long chamadoId) {
        this.chamadoId = chamadoId;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public String getCaminho() {
        return caminho;
    }

    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public long getTamanho() {
        return tamanho;
    }

    public void setTamanho(long tamanho) {
        this.tamanho = tamanho;
    }

    public String getDataUpload() {
        return dataUpload;
    }

    public void setDataUpload(String dataUpload) {
        this.dataUpload = dataUpload;
    }

    // Métodos auxiliares
    public String getTamanhoFormatado() {
        if (tamanho < 1024) {
            return tamanho + " B";
        } else if (tamanho < 1024 * 1024) {
            return String.format("%.2f KB", tamanho / 1024.0);
        } else {
            return String.format("%.2f MB", tamanho / (1024.0 * 1024.0));
        }
    }

    public boolean isImagem() {
        return tipo != null && tipo.startsWith("image/");
    }

    public boolean isPDF() {
        return tipo != null && tipo.equals("application/pdf");
    }

    public String getIcone() {
        if (isImagem()) {
            return "🖼️";
        } else if (isPDF()) {
            return "📄";
        } else {
            return "📎";
        }
    }

    @Override
    public String toString() {
        return "Anexo{" +
                "id=" + id +
                ", chamadoId=" + chamadoId +
                ", nomeArquivo='" + nomeArquivo + '\'' +
                ", tipo='" + tipo + '\'' +
                ", tamanho=" + getTamanhoFormatado() +
                '}';
    }
}