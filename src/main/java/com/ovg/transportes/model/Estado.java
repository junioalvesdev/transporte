package com.ovg.transportes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "estado")
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_uf", nullable = false, unique = true)
    private Integer codigoUf;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(nullable = false, unique = true, length = 2)
    private String uf;

    @Column(nullable = false)
    private Integer regiao;

    protected Estado() {
    }

    public Long getId() {
        return id;
    }

    public Integer getCodigoUf() {
        return codigoUf;
    }

    public String getNome() {
        return nome;
    }

    public String getUf() {
        return uf;
    }

    public Integer getRegiao() {
        return regiao;
    }
}
