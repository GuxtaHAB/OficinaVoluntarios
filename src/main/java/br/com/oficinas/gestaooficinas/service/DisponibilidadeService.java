package br.com.oficinas.gestaooficinas.service;

import br.com.oficinas.gestaooficinas.domain.*;
import br.com.oficinas.gestaooficinas.repository.OficinaHorarioRepository;
import br.com.oficinas.gestaooficinas.repository.VoluntarioDisponibilidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class DisponibilidadeService {

    private final OficinaService oficinaService;
    private final VoluntarioService voluntarioService;
    private final OficinaHorarioRepository oficinaHorarioRepo;
    private final VoluntarioDisponibilidadeRepository voluntarioDispRepo;

    public DisponibilidadeService(OficinaService oficinaService,
                                  VoluntarioService voluntarioService,
                                  OficinaHorarioRepository oficinaHorarioRepo,
                                  VoluntarioDisponibilidadeRepository voluntarioDispRepo) {
        this.oficinaService = oficinaService;
        this.voluntarioService = voluntarioService;
        this.oficinaHorarioRepo = oficinaHorarioRepo;
        this.voluntarioDispRepo = voluntarioDispRepo;
    }

    // ---------- Oficina ----------
    @Transactional
    public OficinaHorario definirHorarioOficina(Usuario dono, DayOfWeek dia, LocalTime inicio, LocalTime fim) {
        Oficina oficina = oficinaService.buscarPorDono(dono);
        if (oficina == null) throw new IllegalStateException("Dono não possui oficina cadastrada.");
        if (!inicio.isBefore(fim)) throw new IllegalArgumentException("Hora início deve ser antes da hora fim.");

        OficinaHorario oh = oficinaHorarioRepo.findByOficina_IdAndDiaSemana(oficina.getId(), dia)
                .orElse(new OficinaHorario(oficina, dia, inicio, fim));
        oh.setHoraInicio(inicio);
        oh.setHoraFim(fim);
        oh.setAtivo(true);
        return oficinaHorarioRepo.save(oh);
    }

    @Transactional(readOnly = true)
    public List<OficinaHorario> listarHorariosOficina(Long oficinaId) {
        return oficinaHorarioRepo.findByOficina_IdOrderByDiaSemanaAsc(oficinaId);
    }

    @Transactional
    public void desativarHorarioOficina(Usuario dono, DayOfWeek dia) {
    Oficina oficina = oficinaService.buscarPorDono(dono);
    if (oficina == null) throw new IllegalStateException("Dono não possui oficina cadastrada.");
    // 🗑️ agora removemos de vez:
    oficinaHorarioRepo.deleteByOficina_IdAndDiaSemana(oficina.getId(), dia);
}

    @Transactional(readOnly = true)
    public boolean oficinaAberta(Long oficinaId, LocalDateTime dataHora) {
        DayOfWeek dia = dataHora.getDayOfWeek();
        LocalTime hora = dataHora.toLocalTime();
        return oficinaHorarioRepo.findByOficina_IdAndDiaSemana(oficinaId, dia)
                .filter(OficinaHorario::isAtivo)
                .filter(oh -> !hora.isBefore(oh.getHoraInicio()) && hora.isBefore(oh.getHoraFim()))
                .isPresent();
    }

    // ---------- Voluntário ----------
    @Transactional
    public VoluntarioDisponibilidade definirDisponibilidadeVoluntario(Usuario usuario,
                                                                      DayOfWeek dia, LocalTime inicio, LocalTime fim) {
        VoluntarioPerfil perfil = voluntarioService.obterPerfil(usuario);
        if (perfil == null) throw new IllegalStateException("Crie seu perfil de voluntário primeiro.");
        if (!inicio.isBefore(fim)) throw new IllegalArgumentException("Hora início deve ser antes da hora fim.");

        // permite múltiplos intervalos por dia (não impomos unicidade)
        VoluntarioDisponibilidade d = new VoluntarioDisponibilidade(perfil, dia, inicio, fim);
        d.setAtivo(true);
        return voluntarioDispRepo.save(d);
    }

    @Transactional(readOnly = true)
    public List<VoluntarioDisponibilidade> listarDisponibilidades(Long voluntarioPerfilId) {
        return voluntarioDispRepo.findByVoluntario_IdOrderByDiaSemanaAsc(voluntarioPerfilId);
    }
    @Transactional
        public void removerDisponibilidade(Long dispId) {
            voluntarioDispRepo.deleteById(dispId);
    }

    @Transactional(readOnly = true)
    public boolean voluntarioDisponivel(Long voluntarioPerfilId, LocalDateTime dataHora) {
        DayOfWeek dia = dataHora.getDayOfWeek();
        LocalTime hora = dataHora.toLocalTime();
        return voluntarioDispRepo.findByVoluntario_IdAndDiaSemana(voluntarioPerfilId, dia).stream()
                .filter(VoluntarioDisponibilidade::isAtivo)
                .anyMatch(d -> !hora.isBefore(d.getHoraInicio()) && hora.isBefore(d.getHoraFim()));
    }
}
