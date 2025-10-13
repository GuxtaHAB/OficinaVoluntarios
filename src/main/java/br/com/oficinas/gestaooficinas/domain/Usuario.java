package br.com.oficinas.gestaooficinas.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_email", columnNames = "email")
})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min = 2, max = 80)
    private String nome;

    @Email @NotBlank @Column(nullable = false)
    private String email;

    @NotBlank @Size(min = 8, max = 120) // hash vai ficar grande, mas ok
    @Column(name = "senha_hash", nullable = false, length = 120)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // Construtores
    protected Usuario() {}
    public Usuario(String nome, String email, String senhaHash, Role role) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.role = role;
    }

    // Getters/Setters
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public Role getRole() { return role; }

    public void setNome(String nome) { this.nome = nome; }
    public void setEmail(String email) { this.email = email; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public void setRole(Role role) { this.role = role; }
}
