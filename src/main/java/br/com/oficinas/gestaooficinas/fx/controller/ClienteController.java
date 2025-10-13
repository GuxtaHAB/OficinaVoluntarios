package br.com.oficinas.gestaooficinas.fx.controller;

import br.com.oficinas.gestaooficinas.domain.*;
import br.com.oficinas.gestaooficinas.fx.SceneRouter;
import br.com.oficinas.gestaooficinas.fx.UserSessionFx;
import br.com.oficinas.gestaooficinas.service.ClienteService;
import br.com.oficinas.gestaooficinas.service.OficinaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ClienteController {

    // topo
    @FXML private Label headerLabel;

    // tab itens
    @FXML private TableView<ItemReparo> itensTable;
    @FXML private TableColumn<ItemReparo, Long> colItemId;
    @FXML private TableColumn<ItemReparo, String> colItemCat;
    @FXML private TableColumn<ItemReparo, String> colItemDesc;
    @FXML private ComboBox<CategoriaItem> categoriaBox;
    @FXML private TextField descricaoField;

    // tab agendar
    @FXML private TableView<ItemReparo> itensAgendarTable;
    @FXML private TableColumn<ItemReparo, Long> colAgiItemId;
    @FXML private TableColumn<ItemReparo, String> colAgiItemCat;
    @FXML private TableColumn<ItemReparo, String> colAgiItemDesc;
    @FXML private TableView<Oficina> oficinasTable;
    @FXML private TableColumn<Oficina, Long> colOfId;
    @FXML private TableColumn<Oficina, String> colOfNome;
    @FXML private TableColumn<Oficina, String> colOfCidade;
    @FXML private TextField dataField, horaField, obsField;
    @FXML private Label agendarMsg;

    // tab agendamentos
    @FXML private TableView<Agendamento> agendamentosTable;
    @FXML private TableColumn<Agendamento, Long> colAgId;
    @FXML private TableColumn<Agendamento, String> colAgData;
    @FXML private TableColumn<Agendamento, String> colAgOficina;
    @FXML private TableColumn<Agendamento, String> colAgStatus;
    @FXML private TableColumn<Agendamento, String> colAgVol;
    @FXML private TextField cancelIdField;
    @FXML private Label cancelMsg;

    private final UserSessionFx session;
    private final SceneRouter router;
    private final ClienteService clienteService;
    private final OficinaService oficinaService;

    private final ObservableList<ItemReparo> itens = FXCollections.observableArrayList();
    private final ObservableList<ItemReparo> itensParaAgendar = FXCollections.observableArrayList();
    private final ObservableList<Oficina> oficinas = FXCollections.observableArrayList();
    private final ObservableList<Agendamento> agendamentos = FXCollections.observableArrayList();

    public ClienteController(UserSessionFx session, SceneRouter router,
                             ClienteService clienteService, OficinaService oficinaService) {
        this.session = session; this.router = router;
        this.clienteService = clienteService; this.oficinaService = oficinaService;
    }

    @FXML
    public void initialize() {
        if (!session.isLogged()) { router.go("/fxml/login.fxml", "Login"); return; }
        headerLabel.setText("Cliente: " + session.getUsuario().getNome() + " (" + session.getUsuario().getEmail() + ")");

        // Tabela itens
        colItemId.setCellValueFactory(d -> new javafx.beans.property.SimpleLongProperty(d.getValue().getId()).asObject());
        colItemCat.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCategoria().name()));
        colItemDesc.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getDescricao()));
        itensTable.setItems(itens);

        // Tabela itens na aba Agendar
        colAgiItemId.setCellValueFactory(d -> new javafx.beans.property.SimpleLongProperty(d.getValue().getId()).asObject());
        colAgiItemCat.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCategoria().name()));
        colAgiItemDesc.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getDescricao()));
        itensAgendarTable.setItems(itensParaAgendar);

        // Tabela oficinas
        colOfId.setCellValueFactory(d -> new javafx.beans.property.SimpleLongProperty(d.getValue().getId()).asObject());
        colOfNome.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNome()));
        colOfCidade.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                (d.getValue().getCidade()==null?"":d.getValue().getCidade()) + "/" + (d.getValue().getUf()==null?"":d.getValue().getUf())
        ));
        oficinasTable.setItems(oficinas);

        // Tabela agendamentos
        colAgId.setCellValueFactory(d -> new javafx.beans.property.SimpleLongProperty(d.getValue().getId()).asObject());
        colAgData.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getDataHora().toString()));
        colAgOficina.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getOficina().getNome()));
        colAgStatus.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus().name()));
        colAgVol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getVoluntarioAtribuido()==null? "(sem)" : d.getValue().getVoluntarioAtribuido().getUsuario().getNome()
        ));
        agendamentosTable.setItems(agendamentos);

        categoriaBox.setItems(FXCollections.observableArrayList(CategoriaItem.values()));

        recarregarTudo();
    }

    private void recarregarTudo() {
        Long id = session.getUsuario().getId();
        List<ItemReparo> meusItens = clienteService.listarItensDoCliente(id);
        itens.setAll(meusItens);
        itensParaAgendar.setAll(meusItens);
        oficinas.setAll(oficinaService.listarTodas());
        agendamentos.setAll(clienteService.listarAgendamentosDoCliente(id));
        agendarMsg.setText(""); cancelMsg.setText("");
    }

    // === Ações ===
    @FXML
    public void onCriarItem() {
        try {
            if (categoriaBox.getValue() == null) throw new IllegalArgumentException("Escolha a categoria.");
            String desc = descricaoField.getText().trim();
            if (desc.isEmpty()) throw new IllegalArgumentException("Informe a descrição.");
            clienteService.criarItem(session.getUsuario(), categoriaBox.getValue(), desc);
            descricaoField.clear(); categoriaBox.getSelectionModel().clearSelection();
            recarregarTudo();
        } catch (Exception e) {
            showAlert("Erro", e.getMessage());
        }
    }

    @FXML
    public void onAgendar() {
        try {
            ItemReparo item = itensAgendarTable.getSelectionModel().getSelectedItem();
            Oficina of = oficinasTable.getSelectionModel().getSelectedItem();
            if (item == null || of == null) throw new IllegalArgumentException("Selecione um item e uma oficina.");
            String data = dataField.getText().trim();
            String hora = horaField.getText().trim();
            LocalDateTime dt = LocalDateTime.parse(data + "T" + (hora.length()==5?hora+":00":hora));
            String obs = obsField.getText().trim();
            if (obs.isEmpty()) obs = null;

            var ag = clienteService.agendar(session.getUsuario(), item.getId(), of.getId(), dt, obs);
            agendarMsg.setText("Agendado: ID=" + ag.getId() + " " + ag.getDataHora());
            dataField.clear(); horaField.clear(); obsField.clear();
            recarregarTudo();
        } catch (Exception e) {
            agendarMsg.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        try {
            long id = Long.parseLong(cancelIdField.getText().trim());
            var ag = clienteService.cancelarAgendamento(session.getUsuario(), id);
            cancelMsg.setText("Cancelado. Status: " + ag.getStatus());
            cancelIdField.clear();
            recarregarTudo();
        } catch (NumberFormatException n) {
            cancelMsg.setText("ID inválido.");
        } catch (Exception e) {
            cancelMsg.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void onLogout() {
        session.clear();
        router.go("/fxml/login.fxml", "Login");
    }

    private void showAlert(String title, String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}
