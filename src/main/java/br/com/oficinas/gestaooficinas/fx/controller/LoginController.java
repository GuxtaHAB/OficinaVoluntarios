package br.com.oficinas.gestaooficinas.fx.controller;

import br.com.oficinas.gestaooficinas.domain.Role;
import br.com.oficinas.gestaooficinas.domain.Usuario;
import br.com.oficinas.gestaooficinas.fx.SceneRouter;
import br.com.oficinas.gestaooficinas.fx.UserSessionFx;
import br.com.oficinas.gestaooficinas.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField senhaField;
    @FXML private Label errorLabel;
    @FXML private Button entrarButton; // opcional (para defaultButton no FXML)

    private final AuthService auth;
    private final UserSessionFx session;
    private final SceneRouter router;

    public LoginController(AuthService auth, UserSessionFx session, SceneRouter router) {
        this.auth = auth;
        this.session = session;
        this.router = router;
    }

    @FXML
    public void initialize() {
        // limpa mensagem de erro ao abrir
        if (errorLabel != null) errorLabel.setText("");

        // opcional: aperto de Enter no botão "Entrar" se no FXML estiver com defaultButton="true"
        if (entrarButton != null) {
            entrarButton.setDefaultButton(true);
        }
    }

    @FXML
    public void onLogin() {
        try {
            String email = emailField.getText() == null ? "" : emailField.getText().trim();
            String senha = senhaField.getText() == null ? "" : senhaField.getText().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                setError("Informe e-mail e senha.");
                return;
            }

            Usuario u = auth.login(email, senha);
            session.setUsuario(u);

            // garante que o router conhece o Stage atual
            Stage stage = (Stage) emailField.getScene().getWindow();
            router.attach(stage);

            if (u.getRole() == Role.DONO) {
                router.go("/fxml/dono.fxml", "Área do Dono");
            } else if (u.getRole() == Role.VOLUNTARIO) {
                router.go("/fxml/voluntario.fxml", "Área do Voluntário");
            } else {
                router.go("/fxml/cliente.fxml", "Área do Cliente");
            }
        } catch (Exception e) {
            setError("Erro: " + e.getMessage());
            senhaField.clear();
            senhaField.requestFocus();
        }
    }

    @FXML
    public void onGoToRegister() {
        // garante que o router conhece o Stage atual antes de navegar
        Stage stage = (Stage) emailField.getScene().getWindow();
        router.attach(stage);
        router.go("/fxml/register.fxml", "Criar Conta");
    }

    // --- utilitários ---

    private void setError(String msg) {
        if (errorLabel != null) errorLabel.setText(msg);
    }

    @FXML
    public void onClearError() {
        // se quiser ligar a algum evento no FXML (ex.: onKeyTyped), limpa a mensagem
        setError("");
    }
}
