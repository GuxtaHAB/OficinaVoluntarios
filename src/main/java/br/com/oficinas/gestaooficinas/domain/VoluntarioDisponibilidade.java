package br.com.oficinas.gestaooficinas.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "voluntario_disponibilidades")
public class VoluntarioDisponibilidade {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "voluntario_id", nullable = false)
    private VoluntarioPerfil voluntario;

    @NotNull @Enumerated(EnumType.STRING) @Column(name = "dia_semana", nullable = false, length = 10)
    private DayOfWeek diaSemana;

    @NotNull @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @NotNull @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    @Column(nullable = false)
    private boolean ativo = true;

    protected VoluntarioDisponibilidade() {}

    public VoluntarioDisponibilidade(VoluntarioPerfil voluntario, DayOfWeek diaSemana,
                                     LocalTime horaInicio, LocalTime horaFim) {
        this.voluntario = voluntario;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public Long getId() { return id; }
    public VoluntarioPerfil getVoluntario() { return voluntario; }
    public DayOfWeek getDiaSemana() { return diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public boolean isAtivo() { return ativo; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
