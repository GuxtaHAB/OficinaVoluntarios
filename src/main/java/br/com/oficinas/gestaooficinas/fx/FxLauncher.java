package br.com.oficinas.gestaooficinas.fx;

import br.com.oficinas.gestaooficinas.GestaoOficinasApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
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
            FxViewLoader loader = context.getBean(FxViewLoader.class);
            SceneRouter router  = context.getBean(SceneRouter.class);   // <- NOVO
            router.attach(stage);                                       // <- NOVO

            Scene scene = new Scene(loader.load("/fxml/login.fxml"));
            stage.setTitle("Gestão de Oficinas - Login");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setWidth(1120);
            stage.setHeight(780);
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.show();
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
