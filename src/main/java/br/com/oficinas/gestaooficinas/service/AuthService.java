package br.com.oficinas.gestaooficinas.service;

import br.com.oficinas.gestaooficinas.domain.Role;
import br.com.oficinas.gestaooficinas.domain.Usuario;
import br.com.oficinas.gestaooficinas.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Usuario cadastrar(String nome, String email, String senhaPlano, Role role) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Já existe usuário com este e-mail.");
        }
        String hash = BCrypt.hashpw(senhaPlano, BCrypt.gensalt(10));
        Usuario u = new Usuario(nome, email.toLowerCase().trim(), hash, role);
        return usuarioRepository.save(u);
    }

    @Transactional(readOnly = true)
    public Usuario login(String email, String senhaPlano) {
        Usuario u = usuarioRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (!BCrypt.checkpw(senhaPlano, u.getSenhaHash())) {
            throw new IllegalArgumentException("Senha inválida.");
        }
        return u;
    }
}
