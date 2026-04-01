package br.com.ucsal.olimpiadas;

import java.util.Scanner;

public class App {


    private static final ParticipanteRepository participanteRepo = new ParticipanteRepository();
    private static final ProvaRepository provaRepo = new ProvaRepository();
    private static final QuestaoRepository questaoRepo = new QuestaoRepository();
    private static final TentativaRepository tentativaRepo = new TentativaRepository();
    private static final AvaliacaoService avaliacaoService = new AvaliacaoService();
    
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        seed();

        while (true) {
            System.out.println("\n=== OLIMPÍADA DE QUESTÕES (V1) ===");
            System.out.println("1) Cadastrar participante");
            System.out.println("2) Cadastrar prova");
            System.out.println("3) Cadastrar questão (A–E) em uma prova");
            System.out.println("4) Aplicar prova (selecionar participante + prova)");
            System.out.println("5) Listar tentativas (resumo)");
            System.out.println("0) Sair");
            System.out.print("> ");

            switch (in.nextLine()) {
                case "1" -> cadastrarParticipante();
                case "2" -> cadastrarProva();
                case "3" -> cadastrarQuestao();
                case "4" -> aplicarProva();
                case "5" -> listarTentativas();
                case "0" -> {
                    System.out.println("tchau");
                    return;
                }
                default -> System.out.println("opção inválida");
            }
        }
    }

    static void cadastrarParticipante() {
        System.out.print("Nome: ");
        var nome = in.nextLine();

        System.out.print("Email (opcional): ");
        var email = in.nextLine();

        if (nome == null || nome.isBlank()) {
            System.out.println("nome inválido");
            return;
        }

        var p = new Participante();
        p.setNome(nome);
        p.setEmail(email);

        participanteRepo.salvar(p);
        System.out.println("Participante cadastrado: " + p.getId());
    }

    static void cadastrarProva() {
        System.out.print("Título da prova: ");
        var titulo = in.nextLine();

        if (titulo == null || titulo.isBlank()) {
            System.out.println("título inválido");
            return;
        }

        var prova = new Prova();
        prova.setTitulo(titulo);

        provaRepo.salvar(prova);
        System.out.println("Prova criada: " + prova.getId());
    }

    static void cadastrarQuestao() {
        if (provaRepo.isVazio()) {
            System.out.println("não há provas cadastradas");
            return;
        }

        var provaId = escolherProva();
        if (provaId == null) return;

        System.out.println("Enunciado:");
        var enunciado = in.nextLine();

        var alternativas = new String[5];
        for (int i = 0; i < 5; i++) {
            char letra = (char) ('A' + i);
            System.out.print("Alternativa " + letra + ": ");
            alternativas[i] = letra + ") " + in.nextLine();
        }

        System.out.print("Alternativa correta (A–E): ");
        char correta;
        try {
            correta = Questao.normalizar(in.nextLine().trim().charAt(0));
        } catch (Exception e) {
            System.out.println("alternativa inválida");
            return;
        }

        var q = new Questao();
        q.setProvaId(provaId);
        q.setEnunciado(enunciado);
        q.setAlternativas(alternativas);
        q.setAlternativaCorreta(correta);

        

        questaoRepo.salvar(q);
        System.out.println("Questão cadastrada: " + q.getId() + " (na prova " + provaId + ")");
    }

    static void aplicarProva() {
        if (participanteRepo.isVazio()) {
            System.out.println("cadastre participantes primeiro");
            return;
        }
        if (provaRepo.isVazio()) {
            System.out.println("cadastre provas primeiro");
            return;
        }

        var participanteId = escolherParticipante();
        if (participanteId == null) return;

        var provaId = escolherProva();
        if (provaId == null) return;

        var questoesDaProva = questaoRepo.buscarPorProvaId(provaId);

        if (questoesDaProva.isEmpty()) {
            System.out.println("esta prova não possui questões cadastradas");
            return;
        }

        var tentativa = new Tentativa();
        tentativa.setParticipanteId(participanteId);
        tentativa.setProvaId(provaId);

        System.out.println("\n--- Início da Prova ---");

        for (var q : questoesDaProva) {
            System.out.println("\nQuestão #" + q.getId());
            System.out.println(q.getEnunciado());

            if (q.getFenInicial() != null && !q.getFenInicial().isBlank()) {
                System.out.println("Posição inicial:");
                imprimirTabuleiroFen(q.getFenInicial());
            }

            for (var alt : q.getAlternativas()) {
                System.out.println(alt);
            }

            System.out.print("Sua resposta (A–E): ");
            char marcada;
            try {
                marcada = Questao.normalizar(in.nextLine().trim().charAt(0));
            } catch (Exception e) {
                System.out.println("resposta inválida (marcando como errada)");
                marcada = 'X';
            }

            var r = new Resposta();
            r.setQuestaoId(q.getId());
            r.setAlternativaMarcada(marcada);
            r.setCorreta(q.isRespostaCorreta(marcada));

            tentativa.getRespostas().add(r);
        }

        tentativaRepo.salvar(tentativa);

        int nota = avaliacaoService.calcularNota(tentativa);
        System.out.println("\n--- Fim da Prova ---");
        System.out.println("Nota (acertos): " + nota + " / " + tentativa.getRespostas().size());
    }

    static void listarTentativas() {
        System.out.println("\n--- Tentativas ---");
        for (var t : tentativaRepo.buscarTodas()) {
            System.out.printf("#%d | participante=%d | prova=%d | nota=%d/%d%n", 
                t.getId(), 
                t.getParticipanteId(),
                t.getProvaId(), 
                avaliacaoService.calcularNota(t), 
                t.getRespostas().size()
            );
        }
    }

    static Long escolherParticipante() {
        System.out.println("\nParticipantes:");
        for (var p : participanteRepo.buscarTodos()) {
            System.out.printf("  %d) %s%n", p.getId(), p.getNome());
        }
        System.out.print("Escolha o id do participante: ");

        try {
            long id = Long.parseLong(in.nextLine());
            return participanteRepo.buscarPorId(id).map(Participante::getId).orElseGet(() -> {
                System.out.println("id inválido");
                return null;
            });
        } catch (Exception e) {
            System.out.println("entrada inválida");
            return null;
        }
    }

    static Long escolherProva() {
        System.out.println("\nProvas:");
        for (var p : provaRepo.buscarTodas()) {
            System.out.printf("  %d) %s%n", p.getId(), p.getTitulo());
        }
        System.out.print("Escolha o id da prova: ");

        try {
            long id = Long.parseLong(in.nextLine());
            return provaRepo.buscarPorId(id).map(Prova::getId).orElseGet(() -> {
                System.out.println("id inválido");
                return null;
            });
        } catch (Exception e) {
            System.out.println("entrada inválida");
            return null;
        }
    }

    static void imprimirTabuleiroFen(String fen) {
        String parteTabuleiro = fen.split(" ")[0];
        String[] ranks = parteTabuleiro.split("/");

        System.out.println();
        System.out.println("    a b c d e f g h");
        System.out.println("   -----------------");

        for (int r = 0; r < 8; r++) {
            String rank = ranks[r];
            System.out.print((8 - r) + " | ");

            for (char c : rank.toCharArray()) {
                if (Character.isDigit(c)) {
                    int vazios = c - '0';
                    for (int i = 0; i < vazios; i++) {
                        System.out.print(". ");
                    }
                } else {
                    System.out.print(c + " ");
                }
            }
            System.out.println("| " + (8 - r));
        }

        System.out.println("   -----------------");
        System.out.println("    a b c d e f g h");
        System.out.println();
    }

    static void seed() {
        var prova = new Prova();
        prova.setTitulo("Olimpíada 2026 • Nível 1 • Prova A");
        provaRepo.salvar(prova);

        var q1 = new Questao();
        q1.setProvaId(prova.getId());
        q1.setEnunciado("Questão 1 — Mate em 1.\nÉ a vez das brancas.\nEncontre o lance que dá mate imediatamente.\n");
        q1.setFenInicial("6k1/5ppp/8/8/8/7Q/6PP/6K1 w - - 0 1");
        q1.setAlternativas(new String[] { "A) Qh7#", "B) Qf5#", "C) Qc8#", "D) Qh8#", "E) Qe6#" });
        q1.setAlternativaCorreta('C');
        
        questaoRepo.salvar(q1);
    }
}