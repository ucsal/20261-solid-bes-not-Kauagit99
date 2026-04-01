package br.com.ucsal.olimpiadas;

import java.util.ArrayList;
import java.util.List;

public class QuestaoRepository {
    private final List<Questao> questoes = new ArrayList<>();
    private long proximaId = 1;

    public void salvar(Questao q) {
        q.setId(proximaId++);
        questoes.add(q);
    }

    public List<Questao> buscarPorProvaId(long provaId) {
        return questoes.stream().filter(q -> q.getProvaId() == provaId).toList();
    }
}