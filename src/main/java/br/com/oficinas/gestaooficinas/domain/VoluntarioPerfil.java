package br.com.oficinas.gestaooficinas.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "voluntarios", uniqueConstraints = {
        @UniqueConstraint(name = "uk_voluntario_usuario", columnNames = "usuario_id")
})
public class VoluntarioPerfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Especialidade especialidade;

    @Min(14) @Max(120)
    private Integer idade;

   
    @ManyToOne
    @JoinColumn(name = "oficina_id")
    private Oficina oficinaAfiliada;

    protected VoluntarioPerfil() {}

    public VoluntarioPerfil(Usuario usuario, Especialidade especialidade, Integer idade) {
        this.usuario = usuario;
        this.especialidade = especialidade;
        this.idade = idade;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Especialidade getEspecialidade() { return especialidade; }
    public Integer getIdade() { return idade; }
    public Oficina getOficinaAfiliada() { return oficinaAfiliada; }

    public void setEspecialidade(Especialidade especialidade) { this.especialidade = especialidade; }
    public void setIdade(Integer idade) { this.idade = idade; }
    public void setOficinaAfiliada(Oficina oficinaAfiliada) { this.oficinaAfiliada = oficinaAfiliada; }
}
