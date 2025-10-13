package br.com.oficinas.gestaooficinas.repository;

import br.com.oficinas.gestaooficinas.domain.Oficina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OficinaRepository extends JpaRepository<Oficina, Long> {
    Optional<Oficina> findByDono_Id(Long donoId);
    boolean existsByDono_Id(Long donoId);
    List<Oficina> findByCidadeIgnoreCase(String cidade);
}
