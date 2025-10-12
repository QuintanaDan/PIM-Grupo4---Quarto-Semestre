package com.example.helpdeskapp.models;

import java.io.Serializable;

public class Tag implements Serializable {
    private long id;
    private String nome;
    private String cor; // Formato hexadecimal: #FF0000
    private String createdAt;

    public Tag() {}

    public Tag(String nome, String cor) {
        this.nome = nome;
        this.cor = cor;
    }

    public Tag(long id, String nome, String cor) {
        this.id = id;
        this.nome = nome;
        this.cor = cor;
    }

    // Getters e Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Métodos auxiliares
    public int getCorInt() {
        try {
            return android.graphics.Color.parseColor(cor);
        } catch (Exception e) {
            return android.graphics.Color.parseColor("#808080"); // Cinza padrão
        }
    }

    public String getEmoji() {
        // Retornar emoji baseado no nome da tag
        if (nome == null) return "🏷️";

        String nomeLower = nome.toLowerCase();

        if (nomeLower.contains("urgente") || nomeLower.contains("crítico")) {
            return "🚨";
        } else if (nomeLower.contains("importante")) {
            return "⚠️";
        } else if (nomeLower.contains("bug")) {
            return "🐛";
        } else if (nomeLower.contains("melhoria")) {
            return "✨";
        } else if (nomeLower.contains("dúvida") || nomeLower.contains("duvida")) {
            return "❓";
        } else if (nomeLower.contains("documentação") || nomeLower.contains("documentacao")) {
            return "📄";
        } else if (nomeLower.contains("treinamento")) {
            return "🎓";
        } else if (nomeLower.contains("atualização") || nomeLower.contains("atualizacao")) {
            return "🔄";
        } else {
            return "🏷️";
        }
    }

    @Override
    public String toString() {
        return nome;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tag tag = (Tag) obj;
        return id == tag.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}