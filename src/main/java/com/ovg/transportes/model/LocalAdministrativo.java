package com.ovg.transportes.model;

import com.ovg.transportes.model.Cidade;

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
@Table(name = "local_administrativo")
public class LocalAdministrativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nome;

    // cidade "de casa" da unidade (ex.: SEDE -> Goiania/GO) — e o que permite
    // o usuario escolher so a unidade no formulario de solicitacao e ainda
    // assim alimentar o motor de compatibilidade de rota, que so entende cidade.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cidade_id", nullable = false)
    private Cidade cidade;

    protected LocalAdministrativo() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Cidade getCidade() {
        return cidade;
    }
}
