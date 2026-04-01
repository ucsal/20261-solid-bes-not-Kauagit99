package br.com.ucsal.olimpiadas;

public class App {

    private static final InteracaoUsuario interacao = new InteracaoConsole();
    private static final ParticipanteRepository participanteRepo = new ParticipanteRepository();
    private static final ProvaRepository provaRepo = new ProvaRepository();
    private static final QuestaoRepository questaoRepo = new QuestaoRepository();
    private static final TentativaRepository tentativaRepo = new TentativaRepository();
    
    private static final CalculadoraNota calculadora = new CalculadoraNotaPadrao();
    private static final AvaliacaoService avaliacaoService = new AvaliacaoService(calculadora);

    public static void main(String[] args) {
        seed();

        while (true) {
            interacao.mostrarMensagem("\n=== OLIMPÍADA DE QUESTÕES (SOLID FINAL) ===");
            interacao.mostrarMensagem("1) Cadastrar participante");
            interacao.mostrarMensagem("2) Cadastrar prova");
            interacao.mostrarMensagem("3) Cadastrar questão (A–E) em uma prova");
            interacao.mostrarMensagem("4) Aplicar prova (selecionar participante + prova)");
            interacao.mostrarMensagem("5) Listar tentativas (resumo)");
            interacao.mostrarMensagem("0) Sair");
            
            String opcao = interacao.lerTexto("> ");

            switch (opcao) {
                case "1" -> cadastrarParticipante();
                case "2" -> cadastrarProva();
                case "3" -> cadastrarQuestao();
                case "4" -> aplicarProva();
                case "5" -> listarTentativas();
                case "0" -> {
                    interacao.mostrarMensagem("tchau");
                    return;
                }
                default -> interacao.mostrarMensagem("opção inválida");
            }
        }
    }

    static void cadastrarParticipante() {
        var nome = interacao.lerTexto("Nome: ");
        var email = interacao.lerTexto("Email (opcional): ");

        if (nome == null || nome.isBlank()) {
            interacao.mostrarMensagem("nome inválido");
            return;
        }

        var p = new Participante();
        p.setNome(nome);
        p.setEmail(email);

        participanteRepo.salvar(p);
        interacao.mostrarMensagem("Participante cadastrado: " + p.getId());
    }

    static void cadastrarProva() {
        var titulo = interacao.lerTexto("Título da prova: ");

        if (titulo == null || titulo.isBlank()) {
            interacao.mostrarMensagem("título inválido");
            return;
        }

        var prova = new Prova();
        prova.setTitulo(titulo);

        provaRepo.salvar(prova);
        interacao.mostrarMensagem("Prova criada: " + prova.getId());
    }

    static void cadastrarQuestao() {
        if (provaRepo.isVazio()) {
            interacao.mostrarMensagem("não há provas cadastradas");
            return;
        }

        var provaId = escolherProva();
        if (provaId == null) return;

        var enunciado = interacao.lerTexto("Enunciado:\n");

        var alternativas = new String[5];
        for (int i = 0; i < 5; i++) {
            char letra = (char) ('A' + i);
            alternativas[i] = letra + ") " + interacao.lerTexto("Alternativa " + letra + ": ");
        }

        char correta;
        try {
            String respostaStr = interacao.lerTexto("Alternativa correta (A–E): ");
            correta = QuestaoMultiplaEscolha.normalizar(respostaStr.trim().charAt(0));
        } catch (Exception e) {
            interacao.mostrarMensagem("alternativa inválida");
            return;
        }

        var q = new QuestaoMultiplaEscolha();
        q.setProvaId(provaId);
        q.setEnunciado(enunciado);
        q.setAlternativas(alternativas);
        q.setAlternativaCorreta(correta);

        questaoRepo.salvar(q);
        interacao.mostrarMensagem("Questão cadastrada: " + q.getId() + " (na prova " + provaId + ")");
    }

