package br.com.oficinas.gestaooficinas.service;

import br.com.oficinas.gestaooficinas.domain.Oficina;
import br.com.oficinas.gestaooficinas.domain.Role;
import br.com.oficinas.gestaooficinas.domain.Usuario;
import br.com.oficinas.gestaooficinas.domain.VoluntarioPerfil;
import br.com.oficinas.gestaooficinas.repository.OficinaRepository;
import br.com.oficinas.gestaooficinas.repository.VoluntarioPerfilRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OficinaService {

    private final OficinaRepository oficinaRepository;
    private final VoluntarioPerfilRepository voluntarioPerfilRepository;

    public OficinaService(OficinaRepository oficinaRepository, VoluntarioPerfilRepository voluntarioPerfilRepository) {
        this.oficinaRepository = oficinaRepository;
        this.voluntarioPerfilRepository = voluntarioPerfilRepository;
    }

    @Transactional
public Oficina criarOuAtualizar(Usuario dono, String nome, String endereco, String cidade, String uf, String telefone) {
    return criarOuAtualizar(dono, nome, endereco, cidade, uf, telefone, null);
}

@Transactional
public Oficina criarOuAtualizar(Usuario dono, String nome, String endereco, String cidade, String uf,
                                String telefone, Integer capacidadePorHora) {
    if (dono.getRole() != Role.DONO) {
        throw new IllegalArgumentException("Apenas usuários com perfil DONO podem gerenciar oficinas.");
    }
    Oficina of = oficinaRepository.findByDono_Id(dono.getId())
            .orElse(new Oficina(nome, endereco, cidade, uf, telefone, dono));

    of.setNome(nome);
    of.setEndereco(endereco);
    of.setCidade(cidade);
    of.setUf(uf);
    of.setTelefone(telefone);
    if (capacidadePorHora != null) {
        if (capacidadePorHora < 1 || capacidadePorHora > 50)
            throw new IllegalArgumentException("Capacidade por hora deve estar entre 1 e 50.");
        of.setCapacidadePorHora(capacidadePorHora);
    }
    return oficinaRepository.save(of);
}

    @Transactional(readOnly = true)
    public Oficina buscarPorDono(Usuario dono) {
        return oficinaRepository.findByDono_Id(dono.getId())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<VoluntarioPerfil> listarVoluntariosAfiliados(Long oficinaId) {
        return voluntarioPerfilRepository.findByOficinaAfiliada_Id(oficinaId);
    }

    @Transactional(readOnly = true)
    public List<Oficina> listarTodas() {
        return oficinaRepository.findAll();
    }
    
}
