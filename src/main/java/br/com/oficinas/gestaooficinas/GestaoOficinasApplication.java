/*package br.com.oficinas.gestaooficinas;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import br.com.oficinas.gestaooficinas.cli.MenuPrincipal;

@SpringBootApplication
public class GestaoOficinasApplication implements CommandLineRunner {

    private final MenuPrincipal menuPrincipal;

    public GestaoOficinasApplication(MenuPrincipal menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
    }

    public static void main(String[] args) {
        SpringApplication.run(GestaoOficinasApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // Loop principal do CLI
        menuPrincipal.iniciar();
    }
}*/

package br.com.oficinas.gestaooficinas;

import br.com.oficinas.gestaooficinas.cli.MenuPrincipal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class GestaoOficinasApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestaoOficinasApplication.class, args);
    }

    @Bean
    @Profile("cli")
    CommandLineRunner cliRunner(MenuPrincipal menuPrincipal) {
        return args -> menuPrincipal.iniciar();
    }
}
