package br.com.ucsal.olimpiadas;

import java.util.ArrayList;
import java.util.List;

public class TentativaRepository {
    private final List<Tentativa> tentativas = new ArrayList<>();
    private long proximaId = 1;

    public void salvar(Tentativa t) {
        t.setId(proximaId++);
        tentativas.add(t);
    }

    public List<Tentativa> buscarTodas() {
        return new ArrayList<>(tentativas);
    }
}