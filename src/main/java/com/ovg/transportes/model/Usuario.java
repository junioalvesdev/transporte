package com.ovg.transportes.model;

import com.ovg.transportes.model.Departamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

/**
 * Cache local do funcionario autenticado via AD (ver com.ovg.transportes.security).
 * Nao guarda senha: a identidade e confirmada no bind LDAP, esta tabela so guarda
 * os dados de perfil/permissao usados pela aplicacao.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_ad", nullable = false, unique = true, length = 100)
    private String loginAd;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 30)
    private String telefone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuario_perfil",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "perfil_id")
    )
    private Set<Perfil> perfis = new HashSet<>();

    @Column(nullable = false)
    private boolean ativo = true;

    protected Usuario() {
    }

    public boolean temPerfil(String codigoPerfil) {
        return perfis.stream()
            .anyMatch(perfil -> perfil.getCodigo().equals(codigoPerfil));
    }

    public Long getId() {
        return id;
    }

    public String getLoginAd() {
        return loginAd;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public Set<Perfil> getPerfis() {
        return perfis;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
