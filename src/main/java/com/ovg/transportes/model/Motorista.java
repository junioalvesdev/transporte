package com.ovg.transportes.model;

import com.ovg.transportes.common.Auditavel;
import com.ovg.transportes.model.LocalAdministrativo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "motorista")
public class Motorista extends Auditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 30)
    private String telefone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_id")
    private LocalAdministrativo local;

    @Column(nullable = false)
    private boolean ativo = true;

    protected Motorista() {
    }

    public Motorista(String nome, String telefone, LocalAdministrativo local) {
        this.nome = nome;
        this.telefone = telefone;
        this.local = local;
    }

    public void atualizar(String nome, String telefone, LocalAdministrativo local) {
        this.nome = nome;
        this.telefone = telefone;
        this.local = local;
    }

    public void inativar() {
        this.ativo = false;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public LocalAdministrativo getLocal() {
        return local;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
