package br.com.oficinas.gestaooficinas.cli.menu;

import br.com.oficinas.gestaooficinas.core.IoUtils;
import br.com.oficinas.gestaooficinas.domain.*;
import br.com.oficinas.gestaooficinas.service.AgendamentoService;
import br.com.oficinas.gestaooficinas.service.DisponibilidadeService;
import br.com.oficinas.gestaooficinas.service.OficinaService;
import br.com.oficinas.gestaooficinas.service.SessionService;
import br.com.oficinas.gestaooficinas.service.VoluntarioService;
import org.springframework.stereotype.Component;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Component
public class MenuVoluntario {

    private final IoUtils io;
    private final SessionService session;
    private final VoluntarioService voluntarioService;
    private final OficinaService oficinaService;
    private final AgendamentoService agendamentoService;
    private final DisponibilidadeService disponibilidadeService;

    public MenuVoluntario(IoUtils io,
                          SessionService session,
                          VoluntarioService voluntarioService,
                          OficinaService oficinaService,
                          AgendamentoService agendamentoService,
                          DisponibilidadeService disponibilidadeService) {
        this.io = io;
        this.session = session;
        this.voluntarioService = voluntarioService;
        this.oficinaService = oficinaService;
        this.agendamentoService = agendamentoService;
        this.disponibilidadeService = disponibilidadeService;
    }

    public void abrir() {
        boolean loop = true;
        while (loop) {
            VoluntarioPerfil perfil = voluntarioService.obterPerfil(session.getUsuarioAtual());
            io.println("\n===== Área do Voluntário =====");
            if (perfil != null) {
                io.println("Perfil: " + perfil.getEspecialidade() +
                        (perfil.getIdade() != null ? (" | Idade: " + perfil.getIdade()) : ""));
                io.println("Afiliação: " + (perfil.getOficinaAfiliada() == null ? "(nenhuma)" :
                        ("[" + perfil.getOficinaAfiliada().getId() + "] " + perfil.getOficinaAfiliada().getNome())));
            } else {
                io.println("Perfil ainda não configurado.");
            }

            io.println("1) Definir/editar especialidade e idade");
            io.println("2) Afiliar-se a uma oficina");
            io.println("3) Desafiliar-se da oficina");
            io.println("4) Ver meus agendamentos");
            io.println("5) Atualizar status de reparos");
            io.println("6) Definir disponibilidade semanal");
            io.println("0) Sair da conta");

            int op = io.lerInteiro("Escolha: ");
            switch (op) {
                case 1 -> editarPerfil();
                case 2 -> afiliar();
                case 3 -> desafiliar();
                case 4 -> verAgendamentos();
                case 5 -> atualizarStatus();
                case 6 -> configurarDisponibilidade();
                case 0 -> { loop = false; io.println("Saindo da conta..."); }
                default -> io.println("Opção inválida.");
            }
        }
    }

    private void editarPerfil() {
        io.println("Especialidades:");
        for (int i = 0; i < Especialidade.values().length; i++) {
            io.println((i + 1) + ") " + Especialidade.values()[i]);
        }
        int sel = io.lerInteiro("Escolha a especialidade: ");
        if (sel < 1 || sel > Especialidade.values().length) {
            io.println("Opção inválida.");
            return;
        }
        Especialidade esp = Especialidade.values()[sel - 1];
        int idade = io.lerInteiro("Informe sua idade (14-120): ");
        if (idade < 14 || idade > 120) {
            io.println("Idade inválida.");
            return;
        }
        try {
            VoluntarioPerfil p = voluntarioService.criarOuAtualizarPerfil(session.getUsuarioAtual(), esp, idade);
            io.println("Perfil salvo. Especialidade: " + p.getEspecialidade() + " | Idade: " + p.getIdade());
        } catch (Exception e) {
            io.println("Erro: " + e.getMessage());
        }
    }

    private void afiliar() {
        List<Oficina> oficinas = oficinaService.listarTodas();
        if (oficinas.isEmpty()) {
            io.println("Não há oficinas cadastradas ainda.");
            return;
        }
        io.println("Oficinas disponíveis:");
        for (Oficina o : oficinas) {
            io.println("[" + o.getId() + "] " + o.getNome() + " - " + o.getCidade() + "/" +
                    (o.getUf() == null ? "" : o.getUf()));
        }
        long idEscolhido;
        try {
            idEscolhido = Long.parseLong(io.lerLinha("Informe o ID da oficina para afiliar-se: ").trim());
        } catch (NumberFormatException e) {
            io.println("ID inválido.");
            return;
        }
        try {
            VoluntarioPerfil p = voluntarioService.afiliar(session.getUsuarioAtual(), idEscolhido);
            io.println("Afiliação concluída: " + p.getOficinaAfiliada().getNome());
        } catch (Exception e) {
            io.println("Erro: " + e.getMessage());
        }
    }

