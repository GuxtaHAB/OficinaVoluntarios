package br.com.oficinas.gestaooficinas.repository;

import br.com.oficinas.gestaooficinas.domain.ItemReparo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemReparoRepository extends JpaRepository<ItemReparo, Long> {
    List<ItemReparo> findByCliente_Id(Long clienteId);
}
