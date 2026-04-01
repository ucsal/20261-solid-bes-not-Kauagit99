package br.com.ucsal.olimpiadas;

public class CalculadoraNotaPadrao implements CalculadoraNota {

    @Override
    public int calcular(Tentativa tentativa) {
        int acertos = 0;
        for (Resposta r : tentativa.getRespostas()) {
            if (r.isCorreta()) {
                acertos++;
            }
        }
        return acertos;
    }
}