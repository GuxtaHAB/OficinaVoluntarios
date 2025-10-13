package br.com.oficinas.gestaooficinas.fx;

import br.com.oficinas.gestaooficinas.GestaoOficinasApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class FxLauncher extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        // Sobe o Spring (sem profile CLI, para não iniciar o menu de console)
        this.context = new SpringApplicationBuilder(GestaoOficinasApplication.class).run();
    }

    @Override
    public void start(Stage stage) {
        try {
            var router = context.getBean(SceneRouter.class);
            router.attach(stage);  // ✅ associa o Stage principal ao router
            router.go("/fxml/login.fxml", "Gestão de Oficinas - Login");
        } catch (Throwable t) {
            t.printStackTrace();
            throw t instanceof RuntimeException ? (RuntimeException) t : new RuntimeException(t);
        }
    }

    @Override
    public void stop() {
        context.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
