package br.com.oficinas.gestaooficinas.fx.controller;

import br.com.oficinas.gestaooficinas.domain.*;
import br.com.oficinas.gestaooficinas.fx.SceneRouter;
import br.com.oficinas.gestaooficinas.fx.UserSessionFx;
import br.com.oficinas.gestaooficinas.service.AgendamentoService;
import br.com.oficinas.gestaooficinas.service.DisponibilidadeService;
import br.com.oficinas.gestaooficinas.service.OficinaService;
import br.com.oficinas.gestaooficinas.service.VoluntarioService;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;

@Component
public class VoluntarioController {

    // topo
    @FXML private Label headerLabel;

    // PERFIL
    @FXML private TextField nomeField, emailField, idadeField;
    @FXML private ComboBox<Especialidade> especialidadeBox;
    @FXML private Label perfilMsg;

    // MINHA OFICINA
    @FXML private TableView<Oficina> oficinasTable;
    @FXML private TableColumn<Oficina, Long> colOfId;
    @FXML private TableColumn<Oficina, String> colOfNome, colOfCidade;
    @FXML private TextField oficinaIdField;
    @FXML private Label afiliacaoMsg;

    // DISPONIBILIDADE
    @FXML private TableView<VoluntarioDisponibilidade> dispTable;
    @FXML private TableColumn<VoluntarioDisponibilidade, String> colDia, colIni, colFim;
    @FXML private TableColumn<VoluntarioDisponibilidade, Long> colDispId;
    @FXML private TextField diaField, iniField, fimField, dispIdRemoverField;
    @FXML private Label dispMsg;

    // AGENDAMENTOS
    @FXML private TableView<Agendamento> agTable;
    @FXML private TableColumn<Agendamento, Long> colId;
    @FXML private TableColumn<Agendamento, String> colData, colItem, colStatus;
    @FXML private TextField agIdField;
    @FXML private ComboBox<StatusReparo> statusBox;
    @FXML private Label agMsg;
    @FXML private Label afiliadoLabel;
    @FXML private CheckBox selectAllDisp;

    private final UserSessionFx session;
    private final SceneRouter router;
    private final VoluntarioService voluntarioService;
    private final OficinaService oficinaService;
    private final AgendamentoService agService;
    private final DisponibilidadeService dispService;

    private VoluntarioPerfil perfil;

    private final ObservableList<Oficina> oficinasObs = FXCollections.observableArrayList();
    private final ObservableList<VoluntarioDisponibilidade> disps = FXCollections.observableArrayList();
    private final ObservableList<Agendamento> ags = FXCollections.observableArrayList();

    public VoluntarioController(UserSessionFx session, SceneRouter router,
                                VoluntarioService voluntarioService, OficinaService oficinaService,
                                AgendamentoService agService, DisponibilidadeService dispService) {
        this.session = session; this.router = router;
        this.voluntarioService = voluntarioService; this.oficinaService = oficinaService;
        this.agService = agService; this.dispService = dispService;
    }

