package br.com.oficinas.gestaooficinas.repository;

import br.com.oficinas.gestaooficinas.domain.VoluntarioDisponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface VoluntarioDisponibilidadeRepository extends JpaRepository<VoluntarioDisponibilidade, Long> {
    List<VoluntarioDisponibilidade> findByVoluntario_IdOrderByDiaSemanaAsc(Long voluntarioId);
    List<VoluntarioDisponibilidade> findByVoluntario_IdAndDiaSemana(Long voluntarioId, DayOfWeek diaSemana);
}
