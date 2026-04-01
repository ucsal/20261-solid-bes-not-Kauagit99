package br.com.ucsal.olimpiadas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProvaRepository {
    private final List<Prova> provas = new ArrayList<>();
    private long proximaId = 1;

    public void salvar(Prova p) {
        p.setId(proximaId++);
        provas.add(p);
    }

    public List<Prova> buscarTodas() {
        return new ArrayList<>(provas);
    }

    public Optional<Prova> buscarPorId(long id) {
        return provas.stream().filter(p -> p.getId() == id).findFirst();
    }

    public boolean isVazio() {
        return provas.isEmpty();
    }
}