    @FXML
    public void initialize() {
        if (!session.isLogged()) { router.go("/fxml/login.fxml", "Login"); return; }
        headerLabel.setText("Voluntário: " + session.getUsuario().getNome() + " (" + session.getUsuario().getEmail() + ")");

        // PERFIL
        emailField.setEditable(false);
        especialidadeBox.setItems(FXCollections.observableArrayList(Especialidade.values()));

        // MINHA OFICINA
        if (oficinasTable != null) {
            colOfId.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().getId()).asObject());
            colOfNome.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNome()));
            colOfCidade.setCellValueFactory(d -> new SimpleStringProperty(
                    (d.getValue().getCidade()==null?"":d.getValue().getCidade()) + "/" +
                    (d.getValue().getUf()==null?"":d.getValue().getUf())
            ));
            oficinasTable.setItems(oficinasObs);
        }

        // DISPONIBILIDADE
        colDia.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDiaSemana().name()));
        colIni.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHoraInicio().toString()));
        colFim.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHoraFim().toString()));
        colDispId.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().getId()).asObject());
        dispTable.setItems(disps);
        dispTable.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);

        // AGENDAMENTOS
        colId.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().getId()).asObject());
        colData.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataHora().toString()));
        colItem.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getItem().getCategoria()+" - "+d.getValue().getItem().getDescricao()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        agTable.setItems(ags);
        statusBox.setItems(FXCollections.observableArrayList(StatusReparo.values()));

        carregarTudo();
    }

    private void carregarTudo() {
        // Perfil
        var u = session.getUsuario();
        emailField.setText(u.getEmail());
        nomeField.setText(u.getNome());

        perfil = voluntarioService.obterPerfil(u); // pode ser null se ainda não criou
        if (perfil != null) {
            if (perfil.getIdade() != null) idadeField.setText(String.valueOf(perfil.getIdade()));
            if (perfil.getEspecialidade() != null) especialidadeBox.setValue(perfil.getEspecialidade());
        } else {
            idadeField.clear();
            especialidadeBox.getSelectionModel().clearSelection();
        }
        

        // Oficinas (lista para afiliar)
        oficinasObs.setAll(oficinaService.listarTodas());

        // Minhas disponibilidades
        if (perfil != null) disps.setAll(dispService.listarDisponibilidades(perfil.getId()));
        else disps.clear();

        // Meus agendamentos
        if (perfil != null) ags.setAll(agService.listarPorVoluntario(perfil.getId()));
        else ags.clear();

        perfilMsg.setText(""); afiliacaoMsg.setText(""); dispMsg.setText(""); agMsg.setText("");

        atualizarAfiliacaoLabel();
    }
    private void atualizarAfiliacaoLabel() {
        if (perfil != null && perfil.getOficinaAfiliada() != null) {
            var of = perfil.getOficinaAfiliada();
            var cidadeUf = (of.getCidade()==null?"":of.getCidade()) + "/" + (of.getUf()==null?"":of.getUf());
            afiliadoLabel.setText("Afiliado à oficina: " + of.getNome() + " (" + cidadeUf + ")");
        } else {
            afiliadoLabel.setText("Não afiliado a nenhuma oficina.");
        }
    }

    // === PERFIL ===
    @FXML
    public void onSalvarPerfil() {
        try {
            String nome = safe(nomeField.getText());
            if (nome.isEmpty()) throw new IllegalArgumentException("Nome não pode ser vazio.");
            session.getUsuario().setNome(nome); // se quiser refletir no header imediatamente

            Integer idade = null;
            if (!safe(idadeField.getText()).isEmpty()) idade = Integer.parseInt(idadeField.getText().trim());
            Especialidade esp = especialidadeBox.getValue();
            if (esp == null) throw new IllegalArgumentException("Selecione a especialidade.");

            perfil = voluntarioService.criarOuAtualizarPerfil(session.getUsuario(), esp, idade);
            headerLabel.setText("Voluntário: " + session.getUsuario().getNome() + " (" + session.getUsuario().getEmail() + ")");
            perfilMsg.setText("Perfil salvo.");
            carregarTudo();
        } catch (NumberFormatException n) {
            perfilMsg.setText("Idade inválida.");
        } catch (Exception e) {
            perfilMsg.setText("Erro: " + e.getMessage());
        }
    }

    // === MINHA OFICINA ===
    @FXML
    public void onAfiliar() {
        try {
            long ofId = Long.parseLong(safe(oficinaIdField.getText()));
            voluntarioService.afiliar(session.getUsuario(), ofId);
            afiliacaoMsg.setText("Afiliado com sucesso.");
            atualizarAfiliacaoLabel();

            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION,"Você se afiliou à oficina com sucesso.").showAndWait();

            oficinaIdField.clear();
            carregarTudo();
        } catch (NumberFormatException n) {
            afiliacaoMsg.setText("ID inválido.");
        } catch (Exception e) {
            afiliacaoMsg.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void onDesafiliar() {
        try {
            voluntarioService.desafiliar(session.getUsuario());
            afiliacaoMsg.setText("Desafiliado.");
            atualizarAfiliacaoLabel();
            carregarTudo();
        } catch (Exception e) {
            afiliacaoMsg.setText("Erro: " + e.getMessage());
        }
    }

    // === DISPONIBILIDADE ===
    @FXML
    public void onAddDisp() {
        try {
            if (perfil == null) throw new IllegalStateException("Crie seu perfil primeiro.");
            int nDia = Integer.parseInt(safe(diaField.getText()));
            if (nDia < 1 || nDia > 7) throw new IllegalArgumentException("Dia deve ser 1..7 (SEG..DOM).");
            DayOfWeek dia = DayOfWeek.of(nDia);

            LocalTime ini = parseHora(iniField.getText());
            LocalTime fim = parseHora(fimField.getText());
            dispService.definirDisponibilidadeVoluntario(session.getUsuario(), dia, ini, fim);

            diaField.clear(); iniField.clear(); fimField.clear();
            dispMsg.setText("Disponibilidade adicionada.");
            carregarTudo();
        } catch (Exception e) {
            dispMsg.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void onRemoverDisp() {
        try {
            long id = Long.parseLong(safe(dispIdRemoverField.getText()));
            dispService.removerDisponibilidade(id); // veja nota abaixo
            dispIdRemoverField.clear();
            dispMsg.setText("Disponibilidade removida.");
            carregarTudo();
        } catch (NumberFormatException n) {
            dispMsg.setText("ID inválido.");
        } catch (Exception e) {
            dispMsg.setText("Erro: " + e.getMessage());
        }
    }

    // === AGENDAMENTOS ===
    @FXML
    public void onAtualizarStatus() {
        try {
            long id = Long.parseLong(safe(agIdField.getText()));
            StatusReparo novo = statusBox.getValue();
            if (novo == null) throw new IllegalArgumentException("Selecione o novo status.");
            // Regra: voluntário não muda para CANCELADO se já CONCLUIDO (o service já valida)
            agService.atualizarStatus(id, novo);
            agIdField.clear(); statusBox.getSelectionModel().clearSelection();
            agMsg.setText("Status atualizado.");
            carregarTudo();
        } catch (NumberFormatException n) {
            agMsg.setText("ID inválido.");
        } catch (Exception e) {
            agMsg.setText("Erro: " + e.getMessage());
        }
    }
    @FXML
    public void onToggleSelectAllDisp() {
        try {
            if (selectAllDisp.isSelected()) {
                dispTable.getSelectionModel().selectAll();
            } else {
                dispTable.getSelectionModel().clearSelection();
            }
        } catch (Exception e) {
            dispMsg.setText("Erro: " + e.getMessage());
        }
    }
    @FXML
    public void onRemoverSelecionadas() {
        try {
            var selecionadas = new java.util.ArrayList<>(dispTable.getSelectionModel().getSelectedItems());
            if (selecionadas.isEmpty()) {
                dispMsg.setText("Nenhuma disponibilidade selecionada.");
                return;
            }
            // confirmação opcional
            var confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION,
                    "Remover " + selecionadas.size() + " disponibilidade(s) selecionada(s)?", 
                    javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
            var resp = confirm.showAndWait();
            if (resp.isEmpty() || resp.get() != javafx.scene.control.ButtonType.YES) return;

            for (var d : selecionadas) {
                dispService.removerDisponibilidade(d.getId());
            }
            dispTable.getSelectionModel().clearSelection();
            selectAllDisp.setSelected(false);
            dispMsg.setText("Remoção concluída.");
            carregarTudo();
        } catch (Exception e) {
            dispMsg.setText("Erro: " + e.getMessage());
        }
    }
    @FXML
    public void onAddDispSemana() {
        try {
            if (perfil == null) throw new IllegalStateException("Crie seu perfil primeiro.");
            var ini = parseHora(iniField.getText());
            var fim = parseHora(fimField.getText());
            for (int i = 1; i <= 7; i++) {
                dispService.definirDisponibilidadeVoluntario(session.getUsuario(), DayOfWeek.of(i), ini, fim);
            }
            dispMsg.setText("Disponibilidade adicionada para todos os dias.");
            carregarTudo();
        } catch (Exception e) {
            dispMsg.setText("Erro: " + e.getMessage());
        }
    }

    // === Sessão ===
    @FXML
    public void onLogout() {
        session.clear();
        Stage s = (Stage) headerLabel.getScene().getWindow();
        router.attach(s);
        router.go("/fxml/login.fxml", "Login");
    }

    // === Utils ===
    private static String safe(String s) { return s == null ? "" : s.trim(); }

    private static LocalTime parseHora(String hhmm) {
        String t = safe(hhmm);
        if (t.length() == 5) t += ":00";
        return LocalTime.parse(t);
    }
}