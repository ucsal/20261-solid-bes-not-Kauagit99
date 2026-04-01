package br.com.ucsal.olimpiadas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ParticipanteRepository {
    private final List<Participante> participantes = new ArrayList<>();
    private long proximoId = 1;

    public void salvar(Participante p) {
        p.setId(proximoId++);
        participantes.add(p);
    }

    public List<Participante> buscarTodos() {
        return new ArrayList<>(participantes);
    }

    public Optional<Participante> buscarPorId(long id) {
        return participantes.stream().filter(p -> p.getId() == id).findFirst();
    }

    public boolean isVazio() {
        return participantes.isEmpty();
    }
}