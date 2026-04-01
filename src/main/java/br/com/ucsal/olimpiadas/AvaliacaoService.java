package br.com.ucsal.olimpiadas;

public class AvaliacaoService {
    
    public int calcularNota(Tentativa tentativa) {
        int acertos = 0;
        for (Resposta r : tentativa.getRespostas()) {
            if (r.isCorreta()) {
                acertos++;
            }
        }
        return acertos;
    }
}