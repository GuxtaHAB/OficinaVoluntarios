package br.com.oficinas.gestaooficinas.core;

import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.Scanner;

@Component
public class IoUtils {

    private final Scanner scanner = new Scanner(System.in);

    public void println(String msg) {
        System.out.println(msg);
    }

    public void print(String msg) {
        System.out.print(msg);
    }

    public String lerLinha(String prompt) {
        print(prompt);
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            // Em alguns ambientes, fechar o input termina a aplicação
            return "";
        }
    }

    public int lerInteiro(String prompt) {
        while (true) {
            String entrada = lerLinha(prompt);
            try {
                return Integer.parseInt(entrada.trim());
            } catch (NumberFormatException e) {
                println("Por favor, digite um número inteiro.");
            }
        }
    }

    public boolean lerConfirmacao(String prompt) {
    while (true) {
        String s = lerLinha(prompt + " (s/n): ").trim().toLowerCase();
        if (s.equals("s")) return true;
        if (s.equals("n")) return false;
        println("Responda com 's' ou 'n'.");
    }
}
}
