package br.com.oficinas.gestaooficinas.repository;

import br.com.oficinas.gestaooficinas.domain.OficinaHorario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface OficinaHorarioRepository extends JpaRepository<OficinaHorario, Long> {
    List<OficinaHorario> findByOficina_IdOrderByDiaSemanaAsc(Long oficinaId);
    Optional<OficinaHorario> findByOficina_IdAndDiaSemana(Long oficinaId, DayOfWeek diaSemana);

    void deleteByOficina_IdAndDiaSemana(Long oficinaId, java.time.DayOfWeek dia);
}
