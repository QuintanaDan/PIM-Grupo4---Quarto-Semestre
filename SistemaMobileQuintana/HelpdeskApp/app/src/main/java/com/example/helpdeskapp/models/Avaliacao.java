package com.example.helpdeskapp.models;

import java.io.Serializable;

public class Avaliacao implements Serializable {
    private long id;
    private long chamadoId;
    private int nota; // 1 a 5 estrelas
    private String comentario;
    private String dataAvaliacao;

    public Avaliacao() {}

    public Avaliacao(long chamadoId, int nota, String comentario) {
        this.chamadoId = chamadoId;
        this.nota = nota;
        this.comentario = comentario;
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

    public int getNota() {
        return nota;
    }

    public void setNota(int nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(String dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }

    public String getNotaTexto() {
        switch (nota) {
            case 1: return "Muito Ruim";
            case 2: return "Ruim";
            case 3: return "Regular";
            case 4: return "Bom";
            case 5: return "Excelente";
            default: return "Sem avaliação";
        }
    }

    public String getEstrelas() {
        StringBuilder estrelas = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < nota) {
                estrelas.append("⭐");
            } else {
                estrelas.append("☆");
            }
        }
        return estrelas.toString();
    }

    @Override
    public String toString() {
        return "Avaliacao{" +
                "id=" + id +
                ", chamadoId=" + chamadoId +
                ", nota=" + nota +
                ", comentario='" + comentario + '\'' +
                ", dataAvaliacao='" + dataAvaliacao + '\'' +
                '}';
    }
}