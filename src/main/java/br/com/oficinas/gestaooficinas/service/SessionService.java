package br.com.oficinas.gestaooficinas.service;

import br.com.oficinas.gestaooficinas.domain.Usuario;
import org.springframework.stereotype.Component;

/**
 * Sessão simples em memória para o CLI (uma instância por execução).
 * Em apps web, isso seria stateless/JWT ou sessão HTTP.
 */
@Component
public class SessionService {
    private Usuario usuarioAtual;

    public void login(Usuario u) { this.usuarioAtual = u; }
    public void logout() { this.usuarioAtual = null; }
    public Usuario getUsuarioAtual() { return usuarioAtual; }
    public boolean isAutenticado() { return usuarioAtual != null; }
}
