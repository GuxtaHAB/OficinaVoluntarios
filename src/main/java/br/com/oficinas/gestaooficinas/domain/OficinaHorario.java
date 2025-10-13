package br.com.oficinas.gestaooficinas.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "oficina_horarios",
        uniqueConstraints = @UniqueConstraint(name = "uk_oficina_dia", columnNames = {"oficina_id", "dia_semana"}))
public class OficinaHorario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "oficina_id", nullable = false)
    private Oficina oficina;

    @NotNull @Enumerated(EnumType.STRING) @Column(name = "dia_semana", nullable = false, length = 10)
    private DayOfWeek diaSemana;

    @NotNull @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @NotNull @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    @Column(nullable = false)
    private boolean ativo = true;

    protected OficinaHorario() {}

    public OficinaHorario(Oficina oficina, DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFim) {
        this.oficina = oficina;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public Long getId() { return id; }
    public Oficina getOficina() { return oficina; }
    public DayOfWeek getDiaSemana() { return diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public boolean isAtivo() { return ativo; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
