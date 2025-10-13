package br.com.oficinas.gestaooficinas.fx.controller;

import br.com.oficinas.gestaooficinas.domain.Role;
import br.com.oficinas.gestaooficinas.fx.SceneRouter;
import br.com.oficinas.gestaooficinas.service.AuthService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class RegisterController {

    @FXML private TextField nomeField;
    @FXML private TextField emailField;
    @FXML private PasswordField senhaField;
    @FXML private ComboBox<Role> roleBox;
    @FXML private Label msgLabel;
    @FXML private Button criarButton;

    private final AuthService authService;
    private final SceneRouter router;

    public RegisterController(AuthService authService, SceneRouter router) {
        this.authService = authService;
        this.router = router;
    }

    @FXML
    public void initialize() {
        roleBox.setItems(FXCollections.observableArrayList(Role.values()));
        if (msgLabel != null) msgLabel.setText("");
        if (criarButton != null) criarButton.setDefaultButton(true);
    }

    @FXML
    public void onCreate() {
        try {
            String nome = safe(nomeField.getText());
            String email = safe(emailField.getText());
            String senha = safe(senhaField.getText());
            Role role = roleBox.getValue();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || role == null) {
                setMsg("Preencha todos os campos e selecione um papel.", true);
                return;
            }

            if (senha.length() < 8) {
                setMsg("A senha deve ter pelo menos 8 caracteres.", true);
                return;
            }

            authService.cadastrar(nome, email, senha, role);
            setMsg("Conta criada com sucesso!", false);
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION,"Cadastrado com sucesso!").showAndWait();

            Stage stage = (Stage) nomeField.getScene().getWindow();
            router.attach(stage);
            router.go("/fxml/login.fxml", "Gestão de Oficinas - Login");

        } catch (Exception e) {
            setMsg("Erro: " + e.getMessage(), true);
        }
    }

    @FXML
    public void onBackToLogin() {
        Stage stage = (Stage) nomeField.getScene().getWindow();
        router.attach(stage);
        router.go("/fxml/login.fxml", "Gestão de Oficinas - Login");
    }

    // Utils
    private String safe(String s) { return s == null ? "" : s.trim(); }

    private void setMsg(String text, boolean error) {
        if (msgLabel != null) {
            msgLabel.setText(text);
            msgLabel.setStyle(error ? "-fx-text-fill: #d32f2f;" : "-fx-text-fill: #2e7d32;");
        }
    }
}
