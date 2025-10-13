package br.com.oficinas.gestaooficinas.fx.controller;
import org.springframework.stereotype.Component;

import br.com.oficinas.gestaooficinas.domain.Agendamento;
import br.com.oficinas.gestaooficinas.domain.CategoriaItem;
import br.com.oficinas.gestaooficinas.domain.Oficina;
import br.com.oficinas.gestaooficinas.domain.OficinaHorario;
import br.com.oficinas.gestaooficinas.domain.StatusReparo;
import br.com.oficinas.gestaooficinas.domain.Usuario;
import br.com.oficinas.gestaooficinas.domain.VoluntarioPerfil;
import br.com.oficinas.gestaooficinas.fx.SceneRouter;
import br.com.oficinas.gestaooficinas.fx.UserSessionFx;
import br.com.oficinas.gestaooficinas.repository.UsuarioRepository;
import br.com.oficinas.gestaooficinas.service.AgendamentoService;
import br.com.oficinas.gestaooficinas.service.DisponibilidadeService;
import br.com.oficinas.gestaooficinas.service.OficinaService;
import br.com.oficinas.gestaooficinas.service.VoluntarioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleLongProperty;

@Component
public class DonoController {

    @FXML private Label headerLabel, statusLabel, triagemMsg;
    @FXML private TextField nomeField, enderecoField, cidadeField, ufField, telefoneField, capacidadeField;
    // triagem:
    @FXML private TableView<Agendamento> agTable;
    @FXML private TableColumn<Agendamento, Long> colAgId;
    @FXML private TableColumn<Agendamento, String> colAgData, colAgItem, colAgStatus, colAgVol;
    @FXML private TableView<VoluntarioPerfil> volTable;
    @FXML private TableColumn<VoluntarioPerfil, Long> colVolId;
    @FXML private TableColumn<VoluntarioPerfil, String> colVolNome, colVolEsp;
    @FXML private TextField agIdField, volIdField;
    // --- AGENDA ---
    @FXML private javafx.scene.control.DatePicker dpInicio, dpFim;
    @FXML private javafx.scene.control.TableView<Agendamento> agTableAgenda;
    @FXML private javafx.scene.control.TableColumn<Agendamento, Long> colAgAId;
    @FXML private javafx.scene.control.TableColumn<Agendamento, String> colAgAData, colAgAItem, colAgAStatus, colAgAVol;
    @FXML private javafx.scene.control.Label agendaMsg;
    // --- RELATÓRIOS ---
    @FXML private javafx.scene.control.DatePicker rpInicio, rpFim;
    @FXML private javafx.scene.control.Label lblTotalAg, lblPorStatus, lblPorCategoria, lblImpacto, relMsg;
    @FXML private TableView<OficinaHorario> horariosTable;
    @FXML private TableColumn<OficinaHorario, Long> colHId;
    @FXML private TableColumn<OficinaHorario, String> colHDia, colHIni, colHFim;
    @FXML private TextField diaOfField;   
    @FXML private TextField iniOfField;   
    @FXML private TextField fimOfField;   
    @FXML private TextField remDiaField;
    @FXML private CheckBox selectAllHor;
    @FXML private Label horMsg;

    private final UsuarioRepository usuarioRepository;
    private final OficinaService oficinaService;
    private final VoluntarioService voluntarioService;
    private final AgendamentoService agendamentoService;
    private final UserSessionFx session;
    private final SceneRouter router;
    private final DisponibilidadeService disponibilidadeService;

    private final ObservableList<Agendamento> ags = FXCollections.observableArrayList();
    private final ObservableList<Agendamento> agendaList = FXCollections.observableArrayList();
    private final ObservableList<VoluntarioPerfil> vols = FXCollections.observableArrayList();
    private final ObservableList<OficinaHorario> horarios = FXCollections.observableArrayList();
    private Usuario dono;
    private Oficina oficina;

    public DonoController(UsuarioRepository usuarioRepository, OficinaService oficinaService,
                          VoluntarioService voluntarioService, DisponibilidadeService disponibilidadeService, AgendamentoService agendamentoService,
                          UserSessionFx session, SceneRouter router) {
        this.usuarioRepository = usuarioRepository;
        this.oficinaService = oficinaService;
        this.voluntarioService = voluntarioService;
        this.agendamentoService = agendamentoService;
        this.session = session; this.router = router;
        this.disponibilidadeService = disponibilidadeService;
    }
    

