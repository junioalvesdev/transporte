package com.ovg.transportes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "departamento")
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String sigla;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "permite_urgencia", nullable = false)
    private boolean permiteUrgencia = true;

    protected Departamento() {
    }

    public Long getId() {
        return id;
    }

    public String getSigla() {
        return sigla;
    }

    public String getNome() {
        return nome;
    }

    public boolean isPermiteUrgencia() {
        return permiteUrgencia;
    }
}