    private void desafiliar() {
        try {
            voluntarioService.desafiliar(session.getUsuarioAtual());
            io.println("Você não está mais afiliado a nenhuma oficina.");
        } catch (Exception e) {
            io.println("Erro: " + e.getMessage());
        }
    }

    private void verAgendamentos() {
        VoluntarioPerfil perfil = voluntarioService.obterPerfil(session.getUsuarioAtual());
        if (perfil == null) {
            io.println("Crie seu perfil de voluntário primeiro.");
            return;
        }
        var lista = agendamentoService.listarPorVoluntario(perfil.getId());
        if (lista.isEmpty()) {
            io.println("Nenhum agendamento atribuído.");
            return;
        }
        io.println("Meus agendamentos:");
        for (Agendamento a : lista) {
            io.println("ID=" + a.getId() + " | " + a.getDataHora() + " | Item[" + a.getItem().getId() + "]: " +
                    a.getItem().getCategoria() + " - " + a.getItem().getDescricao() + " | Status: " + a.getStatus());
        }
    }

    private void atualizarStatus() {
        VoluntarioPerfil perfil = voluntarioService.obterPerfil(session.getUsuarioAtual());
        if (perfil == null) {
            io.println("Crie seu perfil de voluntário primeiro.");
            return;
        }
        var lista = agendamentoService.listarPorVoluntario(perfil.getId());
        if (lista.isEmpty()) {
            io.println("Nenhum agendamento atribuído.");
            return;
        }
        for (Agendamento a : lista) {
            io.println("ID=" + a.getId() + " | " + a.getDataHora() + " | Status: " + a.getStatus());
        }
        long id;
        try {
            id = Long.parseLong(io.lerLinha("Informe o ID do agendamento: ").trim());
        } catch (NumberFormatException e) {
            io.println("ID inválido.");
            return;
        }

        io.println("1) Marcar como PENDENTE");
        io.println("2) Marcar como CONCLUIDO");
        io.println("3) Marcar como CANCELADO");
        int sel = io.lerInteiro("Escolha: ");
        StatusReparo novo = switch (sel) {
            case 1 -> StatusReparo.PENDENTE;
            case 2 -> StatusReparo.CONCLUIDO;
            case 3 -> StatusReparo.CANCELADO;
            default -> null;
        };
        if (novo == null) {
            io.println("Opção inválida.");
            return;
        }

        try {
            var ag = agendamentoService.atualizarStatus(id, novo);
            io.println("Status atualizado: " + ag.getStatus());
        } catch (Exception e) {
            io.println("Erro: " + e.getMessage());
        }
    }private void configurarDisponibilidade() {
        VoluntarioPerfil perfil = voluntarioService.obterPerfil(session.getUsuarioAtual());
        if (perfil == null) { io.println("Crie seu perfil de voluntário primeiro."); return; }

        boolean in = true;
        while (in) {
            io.println("\n--- Minha disponibilidade ---");
            var ds = disponibilidadeService.listarDisponibilidades(perfil.getId());
            if (ds.isEmpty()) io.println("(Nenhuma disponibilidade configurada)");
            else ds.forEach(d -> io.println(d.getDiaSemana() + " - " + d.getHoraInicio() + " às " + d.getHoraFim() +
                    (d.isAtivo() ? "" : " [inativa]")));

            io.println("1) Adicionar intervalo de disponibilidade");
            io.println("0) Voltar");
            int op = io.lerInteiro("Escolha: ");
            switch (op) {
                case 1 -> adicionarDisponibilidade();
                case 0 -> in = false;
                default -> io.println("Opção inválida.");
            }
        }
    }

    private void adicionarDisponibilidade() {
        DayOfWeek dia = lerDiaSemana();
        if (dia == null) return;
        LocalTime ini = lerHora("Hora início (HH:MM): ");
        LocalTime fim = lerHora("Hora fim (HH:MM): ");
        if (ini == null || fim == null) { io.println("Horário inválido."); return; }
        try {
            var d = disponibilidadeService.definirDisponibilidadeVoluntario(session.getUsuarioAtual(), dia, ini, fim);
            io.println("Disponibilidade salva: " + d.getDiaSemana() + " " + d.getHoraInicio() + "–" + d.getHoraFim());
        } catch (Exception e) {
            io.println("Erro: " + e.getMessage());
        }
    }

    private DayOfWeek lerDiaSemana() {
        io.println("Dia da semana:");
        io.println("1) SEG  2) TER  3) QUA  4) QUI  5) SEX  6) SAB  7) DOM");
        int n = io.lerInteiro("Escolha: ");
        if (n < 1 || n > 7) { io.println("Opção inválida."); return null; }
        return DayOfWeek.of(n);
    }

    private LocalTime lerHora(String prompt) {
        String s = io.lerLinha(prompt).trim();
        try { return LocalTime.parse(s + (s.length()==5?":00":"")); }
        catch (Exception e) { return null; }
    }
}