package br.com.oficinas.gestaooficinas.cli.menu;

import br.com.oficinas.gestaooficinas.core.IoUtils;
import br.com.oficinas.gestaooficinas.domain.Agendamento;
import br.com.oficinas.gestaooficinas.domain.Oficina;
import br.com.oficinas.gestaooficinas.domain.StatusReparo;
import br.com.oficinas.gestaooficinas.domain.VoluntarioPerfil;
import br.com.oficinas.gestaooficinas.service.AgendamentoService;
import br.com.oficinas.gestaooficinas.service.DisponibilidadeService;
import br.com.oficinas.gestaooficinas.service.OficinaService;
import br.com.oficinas.gestaooficinas.service.RelatorioService;
import br.com.oficinas.gestaooficinas.service.SessionService;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class MenuDono {

    private final IoUtils io;
    private final SessionService session;
    private final OficinaService oficinaService;
    private final AgendamentoService agendamentoService;
    private final RelatorioService relatorioService;
    private final DisponibilidadeService disponibilidadeService;

    public MenuDono(IoUtils io, SessionService session, OficinaService oficinaService,
                    AgendamentoService agendamentoService, RelatorioService relatorioService,
                    DisponibilidadeService disponibilidadeService) {
        this.io = io;
        this.session = session;
        this.oficinaService = oficinaService;
        this.agendamentoService = agendamentoService;
        this.relatorioService = relatorioService;
        this.disponibilidadeService = disponibilidadeService;
    }

    public void abrir() {
        boolean loop = true;
        while (loop) {
            io.println("\n===== Área do Dono de Oficina =====");
            Oficina minha = oficinaService.buscarPorDono(session.getUsuarioAtual());
            if (minha != null) {
                io.println("Sua oficina: [" + minha.getId() + "] " + minha.getNome() + " - " + minha.getCidade() + "/" +
                        (minha.getUf() == null ? "" : minha.getUf()));
            } else {
                io.println("Você ainda não cadastrou sua oficina.");
            }

            io.println("1) Cadastrar/editar minha oficina");
            io.println("2) Listar voluntários afiliados à minha oficina");
            io.println("3) Controlar horários/agenda da oficina");
            io.println("4) Acompanhar pedidos e triagem (atribuir voluntário)");
            io.println("5) Relatórios (histórico/impacto ambiental)");
            io.println("0) Sair da conta");

            int op = io.lerInteiro("Escolha: ");
            switch (op) {
                case 1 -> cadastrarOuEditarOficina();
                case 2 -> listarVoluntariosAfiliados();
                case 3 -> configurarHorariosOficina();
                case 4 -> triagem();
                case 5 -> relatorios();
                case 0 -> { loop = false; io.println("Saindo da conta..."); }
                default -> io.println("Opção inválida.");
            }
        }
    }

    private void cadastrarOuEditarOficina() {
        String nome = io.lerLinha("Nome da oficina: ").trim();
        String endereco = io.lerLinha("Endereço: ").trim();
        String cidade = io.lerLinha("Cidade (opcional): ").trim();
        String uf = io.lerLinha("UF (opcional, ex: SP): ").trim().toUpperCase();
            if (uf.isEmpty()) uf = null;
        String tel = io.lerLinha("Telefone (opcional): ").trim();
            if (tel.isEmpty()) tel = null;
        Integer cap = null;
        String sCap = io.lerLinha("Capacidade por hora (1-50) [vazio = manter]: ").trim();
            if (!sCap.isEmpty()) {
                try { cap = Integer.parseInt(sCap); }
                catch (NumberFormatException e) { io.println("Valor inválido; mantendo configuração atual."); cap = null; }
    }
        try {
            Oficina of = oficinaService.criarOuAtualizar(session.getUsuarioAtual(), nome, endereco, cidade, uf, tel, cap);
            io.println("Oficina salva: [" + of.getId() + "] " + of.getNome() + " | Capacidade/Hora: " + of.getCapacidadePorHora());
    }   catch (Exception e) { io.println("Erro: " + e.getMessage()); }
    }

    private void listarVoluntariosAfiliados() {
        Oficina minha = oficinaService.buscarPorDono(session.getUsuarioAtual());
        if (minha == null) {
            io.println("Cadastre sua oficina primeiro.");
            return;
        }
        List<VoluntarioPerfil> lista = oficinaService.listarVoluntariosAfiliados(minha.getId());
        if (lista.isEmpty()) {
            io.println("Nenhum voluntário afiliado ainda.");
            return;
        }
        io.println("Voluntários afiliados:");
        for (VoluntarioPerfil v : lista) {
            io.println("- [" + v.getId() + "] " + v.getUsuario().getNome() + " <" + v.getUsuario().getEmail() + "> | Esp: " +
                    v.getEspecialidade() + (v.getIdade() != null ? " | Idade: " + v.getIdade() : ""));
        }
    }

    private void triagem() {
        Oficina minha = oficinaService.buscarPorDono(session.getUsuarioAtual());
        if (minha == null) {
            io.println("Cadastre sua oficina primeiro.");
            return;
        }
        var ags = agendamentoService.listarPorOficina(minha.getId());
        if (ags.isEmpty()) {
            io.println("Não há agendamentos para sua oficina.");
            return;
        }

        io.println("Agendamentos:");
        for (Agendamento a : ags) {
            String vol = (a.getVoluntarioAtribuido() == null ? "(sem voluntário)" :
                    a.getVoluntarioAtribuido().getUsuario().getNome());
            io.println("ID=" + a.getId() + " | " + a.getDataHora() + " | Item[" + a.getItem().getId() + "]: " +
                    a.getItem().getCategoria() + " - " + a.getItem().getDescricao() + " | Status: " + a.getStatus() +
                    " | Voluntário: " + vol);
        }

        if (!io.lerConfirmacao("Deseja atribuir um voluntário a um agendamento agora?")) return;

        long agId;
        try {
            agId = Long.parseLong(io.lerLinha("Informe o ID do agendamento: ").trim());
        } catch (NumberFormatException e) {
            io.println("ID inválido.");
            return;
        }

        var vols = oficinaService.listarVoluntariosAfiliados(minha.getId());
        if (vols.isEmpty()) {
            io.println("Nenhum voluntário afiliado.");
            return;
        }
        io.println("Voluntários afiliados:");
        for (VoluntarioPerfil v : vols) {
            io.println("[" + v.getId() + "] " + v.getUsuario().getNome() + " | " + v.getEspecialidade());
        }

        long volId;
        try {
            volId = Long.parseLong(io.lerLinha("ID do voluntário: ").trim());
        } catch (NumberFormatException e) {
            io.println("ID inválido.");
            return;
        }

        try {
            var ag = agendamentoService.atribuirVoluntario(agId, volId);
            io.println("Atribuído com sucesso. Novo status: " + ag.getStatus());
        } catch (Exception e) {
            io.println("Erro: " + e.getMessage());
        }

        if (io.lerConfirmacao("Deseja marcar algum agendamento como CANCELADO?")) {
            try {
                long id = Long.parseLong(io.lerLinha("ID do agendamento: ").trim());
                var ag = agendamentoService.atualizarStatus(id, StatusReparo.CANCELADO);
                io.println("Atualizado para CANCELADO. ID=" + ag.getId());
            } catch (Exception e) {
                io.println("Erro: " + e.getMessage());
            }
        }
    }

    private void relatorios() {
        Oficina minha = oficinaService.buscarPorDono(session.getUsuarioAtual());
        if (minha == null) {
            io.println("Cadastre sua oficina primeiro.");
            return;
        }
        io.println("\n=== Relatórios ===");
        io.println("1) Impacto ambiental por período");
        io.println("0) Voltar");
        int op = io.lerInteiro("Escolha: ");
        switch (op) {
            case 1 -> relatorioImpactoAmbiental(minha.getId());
            case 0 -> { /* volta */ }
            default -> io.println("Opção inválida.");
        }
    }

    private void relatorioImpactoAmbiental(Long oficinaId) {
        String sInicio = io.lerLinha("Data início (AAAA-MM-DD) [vazio = últimos 30 dias]: ").trim();
        String sFim = io.lerLinha("Data fim (AAAA-MM-DD)   [vazio = hoje]: ").trim();

        LocalDate fim = sFim.isEmpty() ? LocalDate.now() : parseData(sFim);
        LocalDate inicio = sInicio.isEmpty() ? fim.minusDays(30) : parseData(sInicio);
        if (inicio == null || fim == null) {
            io.println("Datas inválidas.");
            return;
        }
        if (inicio.isAfter(fim)) {
            io.println("Data início não pode ser após a data fim.");
            return;
        }

        var rel = relatorioService.gerarImpactoAmbiental(oficinaId, inicio, fim);

        io.println("\nRelatório de Impacto Ambiental");
        io.println("Oficina ID: " + rel.oficinaId());
        io.println("Período: " + rel.inicio() + " a " + rel.fim());
        io.println("---------------------------------------------");
        if (rel.linhas().isEmpty()) {
            io.println("Nenhum reparo CONCLUÍDO no período.");
            return;
        }
        rel.linhas().forEach(l -> {
            io.println("- " + l.categoria() +
                    ": " + l.quantidadeConcluidos() + " itens concluídos  |  Fator " +
                    String.format("%.1f", l.fatorKgPorItem()) + " kg/item  |  " +
                    String.format("%.1f", l.kgEvitados()) + " kg CO2eq evitados");
        });
        io.println("---------------------------------------------");
        io.println("TOTAL: " + rel.totalItens() + " itens  |  " + String.format("%.1f", rel.totalKg()) + " kg CO2eq evitados");
    }

    private LocalDate parseData(String s) {
        try { return LocalDate.parse(s); }
        catch (Exception e) { return null; }
    }private void configurarHorariosOficina() {
        Oficina minha = oficinaService.buscarPorDono(session.getUsuarioAtual());
        if (minha == null) { io.println("Cadastre sua oficina primeiro."); return; }

        boolean in = true;
        while (in) {
            io.println("\n--- Horários de Funcionamento ---");
            var horarios = disponibilidadeService.listarHorariosOficina(minha.getId());
            if (horarios.isEmpty()) io.println("(Nenhum horário configurado)");
            else {
                horarios.forEach(h ->
                        io.println(h.getDiaSemana() + " - " + h.getHoraInicio() + " às " + h.getHoraFim() +
                                (h.isAtivo() ? "" : " [inativo]")));
            }
            io.println("1) Definir/atualizar horário de um dia");
            io.println("2) Desativar um dia");
            io.println("0) Voltar");
            int op = io.lerInteiro("Escolha: ");
            switch (op) {
                case 1 -> definirHorarioDia();
                case 2 -> desativarDia();
                case 0 -> in = false;
                default -> io.println("Opção inválida.");
            }
        }
    }

    private void definirHorarioDia() {
        DayOfWeek dia = lerDiaSemana();
        if (dia == null) return;
        LocalTime ini = lerHora("Hora início (HH:MM): ");
        LocalTime fim = lerHora("Hora fim (HH:MM): ");
        if (ini == null || fim == null) { io.println("Horário inválido."); return; }
        try {
            var oh = disponibilidadeService.definirHorarioOficina(session.getUsuarioAtual(), dia, ini, fim);
            io.println("Salvo: " + oh.getDiaSemana() + " " + oh.getHoraInicio() + "–" + oh.getHoraFim());
        } catch (Exception e) {
            io.println("Erro: " + e.getMessage());
        }
    }

    private void desativarDia() {
        DayOfWeek dia = lerDiaSemana();
        if (dia == null) return;
        try {
            disponibilidadeService.desativarHorarioOficina(session.getUsuarioAtual(), dia);
            io.println("Dia desativado: " + dia);
        } catch (Exception e) {
            io.println("Erro: " + e.getMessage());
        }
    }

    private DayOfWeek lerDiaSemana() {
        io.println("Dia da semana:");
        io.println("1) SEG  2) TER  3) QUA  4) QUI  5) SEX  6) SAB  7) DOM");
        int n = io.lerInteiro("Escolha: ");
        if (n < 1 || n > 7) { io.println("Opção inválida."); return null; }
        // Java DayOfWeek: MONDAY=1 ... SUNDAY=7
        return DayOfWeek.of(n);
    }

    private LocalTime lerHora(String prompt) {
        String s = io.lerLinha(prompt).trim();
        try { return LocalTime.parse(s + (s.length()==5?":00":"")); } // aceita HH:MM
        catch (Exception e) { return null; }
    }
}
