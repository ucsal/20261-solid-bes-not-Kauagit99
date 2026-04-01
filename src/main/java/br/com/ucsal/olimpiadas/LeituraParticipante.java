package br.com.ucsal.olimpiadas;

import java.util.List;
import java.util.Optional;


public interface LeituraParticipante {
    List<Participante> buscarTodos();
    Optional<Participante> buscarPorId(long id);
    boolean isVazio();
}
