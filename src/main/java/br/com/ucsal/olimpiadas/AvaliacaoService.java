package br.com.ucsal.olimpiadas;

public class AvaliacaoService {
    
    private final CalculadoraNota calculadora;

    public AvaliacaoService(CalculadoraNota calculadora) {
        this.calculadora = calculadora;
    }

    public int processarNotaFinal(Tentativa tentativa) {
        return calculadora.calcular(tentativa);
    }
}