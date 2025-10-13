package br.com.oficinas.gestaooficinas.cli;

import br.com.oficinas.gestaooficinas.core.IoUtils;
import br.com.oficinas.gestaooficinas.domain.Role;
import br.com.oficinas.gestaooficinas.domain.Usuario;
import br.com.oficinas.gestaooficinas.cli.menu.MenuCliente;
import br.com.oficinas.gestaooficinas.cli.menu.MenuDono;
import br.com.oficinas.gestaooficinas.cli.menu.MenuVoluntario;
import br.com.oficinas.gestaooficinas.service.AuthService;
import br.com.oficinas.gestaooficinas.service.SessionService;
import org.springframework.stereotype.Component;

@Component
public class MenuPrincipal {

    private final IoUtils io;
    private final AuthService authService;
    private final SessionService session;
    private final MenuDono menuDono;
    private final MenuVoluntario menuVoluntario;
    private final MenuCliente menuCliente;

    public MenuPrincipal(IoUtils io, AuthService authService, SessionService session,
                         MenuDono menuDono, MenuVoluntario menuVoluntario, MenuCliente menuCliente) {
        this.io = io;
        this.authService = authService;
        this.session = session;
        this.menuDono = menuDono;
        this.menuVoluntario = menuVoluntario;
        this.menuCliente = menuCliente;
    }

    public void iniciar() {
        boolean executando = true;
        while (executando) {
            if (!session.isAutenticado()) {
                executando = telaBoasVindas();
            } else {
                abreMenuDoPapel();
            }
        }
        io.println("\nSistema encerrado. Até logo!");
    }

    private boolean telaBoasVindas() {
        io.println("========================================");
        io.println("  Sistema de Gestão de Oficinas (CLI)  ");
        io.println("========================================");
        io.println("1) Entrar");
        io.println("2) Criar conta");
        io.println("0) Sair");
        io.println("----------------------------------------");

        int op = io.lerInteiro("Escolha: ");
        return switch (op) {
            case 1 -> { login(); yield true; }
            case 2 -> { criarConta(); yield true; }
            case 0 -> false;
            default -> { io.println("Opção inválida."); yield true; }
        };
    }

    private void login() {
        String email = io.lerLinha("E-mail: ").trim();
        String senha = io.lerLinha("Senha: ").trim(); // Em CLI não dá para mascarar fácil com Scanner

        try {
            Usuario u = authService.login(email, senha);
            session.login(u);
            io.println("Login efetuado como " + u.getRole() + ". Bem-vindo(a), " + u.getNome() + "!");
        } catch (Exception e) {
            io.println("Falha no login: " + e.getMessage());
        }
    }

    private void criarConta() {
        io.println("\n=== Criar Conta ===");
        String nome = io.lerLinha("Nome: ").trim();
        String email = io.lerLinha("E-mail: ").trim();
        String senha = io.lerLinha("Senha (mín. 8): ").trim();

        Role role = escolherRole();
        if (role == null) {
            io.println("Operação cancelada.");
            return;
        }

        try {
            Usuario u = authService.cadastrar(nome, email, senha, role);
            io.println("Conta criada com sucesso para o perfil " + role + ". Agora faça login.");
        } catch (Exception e) {
            io.println("Erro ao criar conta: " + e.getMessage());
        }
    }

    private Role escolherRole() {
        io.println("\nSelecione o perfil:");
        io.println("1) Dono de Oficina");
        io.println("2) Voluntário");
        io.println("3) Cliente");
        io.println("0) Cancelar");
        int op = io.lerInteiro("Escolha: ");
        return switch (op) {
            case 1 -> Role.DONO;
            case 2 -> Role.VOLUNTARIO;
            case 3 -> Role.CLIENTE;
            case 0 -> null;
            default -> { io.println("Opção inválida."); yield null; }
        };
    }

    private void abreMenuDoPapel() {
        switch (session.getUsuarioAtual().getRole()) {
            case DONO -> menuDono.abrir();
            case VOLUNTARIO -> menuVoluntario.abrir();
            case CLIENTE -> menuCliente.abrir();
        }
        // Ao sair do menu, perguntar se quer desconectar ou trocar de conta
        io.println("\n1) Desconectar");
        io.println("2) Trocar de conta");
        io.println("3) Voltar ao menu do papel");
        io.println("0) Encerrar sistema");
        int op = io.lerInteiro("Escolha: ");
        switch (op) {
            case 1 -> session.logout();
            case 2 -> { session.logout(); login(); }
            case 3 -> { /* nada: voltará ao mesmo papel no próximo loop */ }
            case 0 -> { session.logout(); System.exit(0); }
            default -> io.println("Opção inválida.");
        }
    }
}
