package br.com.ucsal.olimpiadas;

import java.util.Scanner;

public class InteracaoConsole implements InteracaoUsuario {
    private final Scanner sc = new Scanner(System.in);

    @Override
    public void mostrarMensagem(String mensagem) {
        System.out.println(mensagem);
    }

    @Override
    public String lerTexto(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }
}