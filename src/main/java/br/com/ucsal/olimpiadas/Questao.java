package br.com.ucsal.olimpiadas;

public abstract class Questao {
    private long id;
    private long provaId;
    private String enunciado;
    private String fenInicial;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getProvaId() { return provaId; }
    public void setProvaId(long provaId) { this.provaId = provaId; }

    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }

    public String getFenInicial() { return fenInicial; }
    public void setFenInicial(String fenInicial) { this.fenInicial = fenInicial; }

    
    public abstract boolean isRespostaCorreta(char marcada);
}