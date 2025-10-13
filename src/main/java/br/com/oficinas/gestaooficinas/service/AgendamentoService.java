package br.com.oficinas.gestaooficinas.service;

import br.com.oficinas.gestaooficinas.domain.*;
import br.com.oficinas.gestaooficinas.repository.AgendamentoRepository;
import br.com.oficinas.gestaooficinas.repository.VoluntarioPerfilRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agRepo;
    private final VoluntarioPerfilRepository volRepo;
    private final DisponibilidadeService disponibilidadeService;

    public AgendamentoService(AgendamentoRepository agRepo,
                              VoluntarioPerfilRepository volRepo,
                              DisponibilidadeService disponibilidadeService) {
        this.agRepo = agRepo;
        this.volRepo = volRepo;
        this.disponibilidadeService = disponibilidadeService;
    }

    @Transactional(readOnly = true)
    public List<Agendamento> listarPorOficina(Long oficinaId) {
        return agRepo.findByOficina_IdOrderByDataHoraAsc(oficinaId);
    }

    @Transactional(readOnly = true)
    public List<Agendamento> listarPorVoluntario(Long voluntarioPerfilId) {
        return agRepo.findByVoluntarioAtribuido_IdOrderByDataHoraAsc(voluntarioPerfilId);
    }

    @Transactional
    public Agendamento atribuirVoluntario(Long agendamentoId, Long voluntarioPerfilId) {
        Agendamento ag = agRepo.findById(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));
        VoluntarioPerfil vol = volRepo.findById(voluntarioPerfilId)
                .orElseThrow(() -> new IllegalArgumentException("Voluntário não encontrado."));
        // Mesma oficina
        if (ag.getOficina().getId() == null || vol.getOficinaAfiliada() == null ||
                !ag.getOficina().getId().equals(vol.getOficinaAfiliada().getId())) {
            throw new IllegalArgumentException("Voluntário não está afiliado à mesma oficina do agendamento.");
        }
        // NOVO: disponibilidade do voluntário
        if (!disponibilidadeService.voluntarioDisponivel(vol.getId(), ag.getDataHora())) {
            throw new IllegalArgumentException("Voluntário indisponível nesse dia/horário.");
        }
        // Conflito do voluntário (janela de 1h)
        LocalDateTime inicio = ag.getDataHora().truncatedTo(ChronoUnit.HOURS);
        LocalDateTime fim = inicio.plusHours(1);
        long conflitos = agRepo.countByVoluntarioAtribuido_IdAndDataHoraBetween(vol.getId(), inicio, fim);
        if (conflitos > 0) {
            throw new IllegalArgumentException("Voluntário já possui agendamento no mesmo horário.");
        }

        ag.setVoluntarioAtribuido(vol);
        if (ag.getStatus() == StatusReparo.NA_FILA) {
            ag.setStatus(StatusReparo.PENDENTE);
        }
        return agRepo.save(ag);
    }

    @Transactional
    public Agendamento atualizarStatus(Long agendamentoId, StatusReparo novoStatus) {
        Agendamento ag = agRepo.findById(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));
        ag.setStatus(novoStatus);
        return agRepo.save(ag);
    }
}
