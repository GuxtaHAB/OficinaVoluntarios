package br.com.oficinas.gestaooficinas.service;

import br.com.oficinas.gestaooficinas.domain.*;
import br.com.oficinas.gestaooficinas.repository.AgendamentoRepository;
import br.com.oficinas.gestaooficinas.repository.ItemReparoRepository;
import br.com.oficinas.gestaooficinas.repository.OficinaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
public class ClienteService {

    private final ItemReparoRepository itemRepo;
    private final OficinaRepository oficinaRepo;
    private final AgendamentoRepository agendamentoRepo;
    private final DisponibilidadeService disponibilidadeService;

    public ClienteService(ItemReparoRepository itemRepo,
                          OficinaRepository oficinaRepo,
                          AgendamentoRepository agendamentoRepo,
                          DisponibilidadeService disponibilidadeService) {
        this.itemRepo = itemRepo;
        this.oficinaRepo = oficinaRepo;
        this.agendamentoRepo = agendamentoRepo;
        this.disponibilidadeService = disponibilidadeService;
    }

    @Transactional
    public ItemReparo criarItem(Usuario cliente, CategoriaItem categoria, String descricao) {
        if (cliente.getRole() != Role.CLIENTE) {
            throw new IllegalArgumentException("Apenas CLIENTE pode criar item de reparo.");
        }
        ItemReparo item = new ItemReparo(cliente, categoria, descricao);
        return itemRepo.save(item);
    }

    @Transactional
    public Agendamento agendar(Usuario cliente, Long itemId, Long oficinaId, LocalDateTime dataHora, String obs) {
        ItemReparo item = itemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado."));
        if (!item.getCliente().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("Você só pode agendar itens seus.");
        }
        Oficina oficina = oficinaRepo.findById(oficinaId)
                .orElseThrow(() -> new IllegalArgumentException("Oficina não encontrada."));


        if (!disponibilidadeService.oficinaAberta(oficina.getId(), dataHora)) {
            throw new IllegalArgumentException("Oficina fechada nesse dia/horário.");
        }


        if (agendamentoRepo.existsByItem_IdAndDataHora(item.getId(), dataHora)) {
            throw new IllegalArgumentException("Este item já possui agendamento no mesmo horário.");
        }

        LocalDateTime inicio = dataHora.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime fim = inicio.plusHours(1);
        long ocupacao = agendamentoRepo.countByOficina_IdAndStatusInAndDataHoraBetween(
                oficina.getId(),
                Arrays.asList(StatusReparo.NA_FILA, StatusReparo.PENDENTE),
                inicio, fim
        );
        int cap = oficina.getCapacidadePorHora() == null ? 3 : oficina.getCapacidadePorHora();
        if (ocupacao >= cap) {
            throw new IllegalArgumentException("Capacidade esgotada para este horário. Tente outro horário.");
        }

        Agendamento ag = new Agendamento(item, oficina, dataHora);
        ag.setStatus(StatusReparo.NA_FILA);
        ag.setObservacoes(obs);
        return agendamentoRepo.save(ag);
    }

    @Transactional(readOnly = true)
    public List<ItemReparo> listarItensDoCliente(Long clienteId) {
        return itemRepo.findByCliente_Id(clienteId);
    }

    @Transactional(readOnly = true)
    public List<Agendamento> listarAgendamentosDoCliente(Long clienteId) {
        return agendamentoRepo.findByItem_Cliente_IdOrderByDataHoraDesc(clienteId);
    }

    @Transactional
    public Agendamento cancelarAgendamento(Usuario cliente, Long agendamentoId) {
        Agendamento ag = agendamentoRepo.findById(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));
        if (!ag.getItem().getCliente().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("Você só pode cancelar seus próprios agendamentos.");
        }
        if (ag.getStatus() == StatusReparo.CONCLUIDO) {
            throw new IllegalArgumentException("Não é possível cancelar um reparo concluído.");
        }
        // 24h de antecedência
        if (ag.getDataHora().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new IllegalArgumentException("Cancelamento permitido apenas com 24h de antecedência.");
        }
        ag.setStatus(StatusReparo.CANCELADO);
        return agendamentoRepo.save(ag);
    }
}
