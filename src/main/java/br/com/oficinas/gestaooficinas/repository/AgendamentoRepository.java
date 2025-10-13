package br.com.oficinas.gestaooficinas.repository;

import br.com.oficinas.gestaooficinas.domain.Agendamento;
import br.com.oficinas.gestaooficinas.domain.StatusReparo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    List<Agendamento> findByOficina_IdOrderByDataHoraAsc(Long oficinaId);
    List<Agendamento> findByVoluntarioAtribuido_IdOrderByDataHoraAsc(Long voluntarioId);
    List<Agendamento> findByItem_Cliente_IdOrderByDataHoraDesc(Long clienteId);
    boolean existsByItem_IdAndDataHora(Long itemId, LocalDateTime dataHora);
    long countByVoluntarioAtribuido_IdAndDataHoraBetween(Long voluntarioId, LocalDateTime inicio, LocalDateTime fim);
    long countByOficina_IdAndStatusInAndDataHoraBetween(Long oficinaId, List<StatusReparo> status, LocalDateTime inicio, LocalDateTime fim);

    @Query("""
           select a.item.categoria, count(a)
           from Agendamento a
           where a.oficina.id = :oficinaId
             and a.status = br.com.oficinas.gestaooficinas.domain.StatusReparo.CONCLUIDO
             and a.dataHora between :inicio and :fim
           group by a.item.categoria
           """)
    List<Object[]> contagemConcluidosPorCategoria(
            @Param("oficinaId") Long oficinaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}
