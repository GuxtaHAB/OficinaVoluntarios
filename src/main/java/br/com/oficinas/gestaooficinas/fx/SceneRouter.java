package br.com.oficinas.gestaooficinas.fx;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.springframework.stereotype.Component;

@Component
public class SceneRouter {

    private final FxViewLoader loader;
    private Stage stage;

    public SceneRouter(FxViewLoader loader) {
        this.loader = loader;
    }

    public void attach(Stage stage) {
        this.stage = stage;
    }

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

    public void go(String fxmlPath, String title) {
        if (!ensureAttached()) {
            throw new IllegalStateException("Stage não associado ao router.");
        }
        Scene scene = new Scene(loader.load(fxmlPath));
        this.stage.setScene(scene);
        if (title != null && !title.isBlank()) {
            this.stage.setTitle(title);
        }
        this.stage.show();
    }
}