    @FXML
    public void initialize() {
        if (!session.isLogged()) { router.go("/fxml/login.fxml", "Login"); return; }
        this.dono = session.getUsuario();
        headerLabel.setText("Dono: " + dono.getNome() + " (" + dono.getEmail() + ")");

        // carregar oficina (se houver) para preencher o form:
        this.oficina = oficinaService.buscarPorDono(dono);
        if (oficina != null) {
            nomeField.setText(oficina.getNome());
            enderecoField.setText(oficina.getEndereco());
            cidadeField.setText(oficina.getCidade());
            ufField.setText(oficina.getUf());
            telefoneField.setText(oficina.getTelefone());
            if (oficina.getCapacidadePorHora()!=null)
                capacidadeField.setText(String.valueOf(oficina.getCapacidadePorHora()));
        }
        if (horariosTable != null) {
            colHId.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().getId()).asObject());
            colHDia.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDiaSemana().name()));
            colHIni.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHoraInicio().toString()));
            colHFim.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHoraFim().toString()));
            horariosTable.setItems(horarios);
            horariosTable.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        }
        recarregarHorarios();
        

        // tabelas triagem
        colAgId.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().getId()).asObject());
        colAgData.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataHora().toString()));
        colAgItem.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getItem().getCategoria()+" - "+d.getValue().getItem().getDescricao()));
        colAgStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        colAgVol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getVoluntarioAtribuido()==null?"(sem)":
                        d.getValue().getVoluntarioAtribuido().getUsuario().getNome()
        ));
        agTable.setItems(ags);

        colVolId.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().getId()).asObject());
        colVolNome.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsuario().getNome()));
        colVolEsp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEspecialidade().name()));
        volTable.setItems(vols);

        recarregarTriagem();


        if (agTableAgenda != null) {
        colAgAId.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().getId()).asObject());
        colAgAData.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataHora().toString()));
        colAgAItem.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getItem().getCategoria() + " - " + d.getValue().getItem().getDescricao()));
        colAgAStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        colAgAVol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getVoluntarioAtribuido() == null ? "(sem)" :
                        d.getValue().getVoluntarioAtribuido().getUsuario().getNome()
        ));
        agTableAgenda.setItems(agendaList);
    }
    }

    private void recarregarTriagem() {
    this.oficina = oficinaService.buscarPorDono(dono);
    if (oficina != null) {
        ags.setAll(agendamentoService.listarPorOficina(oficina.getId()));
        // antes: vols.setAll(voluntarioService.listarAfiliados(oficina.getId()));
        vols.setAll(oficinaService.listarVoluntariosAfiliados(oficina.getId()));
    } else {
        ags.clear();
        vols.clear();
    }
    triagemMsg.setText("");
    }
    private void recarregarHorarios() {
        if (oficina != null) {
            horarios.setAll(disponibilidadeService.listarHorariosOficina(oficina.getId()));
        } else {
            horarios.clear();
        }
        if (horMsg != null) horMsg.setText("");
    }

    @FXML
    public void onSalvarOficina() {
        try {
            Integer cap = null;
            if (!capacidadeField.getText().trim().isEmpty())
                cap = Integer.parseInt(capacidadeField.getText().trim());

            this.oficina = oficinaService.criarOuAtualizar(
                    dono,
                    nomeField.getText().trim(),
                    enderecoField.getText().trim(),
                    cidadeField.getText().trim(),
                    ufField.getText().trim().isEmpty()? null : ufField.getText().trim().toUpperCase(),
                    telefoneField.getText().trim().isEmpty()? null : telefoneField.getText().trim(),
                    cap
            );
            statusLabel.setText("Oficina salva: [" + oficina.getId() + "] " + oficina.getNome());
            recarregarTriagem();
        } catch (Exception e) {
            statusLabel.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void onAtribuir() {
        try {
            long agId = Long.parseLong(agIdField.getText().trim());
            long volId = Long.parseLong(volIdField.getText().trim());
            var ag = agendamentoService.atribuirVoluntario(agId, volId);
            triagemMsg.setText("Atribuído! " + ag.getVoluntarioAtribuido().getUsuario().getNome());
            agIdField.clear(); volIdField.clear();
            recarregarTriagem();
        } catch (NumberFormatException n) {
            triagemMsg.setText("IDs inválidos.");
        } catch (Exception e) {
            triagemMsg.setText("Erro: " + e.getMessage());
        }
    }
    @FXML
    public void onFiltrarAgenda() {
        try {
            if (oficina == null) {
                agendaMsg.setText("Cadastre sua oficina primeiro.");
                return;
            }
            var todos = agendamentoService.listarPorOficina(oficina.getId());
            var ini = dpInicio.getValue() == null ? null : dpInicio.getValue().atStartOfDay();
            var fim = dpFim.getValue() == null ? null : dpFim.getValue().atTime(23, 59, 59);

            var filtrados = todos.stream().filter(a -> {
                var dt = a.getDataHora();
                boolean ok = true;
                if (ini != null) ok &= !dt.isBefore(ini);
                if (fim != null) ok &= !dt.isAfter(fim);
                return ok;
            }).toList();

            agendaList.setAll(filtrados);
            agendaMsg.setText("Exibindo " + filtrados.size() + " agendamento(s).");
        } catch (Exception e) {
            agendaMsg.setText("Erro: " + e.getMessage());
        }
    }
    @FXML
    public void onLimparAgenda() {
        dpInicio.setValue(null);
        dpFim.setValue(null);
        agendaList.clear();
        agendaMsg.setText("");
    }
    @FXML
    public void onAddHorario() {
        try {
            if (oficina == null) { horMsg.setText("Cadastre sua oficina primeiro."); return; }

            int nDia = Integer.parseInt(diaOfField.getText().trim());
            if (nDia < 1 || nDia > 7) throw new IllegalArgumentException("Dia deve ser 1..7 (SEG..DOM).");
            var dia = java.time.DayOfWeek.of(nDia);

            var ini = parseHora(iniOfField.getText());
            var fim = parseHora(fimOfField.getText());

            disponibilidadeService.definirHorarioOficina(dono, dia, ini, fim); // ✅ usa seu service

            diaOfField.clear(); iniOfField.clear(); fimOfField.clear();
            horMsg.setText("Horário salvo para " + dia + ".");
            recarregarHorarios();
        } catch (NumberFormatException e) {
            horMsg.setText("Dia inválido.");
        } catch (Exception e) {
            horMsg.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void onDesativarHorario() {
        try {
            if (oficina == null) { horMsg.setText("Cadastre sua oficina primeiro."); return; }

            int nDia = Integer.parseInt(remDiaField.getText().trim());
            if (nDia < 1 || nDia > 7) throw new IllegalArgumentException("Dia deve ser 1..7 (SEG..DOM).");
            var dia = java.time.DayOfWeek.of(nDia);

            disponibilidadeService.desativarHorarioOficina(dono, dia); // ✅ usa seu service
            remDiaField.clear();
            horMsg.setText("Horário desativado para " + dia + ".");
            recarregarHorarios();
        } catch (NumberFormatException e) {
            horMsg.setText("Dia inválido.");
        } catch (Exception e) {
            horMsg.setText("Erro: " + e.getMessage());
        }
    }
    @FXML
    private void onToggleSelectAllHor() {
        try {
            if (selectAllHor.isSelected()) {
                horariosTable.getSelectionModel().selectAll();
            } else {
                horariosTable.getSelectionModel().clearSelection();
            }
        } catch (Exception e) {
            if (horMsg != null) horMsg.setText("Erro: " + e.getMessage());
        }
    }
    @FXML
    private void onDesativarSelecionados() {
        try {
            var selecionados = new java.util.ArrayList<>(horariosTable.getSelectionModel().getSelectedItems());
            if (selecionados.isEmpty()) {
                horMsg.setText("Nenhum horário selecionado.");
                return;
            }

            // confirmação
            var confirm = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.CONFIRMATION,
                    "Desativar " + selecionados.size() + " horário(s) selecionado(s)?",
                    javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO
            );
            var resp = confirm.showAndWait();
            if (resp.isEmpty() || resp.get() != javafx.scene.control.ButtonType.YES) return;

            // desativar por DIA (evita duplicar dias repetidos)
            var diasUnicos = selecionados.stream()
                    .map(br.com.oficinas.gestaooficinas.domain.OficinaHorario::getDiaSemana)
                    .distinct()
                    .toList();

            for (var dia : diasUnicos) {
                disponibilidadeService.desativarHorarioOficina(dono, dia);
            }

            horariosTable.getSelectionModel().clearSelection();
            selectAllHor.setSelected(false);
            horMsg.setText("Horários selecionados desativados.");
            recarregarHorarios();
        } catch (Exception e) {
            horMsg.setText("Erro: " + e.getMessage());
        }
    }

    private static java.time.LocalTime parseHora(String hhmm) {
        String t = (hhmm == null ? "" : hhmm.trim());
        if (t.length() == 5) t += ":00";
        return java.time.LocalTime.parse(t);
    }
    @FXML
    public void onGerarRelatorio() {
        try {
            if (oficina == null) {
                relMsg.setText("Cadastre sua oficina primeiro.");
                return;
            }
            var todos = agendamentoService.listarPorOficina(oficina.getId());
            var ini = rpInicio.getValue() == null ? null : rpInicio.getValue().atStartOfDay();
            var fim = rpFim.getValue() == null ? null : rpFim.getValue().atTime(23, 59, 59);

            var filtrados = todos.stream().filter(a -> {
                var dt = a.getDataHora();
                boolean ok = true;
                if (ini != null) ok &= !dt.isBefore(ini);
                if (fim != null) ok &= !dt.isAfter(fim);
                return ok;
            }).toList();

            lblTotalAg.setText("Total de agendamentos: " + filtrados.size());

            var porStatus = new java.util.LinkedHashMap<StatusReparo, Long>();
            for (var s : StatusReparo.values()) {
                long c = filtrados.stream().filter(a -> a.getStatus() == s).count();
                porStatus.put(s, c);
            }
            lblPorStatus.setText("Por status: " + porStatus);

            var porCategoria = new java.util.LinkedHashMap<CategoriaItem, Long>();
            for (var c : CategoriaItem.values()) {
                long cnt = filtrados.stream().filter(a -> a.getItem().getCategoria() == c).count();
                porCategoria.put(c, cnt);
            }
            lblPorCategoria.setText("Por categoria: " + porCategoria);

            // “Impacto” simples: CONCLUÍDO = 1 item salvo do descarte
            long concluidos = porStatus.getOrDefault(StatusReparo.CONCLUIDO, 0L);
            lblImpacto.setText("Impacto estimado: " + concluidos + " item(ns) reaproveitado(s).");

            relMsg.setText("Relatório gerado.");
        } catch (Exception e) {
            relMsg.setText("Erro: " + e.getMessage());
        }
    }
    @FXML
    public void onExportarCsv() {
        try {
            if (oficina == null) {
                relMsg.setText("Cadastre sua oficina primeiro.");
                return;
            }
            var todos = agendamentoService.listarPorOficina(oficina.getId());
            var ini = rpInicio.getValue() == null ? null : rpInicio.getValue().atStartOfDay();
            var fim = rpFim.getValue() == null ? null : rpFim.getValue().atTime(23, 59, 59);

            var filtrados = todos.stream().filter(a -> {
                var dt = a.getDataHora();
                boolean ok = true;
                if (ini != null) ok &= !dt.isBefore(ini);
                if (fim != null) ok &= !dt.isAfter(fim);
                return ok;
            }).toList();

            StringBuilder sb = new StringBuilder();
            sb.append("id,dataHora,status,categoria,descricao,voluntario,cliente,oficina\n");
            for (var a : filtrados) {
                var vol = a.getVoluntarioAtribuido() == null ? "" : a.getVoluntarioAtribuido().getUsuario().getNome();
                var cli = a.getItem().getCliente().getNome();
                sb.append(a.getId()).append(',')
                .append(a.getDataHora()).append(',')
                .append(a.getStatus()).append(',')
                .append(a.getItem().getCategoria()).append(',')
                .append(escapeCsv(a.getItem().getDescricao())).append(',')
                .append(escapeCsv(vol)).append(',')
                .append(escapeCsv(cli)).append(',')
                .append(escapeCsv(a.getOficina().getNome()))
                .append('\n');
            }

            var chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Salvar relatório CSV");
            chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV", "*.csv"));
            var file = chooser.showSaveDialog(((javafx.scene.Node) lblTotalAg).getScene().getWindow());
            if (file != null) {
                java.nio.file.Files.writeString(file.toPath(), sb.toString(), java.nio.charset.StandardCharsets.UTF_8);
                relMsg.setText("CSV exportado: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            relMsg.setText("Erro: " + e.getMessage());
        }
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        boolean needQuotes = s.contains(",") || s.contains("\"") || s.contains("\n");
        String out = s.replace("\"", "\"\"");
        return needQuotes ? "\"" + out + "\"" : out;
    }

    @FXML
    public void onLogout() { session.clear(); router.go("/fxml/login.fxml", "Login"); }
}