    static void aplicarProva() {
        if (participanteRepo.isVazio()) {
            interacao.mostrarMensagem("cadastre participantes primeiro");
            return;
        }
        if (provaRepo.isVazio()) {
            interacao.mostrarMensagem("cadastre provas primeiro");
            return;
        }

        var participanteId = escolherParticipante();
        if (participanteId == null) return;

        var provaId = escolherProva();
        if (provaId == null) return;

        var questoesDaProva = questaoRepo.buscarPorProvaId(provaId);

        if (questoesDaProva.isEmpty()) {
            interacao.mostrarMensagem("esta prova não possui questões cadastradas");
            return;
        }

        var tentativa = new Tentativa();
        tentativa.setParticipanteId(participanteId);
        tentativa.setProvaId(provaId);

        interacao.mostrarMensagem("\n--- Início da Prova ---");

        for (var q : questoesDaProva) {
            interacao.mostrarMensagem("\nQuestão #" + q.getId());
            interacao.mostrarMensagem(q.getEnunciado());

            if (q.getFenInicial() != null && !q.getFenInicial().isBlank()) {
                interacao.mostrarMensagem("Posição inicial:");
                imprimirTabuleiroFen(q.getFenInicial());
            }

            if (q instanceof QuestaoMultiplaEscolha qme) {
                for (var alt : qme.getAlternativas()) {
                    interacao.mostrarMensagem(alt);
                }
            }

            char marcada;
            try {
                String respStr = interacao.lerTexto("Sua resposta (A–E): ");
                marcada = QuestaoMultiplaEscolha.normalizar(respStr.trim().charAt(0));
            } catch (Exception e) {
                interacao.mostrarMensagem("resposta inválida (marcando como errada)");
                marcada = 'X';
            }

            var r = new Resposta();
            r.setQuestaoId(q.getId());
            r.setAlternativaMarcada(marcada);
            r.setCorreta(q.isRespostaCorreta(marcada));

            tentativa.getRespostas().add(r);
        }

        tentativaRepo.salvar(tentativa);

        int nota = avaliacaoService.processarNotaFinal(tentativa);
        interacao.mostrarMensagem("\n--- Fim da Prova ---");
        interacao.mostrarMensagem("Nota (acertos): " + nota + " / " + tentativa.getRespostas().size());
    }

    static void listarTentativas() {
        interacao.mostrarMensagem("\n--- Tentativas ---");
        for (var t : tentativaRepo.buscarTodas()) {
            String texto = String.format("#%d | participante=%d | prova=%d | nota=%d/%d", 
                t.getId(), t.getParticipanteId(), t.getProvaId(), 
                avaliacaoService.processarNotaFinal(t), t.getRespostas().size());
            interacao.mostrarMensagem(texto);
        }
    }

    static Long escolherParticipante() {
        interacao.mostrarMensagem("\nParticipantes:");
        for (var p : participanteRepo.buscarTodos()) {
            interacao.mostrarMensagem(String.format("  %d) %s", p.getId(), p.getNome()));
        }
        
        try {
            String idStr = interacao.lerTexto("Escolha o id do participante: ");
            long id = Long.parseLong(idStr);
            return participanteRepo.buscarPorId(id).map(Participante::getId).orElseGet(() -> {
                interacao.mostrarMensagem("id inválido");
                return null;
            });
        } catch (Exception e) {
            interacao.mostrarMensagem("entrada inválida");
            return null;
        }
    }

    static Long escolherProva() {
        interacao.mostrarMensagem("\nProvas:");
        for (var p : provaRepo.buscarTodas()) {
            interacao.mostrarMensagem(String.format("  %d) %s", p.getId(), p.getTitulo()));
        }
        
        try {
            String idStr = interacao.lerTexto("Escolha o id da prova: ");
            long id = Long.parseLong(idStr);
            return provaRepo.buscarPorId(id).map(Prova::getId).orElseGet(() -> {
                interacao.mostrarMensagem("id inválido");
                return null;
            });
        } catch (Exception e) {
            interacao.mostrarMensagem("entrada inválida");
            return null;
        }
    }

    static void imprimirTabuleiroFen(String fen) {
        StringBuilder sb = new StringBuilder();
        String parteTabuleiro = fen.split(" ")[0];
        String[] ranks = parteTabuleiro.split("/");

        sb.append("\n    a b c d e f g h\n");
        sb.append("   -----------------\n");

        for (int r = 0; r < 8; r++) {
            String rank = ranks[r];
            sb.append((8 - r)).append(" | ");

            for (char c : rank.toCharArray()) {
                if (Character.isDigit(c)) {
                    int vazios = c - '0';
                    for (int i = 0; i < vazios; i++) {
                        sb.append(". ");
                    }
                } else {
                    sb.append(c).append(" ");
                }
            }
            sb.append("| ").append((8 - r)).append("\n");
        }

        sb.append("   -----------------\n");
        sb.append("    a b c d e f g h\n");
        
        interacao.mostrarMensagem(sb.toString());
    }

    static void seed() {
        var prova = new Prova();
        prova.setTitulo("Olimpíada 2026 • Nível 1 • Prova A");
        provaRepo.salvar(prova);

        var q1 = new QuestaoMultiplaEscolha();
        q1.setProvaId(prova.getId());
        q1.setEnunciado("Questão 1 — Mate em 1.\nÉ a vez das brancas.\nEncontre o lance que dá mate imediatamente.\n");
        q1.setFenInicial("6k1/5ppp/8/8/8/7Q/6PP/6K1 w - - 0 1");
        q1.setAlternativas(new String[] { "A) Qh7#", "B) Qf5#", "C) Qc8#", "D) Qh8#", "E) Qe6#" });
        q1.setAlternativaCorreta('C');
        
        questaoRepo.salvar(q1);
    }
}