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
@Table(name = "veiculo")
public class Veiculo extends Auditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @Column(nullable = false, length = 150)
    private String modelo;

    @Column(nullable = false)
    private Integer capacidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_veiculo_id", nullable = false)
    private TipoVeiculo tipoVeiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_id")
    private LocalAdministrativo local;

    @Column(nullable = false)
    private boolean ativo = true;

    protected Veiculo() {
    }

    public Veiculo(String placa, String modelo, Integer capacidade, TipoVeiculo tipoVeiculo, LocalAdministrativo local) {
        this.placa = placa;
        this.modelo = modelo;
        this.capacidade = capacidade;
        this.tipoVeiculo = tipoVeiculo;
        this.local = local;
    }

    public void atualizar(String modelo, Integer capacidade, TipoVeiculo tipoVeiculo, LocalAdministrativo local) {
        this.modelo = modelo;
        this.capacidade = capacidade;
        this.tipoVeiculo = tipoVeiculo;
        this.local = local;
    }

    public void inativar() {
        this.ativo = false;
    }

    public Long getId() {
        return id;
    }

    public String getPlaca() {
        return placa;
    }

    public String getModelo() {
        return modelo;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public TipoVeiculo getTipoVeiculo() {
        return tipoVeiculo;
    }

    public LocalAdministrativo getLocal() {
        return local;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
