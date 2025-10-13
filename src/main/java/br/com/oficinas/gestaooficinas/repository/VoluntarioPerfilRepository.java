package br.com.oficinas.gestaooficinas.repository;

import br.com.oficinas.gestaooficinas.domain.VoluntarioPerfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoluntarioPerfilRepository extends JpaRepository<VoluntarioPerfil, Long> {
    Optional<VoluntarioPerfil> findByUsuario_Id(Long usuarioId);
    List<VoluntarioPerfil> findByOficinaAfiliada_Id(Long oficinaId);
}
