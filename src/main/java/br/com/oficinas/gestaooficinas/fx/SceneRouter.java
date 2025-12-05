package br.com.oficinas.gestaooficinas.fx;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.springframework.stereotype.Component;

@Component
public class SceneRouter {

    private final FxViewLoader loader;
    private Stage stage;

    // controle de tema
    private boolean darkEnabled = false;

    public SceneRouter(FxViewLoader loader) {
        this.loader = loader;
    }

    /** Associa o Stage principal ao router (faça isso no FxLauncher.start) */
    public void attach(Stage stage) {
        this.stage = stage;
        // Se já existir uma cena, garanta que o CSS está aplicado
        if (this.stage.getScene() != null) {
            applyStyles(this.stage.getScene());
        }
    }

    /** Tenta descobrir um Stage visível quando attach ainda não foi chamado */
    private boolean ensureAttached() {
        if (this.stage != null) return true;
        for (Window w : Window.getWindows()) {
            if (w instanceof Stage s && w.isShowing()) {
                this.stage = s;
                return true;
            }
        }
        return false;
    }

    /** Navega para um FXML, reaproveitando a Scene quando possível e aplicando CSS global */
    public void go(String fxmlPath, String title) {
        if (!ensureAttached()) {
            throw new IllegalStateException("Stage não associado ao router.");
        }

        Parent root = loader.load(fxmlPath);
        Scene scene = this.stage.getScene();

        if (scene == null) {
            scene = new Scene(root);
        } else {
            // reaproveita a mesma scene (mantém tamanho, posição, etc.)
            scene.setRoot(root);
        }

        applyStyles(scene);

        if (title != null && !title.isBlank()) {
            this.stage.setTitle(title);
        }

        this.stage.setScene(scene);
        this.stage.centerOnScreen();
        this.stage.show();
    }

    /** Aplica o app.css e, se habilitado, o dark.css; evita duplicatas */
    private void applyStyles(Scene scene) {
        String appCss = resource("/styles/app.css");
        if (appCss != null && !scene.getStylesheets().contains(appCss)) {
            scene.getStylesheets().add(0, appCss);
        }

        String darkCss = resource("/styles/dark.css");
        boolean hasDark = darkCss != null && scene.getStylesheets().contains(darkCss);

        if (darkEnabled && darkCss != null && !hasDark) {
            scene.getStylesheets().add(darkCss);
        } else if (!darkEnabled && hasDark) {
            scene.getStylesheets().remove(darkCss);
        }
    }

    private String resource(String path) {
        var url = getClass().getResource(path);
        return url == null ? null : url.toExternalForm();
    }

    /** Liga/desliga tema escuro em tempo real */
    public void setTheme(String theme) {
        this.darkEnabled = "dark".equalsIgnoreCase(theme);
        if (stage != null && stage.getScene() != null) {
            applyStyles(stage.getScene());
        }
    }

    /** Alterna o tema atual (light/dark) */
    public void toggleTheme() {
        this.darkEnabled = !this.darkEnabled;
        if (stage != null && stage.getScene() != null) {
            applyStyles(stage.getScene());
        }
    }
}
