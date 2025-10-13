package br.com.oficinas.gestaooficinas.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "oficinas", uniqueConstraints = {
        @UniqueConstraint(name = "uk_oficina_dono", columnNames = "dono_id")
})
public class Oficina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min = 2, max = 120)
    private String nome;

    @NotBlank @Size(min = 2, max = 200)
    private String endereco;

    @Size(max = 80)
    private String cidade;

    @Size(max = 2)
    private String uf;

    @Size(max = 20)
    private String telefone;

    @OneToOne(optional = false)
    @JoinColumn(name = "dono_id", nullable = false)
    private Usuario dono;

    @Column(nullable = false)
    private Integer capacidadePorHora = 3; // padrão: 3 atendimentos/hora

    protected Oficina() {}

    public Oficina(String nome, String endereco, String cidade, String uf, String telefone, Usuario dono) {
        this.nome = nome;
        this.endereco = endereco;
        this.cidade = cidade;
        this.uf = uf;
        this.telefone = telefone;
        this.dono = dono;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getCidade() { return cidade; }
    public String getUf() { return uf; }
    public String getTelefone() { return telefone; }
    public Usuario getDono() { return dono; }
    public Integer getCapacidadePorHora() { return capacidadePorHora; }

    public void setNome(String nome) { this.nome = nome; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public void setUf(String uf) { this.uf = uf; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public void setDono(Usuario dono) { this.dono = dono; }
    public void setCapacidadePorHora(Integer capacidadePorHora) { this.capacidadePorHora = capacidadePorHora; }
}
