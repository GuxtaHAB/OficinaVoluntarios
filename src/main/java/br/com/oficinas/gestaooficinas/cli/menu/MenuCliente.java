package br.com.oficinas.gestaooficinas.cli.menu;

import br.com.oficinas.gestaooficinas.core.IoUtils;
import br.com.oficinas.gestaooficinas.domain.*;
import br.com.oficinas.gestaooficinas.service.ClienteService;
import br.com.oficinas.gestaooficinas.service.OficinaService;
import br.com.oficinas.gestaooficinas.service.SessionService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MenuCliente {
    private final IoUtils io;
    private final SessionService session;
    private final ClienteService clienteService;
    private final OficinaService oficinaService;

    public MenuCliente(IoUtils io, SessionService session, ClienteService clienteService, OficinaService oficinaService) {
        this.io = io;
        this.session = session;
        this.clienteService = clienteService;
        this.oficinaService = oficinaService;
    }

    public void abrir() {
        boolean loop = true;
        while (loop) {
            io.println("\n===== Área do Cliente =====");
            io.println("1) Cadastrar item para reparo");
            io.println("2) Agendar horário para um item");
            io.println("3) Ver meus itens e status/agendamentos");
            io.println("4) Cancelar agendamento");
            io.println("0) Sair da conta");
            int op = io.lerInteiro("Escolha: ");
            switch (op) {
                case 1 -> cadastrarItem();
                case 2 -> agendar();
                case 3 -> listarStatus();
                case 4 -> cancelar();
                case 0 -> { loop = false; io.println("Saindo da conta..."); }
                default -> io.println("Opção inválida.");
            }
        }
    }

    private void cadastrarItem() {
        io.println("Categorias:");
        for (int i = 0; i < CategoriaItem.values().length; i++) {
            io.println((i+1) + ") " + CategoriaItem.values()[i]);
        }
        int sel = io.lerInteiro("Escolha a categoria: ");
        if (sel < 1 || sel > CategoriaItem.values().length) { io.println("Opção inválida."); return; }
        CategoriaItem cat = CategoriaItem.values()[sel-1];
        String desc = io.lerLinha("Descrição do problema: ").trim();
        if (desc.isEmpty()) { io.println("Descrição obrigatória."); return; }

        try {
            ItemReparo item = clienteService.criarItem(session.getUsuarioAtual(), cat, desc);
            io.println("Item criado: [" + item.getId() + "] " + item.getCategoria() + " - " + item.getDescricao());
        } catch (Exception e) {
            io.println("Erro: " + e.getMessage());
        }
    }

    private void agendar() {
        List<ItemReparo> itens = clienteService.listarItensDoCliente(session.getUsuarioAtual().getId());
        if (itens.isEmpty()) { io.println("Você ainda não possui itens. Cadastre um primeiro."); return; }
        io.println("Seus itens:");
        for (ItemReparo it : itens) {
            io.println("[" + it.getId() + "] " + it.getCategoria() + " - " + it.getDescricao());
        }
        long itemId;
        try { itemId = Long.parseLong(io.lerLinha("ID do item a agendar: ").trim()); }
        catch (NumberFormatException e) { io.println("ID inválido."); return; }

        var oficinas = oficinaService.listarTodas();
        if (oficinas.isEmpty()) { io.println("Não há oficinas cadastradas no sistema."); return; }
        io.println("Oficinas:");
        for (Oficina o : oficinas) {
            io.println("[" + o.getId() + "] " + o.getNome() + " - " + o.getCidade() + "/" + (o.getUf()==null?"":o.getUf()));
        }
        long oficinaId;
        try { oficinaId = Long.parseLong(io.lerLinha("ID da oficina: ").trim()); }
        catch (NumberFormatException e) { io.println("ID inválido."); return; }

        String data = io.lerLinha("Data (AAAA-MM-DD): ").trim();
        String hora = io.lerLinha("Hora (HH:MM): ").trim();
        LocalDateTime dt;
        try { dt = LocalDateTime.parse(data + "T" + hora + ":00"); }
        catch (Exception e) { io.println("Data/hora inválidas."); return; }

        String obs = io.lerLinha("Observações (opcional): ").trim();
        if (obs.isEmpty()) obs = null;

        try {
            Agendamento ag = clienteService.agendar(session.getUsuarioAtual(), itemId, oficinaId, dt, obs);
            io.println("Agendado! ID=" + ag.getId() + " | " + ag.getDataHora() + " | Status: " + ag.getStatus());
        } catch (Exception e) {
            io.println("Erro: " + e.getMessage());
        }
    }

    private void listarStatus() {
        var itens = clienteService.listarItensDoCliente(session.getUsuarioAtual().getId());
        if (itens.isEmpty()) { io.println("Você não possui itens cadastrados."); return; }
        io.println("Seus itens:");
        for (ItemReparo it : itens) {
            io.println("[" + it.getId() + "] " + it.getCategoria() + " - " + it.getDescricao());
        }
        var ags = clienteService.listarAgendamentosDoCliente(session.getUsuarioAtual().getId());
        if (ags.isEmpty()) { io.println("Nenhum agendamento encontrado."); return; }
        io.println("Seus agendamentos:");
        for (Agendamento a : ags) {
            String vol = (a.getVoluntarioAtribuido()==null?"(sem voluntário)":a.getVoluntarioAtribuido().getUsuario().getNome());
            io.println("- ID="+a.getId()+" | "+a.getDataHora()+" | Oficina: "+a.getOficina().getNome()+
                    " | Status: "+a.getStatus()+" | Voluntário: "+vol);
        }
    }
    private void cancelar() {
        var ags = clienteService.listarAgendamentosDoCliente(session.getUsuarioAtual().getId());
        if (ags.isEmpty()) { io.println("Você não possui agendamentos."); return; }
            for (Agendamento a : ags) io.println("ID="+a.getId()+" | "+a.getDataHora()+" | Status: "+a.getStatus());
            long id;
            try { id = Long.parseLong(io.lerLinha("Informe o ID do agendamento para cancelar: ").trim()); }
            catch (NumberFormatException e) { io.println("ID inválido."); return; }
            try {
                var ag = clienteService.cancelarAgendamento(session.getUsuarioAtual(), id);
                io.println("Cancelado com sucesso. Novo status: " + ag.getStatus());
        }   catch (Exception e) { io.println("Erro: " + e.getMessage()); }
    }   
}
