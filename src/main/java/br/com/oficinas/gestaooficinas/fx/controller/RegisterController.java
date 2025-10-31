package br.com.oficinas.gestaooficinas.fx.controller;

import br.com.oficinas.gestaooficinas.domain.Role;
import br.com.oficinas.gestaooficinas.fx.SceneRouter;
import br.com.oficinas.gestaooficinas.service.AuthService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class RegisterController {

    @FXML private TextField nomeField;
    @FXML private TextField emailField;

    @FXML private PasswordField senhaField, confirmaSenhaField;
    @FXML private TextField senhaVisivelField, confirmaSenhaVisivelField;
    @FXML private Button toggleSenhaBtn, toggleConfirmaBtn;

    @FXML private ComboBox<Role> roleBox;
    @FXML private Label msgLabel;
    @FXML private Button criarButton;

    private final AuthService authService;
    private final SceneRouter router;

    // === MODELO CENTRAL (evita loops e travamentos) ===
    private final StringProperty senhaModel   = new SimpleStringProperty("");
    private final StringProperty confirmaModel = new SimpleStringProperty("");

    public RegisterController(AuthService authService, SceneRouter router) {
        this.authService = authService;
        this.router = router;
    }

    @FXML
    public void initialize() {
        msgLabel.setText("");

        if (roleBox.getItems().isEmpty()) {
            roleBox.getItems().addAll(Role.values());
        }

        // --- Bind cada controle AO MODELO (NÃO controle↔controle) ---
        senhaField.textProperty().bindBidirectional(senhaModel);
        senhaVisivelField.textProperty().bindBidirectional(senhaModel);

        confirmaSenhaField.textProperty().bindBidirectional(confirmaModel);
        confirmaSenhaVisivelField.textProperty().bindBidirectional(confirmaModel);

        // Feedback visual em tempo real na confirmação
        confirmaModel.addListener((obs, oldV, newV) -> {
            boolean ok = senhaModel.get() != null && senhaModel.get().equals(newV);
            confirmaSenhaField.setStyle(ok ? "" : "-fx-background-color: #ffebee;");
            confirmaSenhaVisivelField.setStyle(ok ? "" : "-fx-background-color: #ffebee;");
        });
    }

    // =======================
    // TOGGLES 👁️
    // =======================
    @FXML
    private void toggleSenhaVisivel() {
        boolean mostrar = !senhaVisivelField.isVisible();
        senhaVisivelField.setVisible(mostrar);
        senhaVisivelField.setManaged(mostrar);
        senhaField.setVisible(!mostrar);
        senhaField.setManaged(!mostrar);
        toggleSenhaBtn.setText(mostrar ? "🙈" : "👁️");
    }

    @FXML
    private void toggleConfirmaVisivel() {
        boolean mostrar = !confirmaSenhaVisivelField.isVisible();
        confirmaSenhaVisivelField.setVisible(mostrar);
        confirmaSenhaVisivelField.setManaged(mostrar);
        confirmaSenhaField.setVisible(!mostrar);
        confirmaSenhaField.setManaged(!mostrar);
        toggleConfirmaBtn.setText(mostrar ? "🙈" : "👁️");
    }

    // =======================
    // CADASTRO
    // =======================
    @FXML
    public void onCreate() {
        try {
            msgLabel.setStyle("-fx-text-fill: -fx-text-base-color;");
            msgLabel.setText("");

            String nome  = safe(nomeField.getText());
            String email = safe(emailField.getText());
            String senha = senhaModel.get();
            String conf  = confirmaModel.get();
            Role role    = roleBox.getValue();

            if (nome.isEmpty())  throw new IllegalArgumentException("Informe o nome.");
            if (email.isEmpty()) throw new IllegalArgumentException("Informe o e-mail.");
            if (role == null)    throw new IllegalArgumentException("Selecione o perfil.");
            if (senha.isEmpty() || conf.isEmpty())
                throw new IllegalArgumentException("Informe a senha e a confirmação.");

            if (!senha.equals(conf))
                throw new IllegalArgumentException("As senhas não coincidem.");

            validarSenha(senha);

            // Use o método que EXISTE no seu AuthService:
            // você usou 'cadastrar' aqui; se o seu service expõe 'registrar', troque.
            authService.cadastrar(nome, email, senha, role);

            msgLabel.setStyle("-fx-text-fill: #2e7d32;");
            msgLabel.setText("Conta criada! Você já pode fazer login.");
            limparCampos();
        } catch (Exception e) {
            msgLabel.setStyle("-fx-text-fill: #c62828;");
            msgLabel.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void onBackToLogin() {
        Stage stage = (Stage) nomeField.getScene().getWindow();
        router.attach(stage);
        router.go("/fxml/login.fxml", "Gestão de Oficinas - Login");
    }

    // ----------------- helpers -----------------
    private static String safe(String s) { return s == null ? "" : s.trim(); }

    private static void validarSenha(String s) {
        if (s.length() < 8) throw new IllegalArgumentException("A senha deve ter pelo menos 8 caracteres.");
        if (!s.matches(".*[a-z].*")) throw new IllegalArgumentException("A senha deve conter ao menos 1 minúscula.");
        if (!s.matches(".*[A-Z].*")) throw new IllegalArgumentException("A senha deve conter ao menos 1 maiúscula.");
        if (!s.matches(".*\\d.*"))   throw new IllegalArgumentException("A senha deve conter ao menos 1 dígito.");
    }

    private void limparCampos() {
        nomeField.clear();
        emailField.clear();
        senhaModel.set("");
        confirmaModel.set("");
        roleBox.getSelectionModel().clearSelection();

        // Reset de visibilidade/managed e ícones
        senhaVisivelField.setVisible(false);
        senhaVisivelField.setManaged(false);
        confirmaSenhaVisivelField.setVisible(false);
        confirmaSenhaVisivelField.setManaged(false);
        senhaField.setVisible(true);
        senhaField.setManaged(true);
        confirmaSenhaField.setVisible(true);
        confirmaSenhaField.setManaged(true);
        toggleSenhaBtn.setText("👁️");
        toggleConfirmaBtn.setText("👁️");

        // limpar estilos
        confirmaSenhaField.setStyle("");
        confirmaSenhaVisivelField.setStyle("");

        nomeField.requestFocus();
    }
}
