package br.com.oficinas.gestaooficinas.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemReparo item;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oficina_id", nullable = false)
    private Oficina oficina;

    @ManyToOne // opcional – definido após triagem
    @JoinColumn(name = "voluntario_id")
    private VoluntarioPerfil voluntarioAtribuido;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime dataHora;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusReparo status = StatusReparo.NA_FILA;

    @Column(length = 300)
    private String observacoes;

    protected Agendamento() {}

    public Agendamento(ItemReparo item, Oficina oficina, LocalDateTime dataHora) {
        this.item = item;
        this.oficina = oficina;
        this.dataHora = dataHora;
    }

    public Long getId() { return id; }
    public ItemReparo getItem() { return item; }
    public Oficina getOficina() { return oficina; }
    public VoluntarioPerfil getVoluntarioAtribuido() { return voluntarioAtribuido; }
    public LocalDateTime getDataHora() { return dataHora; }
    public StatusReparo getStatus() { return status; }
    public String getObservacoes() { return observacoes; }

    public void setVoluntarioAtribuido(VoluntarioPerfil v) { this.voluntarioAtribuido = v; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public void setStatus(StatusReparo status) { this.status = status; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
