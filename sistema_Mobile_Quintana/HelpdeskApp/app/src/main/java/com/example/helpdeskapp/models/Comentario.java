package com.example.helpdeskapp.models;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Comentario implements Serializable {
    private long id;
    private long chamadoId;
    private long usuarioId;
    private String texto;
    private String dataCriacao;
    private String tipo;
    private String nomeUsuario; // Para exibição

    // Constructors
    public Comentario() {}

    public Comentario(long id, long chamadoId, long usuarioId, String texto, String dataCriacao, String tipo) {
        this.id = id;
        this.chamadoId = chamadoId;
        this.usuarioId = usuarioId;
        this.texto = texto;
        this.dataCriacao = dataCriacao;
        this.tipo = tipo;
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

    public long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    // Métodos auxiliares
    public String getDataCriacaoFormatada() {
        try {
            SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat formatoFinal = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
            Date data = formatoOriginal.parse(dataCriacao);
            return formatoFinal.format(data);
        } catch (ParseException e) {
            return dataCriacao;
        }
    }

    // Métodos de compatibilidade (caso sejam usados em outras partes do código)
    public String getAutor() {
        return nomeUsuario;
    }

    public void setAutor(String autor) {
        this.nomeUsuario = autor;
    }

    public String getDataComentario() {
        return dataCriacao;
    }

    public void setDataComentario(String dataComentario) {
        this.dataCriacao = dataComentario;
    }

    public String getDataComentarioFormatada() {
        return getDataCriacaoFormatada();
    }
}
