package com.ovg.transportes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "perfil")
public class Perfil {

    public static final String FUNCIONARIO = "FUNCIONARIO";
    public static final String TRANSPORTE = "TRANSPORTE";
    public static final String ADMINISTRADOR = "ADMINISTRADOR";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    protected Perfil() {
    }

    public Perfil(String codigo) {
        this.codigo = codigo;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }
}
