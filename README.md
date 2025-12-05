Sistema de Gestão de Oficinas – README

Este projeto é uma aplicação completa para gestão de oficinas
voluntárias, composta por um cliente JavaFX + Spring Boot (gestão de
telas, controladores, navegação e serviços) integrado a um backend REST
em Spring Boot.

------------------------------------------------------------------------

🧰 Tecnologias Utilizadas

Frontend (Desktop)

-   Java 21+
-   JavaFX 17+
-   Spring Boot 3+ (para injeção de dependências e organização modular)
-   Maven
-   FXML (layouts)
-   Controllers estruturados

Backend

-   Spring Boot 3+
-   Spring Web
-   Spring Data JPA
-   Hibernate
-   Banco H2 / PostgreSQL (dependendo do perfil)
-   Maven

------------------------------------------------------------------------

🎯 Principais Funcionalidades

1. Papéis do sistema

-   Cliente
    -   Cadastro de itens
    -   Agendamentos
    -   Consulta de status e histórico
-   Voluntário
    -   Gerenciamento de disponibilidade
    -   Atualização de status dos reparos
    -   Afiliação a oficinas
-   Dono da Oficina
    -   Gerenciamento da oficina
    -   Triagem e atribuição de agendamentos
    -   Relatórios
    -   Gerenciamento de horários de atendimento

2. Módulos

-   Login e autenticação
-   CRUDs completos
-   Agenda e filtragem por intervalo de datas
-   Exportação CSV
-   Relatórios com indicadores
-   Navegação usando SceneRouter e FxViewLoader

------------------------------------------------------------------------

📁 Estrutura Geral do Projeto

    gestao-oficinas/
     ├── backend/
     │    ├── src/main/java
     │    │     ├── controller/
     │    │     ├── service/
     │    │     ├── repository/
     │    │     └── model/
     │    └── src/main/resources
     │          ├── application.properties
     │          └── data.sql (opcional)
     │
     └── client/
          ├── src/main/java/br/.../fx
          │      ├── SceneRouter.java
          │      ├── FxViewLoader.java
          │      ├── controller/
          │      └── config/
          └── src/main/resources
                 ├── fxml/
                 └── styles.css

------------------------------------------------------------------------

▶️ Como Rodar o Projeto

1. Requisitos

-   Java 21+
-   Maven 3.9+
-   (Opcional) PostgreSQL 14+, caso não use H2

------------------------------------------------------------------------

🖥️ Executando o Backend

Dentro da pasta backend/:

    mvn spring-boot:run

O backend subirá em:

    http://localhost:8080

------------------------------------------------------------------------

🖼️ Executando o Cliente JavaFX

Dentro da pasta client/:

    mvn clean javafx:run

Caso use wrapper:

    ./mvnw javafx:run

------------------------------------------------------------------------

🔧 Configurações Opcionais

Trocar entre H2 e PostgreSQL

No application.properties:

Para H2:

    spring.datasource.url=jdbc:h2:mem:oficinas
    spring.jpa.hibernate.ddl-auto=update
    spring.h2.console.enabled=true

Para PostgreSQL:

    spring.datasource.url=jdbc:postgresql://localhost:5432/oficinas
    spring.datasource.username=postgres
    spring.datasource.password=1234
    spring.jpa.hibernate.ddl-auto=update

------------------------------------------------------------------------

📝 Observações Importantes

-   Certifique-se de que o backend está rodando antes de iniciar o
    cliente.

-   Caso ocorra erro de CORS ou comunicação, verifique a URL configurada
    no cliente.

-   Todos os layouts FXML devem estar na pasta resources/fxml/.

-   Arquivo de estilo global pode ser adicionado em:

        resources/styles.css

------------------------------------------------------------------------

📄 Licença

Uso livre para fins acadêmicos e estudos.

------------------------------------------------------------------------
