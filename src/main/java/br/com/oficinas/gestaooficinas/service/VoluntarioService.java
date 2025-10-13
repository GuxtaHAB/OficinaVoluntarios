package br.com.oficinas.gestaooficinas.service;

import br.com.oficinas.gestaooficinas.domain.*;
import br.com.oficinas.gestaooficinas.repository.OficinaRepository;
import br.com.oficinas.gestaooficinas.repository.VoluntarioPerfilRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoluntarioService {

    private final VoluntarioPerfilRepository voluntarioRepo;
    private final OficinaRepository oficinaRepository;

    public VoluntarioService(VoluntarioPerfilRepository voluntarioRepo, OficinaRepository oficinaRepository) {
        this.voluntarioRepo = voluntarioRepo;
        this.oficinaRepository = oficinaRepository;
    }

    @Transactional
    public VoluntarioPerfil criarOuAtualizarPerfil(Usuario usuario, Especialidade esp, Integer idade) {
        if (usuario.getRole() != Role.VOLUNTARIO) {
            throw new IllegalArgumentException("Apenas usuários VOLUNTARIO podem editar perfil de voluntário.");
        }
        VoluntarioPerfil perfil = voluntarioRepo.findByUsuario_Id(usuario.getId())
                .orElse(new VoluntarioPerfil(usuario, esp, idade));

        perfil.setEspecialidade(esp);
        perfil.setIdade(idade);

        return voluntarioRepo.save(perfil);
    }

    @Transactional
    public VoluntarioPerfil afiliar(Usuario usuario, Long oficinaId) {
        VoluntarioPerfil perfil = voluntarioRepo.findByUsuario_Id(usuario.getId())
                .orElseThrow(() -> new IllegalStateException("Complete seu perfil antes de afiliar-se a uma oficina."));
        Oficina oficina = oficinaRepository.findById(oficinaId)
                .orElseThrow(() -> new IllegalArgumentException("Oficina não encontrada."));
        perfil.setOficinaAfiliada(oficina);
        return voluntarioRepo.save(perfil);
    }

    @Transactional
    public VoluntarioPerfil desafiliar(Usuario usuario) {
        VoluntarioPerfil perfil = voluntarioRepo.findByUsuario_Id(usuario.getId())
                .orElseThrow(() -> new IllegalStateException("Perfil de voluntário não encontrado."));
        perfil.setOficinaAfiliada(null);
        return voluntarioRepo.save(perfil);
    }

    @Transactional(readOnly = true)
    public VoluntarioPerfil obterPerfil(Usuario usuario) {
        return voluntarioRepo.findByUsuario_Id(usuario.getId()).orElse(null);
    }
}
