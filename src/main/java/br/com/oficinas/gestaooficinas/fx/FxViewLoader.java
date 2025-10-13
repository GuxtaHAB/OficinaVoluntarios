package br.com.oficinas.gestaooficinas.fx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
public class FxViewLoader {

    private final ApplicationContext ctx;

    public FxViewLoader(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    public Parent load(String fxmlPath) {
        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                System.err.println("❌ FXML não encontrado no classpath: " + fxmlPath);
                System.err.println("   Verifique se o arquivo existe em: src/main/resources" + fxmlPath);
                throw new IllegalStateException("FXML não encontrado: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(ctx::getBean); // controllers como beans Spring
            return loader.load();
        } catch (IOException e) {
            System.err.println("❌ Falha ao carregar view: " + fxmlPath);
            e.printStackTrace();
            throw new RuntimeException("Falha ao carregar view: " + fxmlPath, e);
        } catch (Exception e) {
            System.err.println("❌ Erro inesperado ao carregar view: " + fxmlPath);
            e.printStackTrace();
            throw e;
        }
    }
}
