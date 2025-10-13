package br.com.oficinas.gestaooficinas.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "itens_reparo")
public class ItemReparo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) // dono do item = cliente
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaItem categoria;

    @NotBlank
    @Column(nullable = false, length = 300)
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    protected ItemReparo() {}

    public ItemReparo(Usuario cliente, CategoriaItem categoria, String descricao) {
        this.cliente = cliente;
        this.categoria = categoria;
        this.descricao = descricao;
    }

    public Long getId() { return id; }
    public Usuario getCliente() { return cliente; }
    public CategoriaItem getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }

    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setCategoria(CategoriaItem categoria) { this.categoria = categoria; }
}
