package br.com.oficinas.gestaooficinas.fx;

import br.com.oficinas.gestaooficinas.domain.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UserSessionFx {
    private Usuario usuario;

    public void setUsuario(Usuario u) { this.usuario = u; }
    public Usuario getUsuario() { return usuario; }
    public boolean isLogged() { return usuario != null; }
    public void clear() { usuario = null; }
}
