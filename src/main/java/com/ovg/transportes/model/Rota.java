package com.ovg.transportes.model;

import com.ovg.transportes.model.Viagem;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "rota")
public class Rota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viagem_id", nullable = false)
    private Viagem viagem;

    @OneToMany(mappedBy = "rota", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<PontoRota> pontos = new ArrayList<>();

    protected Rota() {
    }

    public Rota(Viagem viagem) {
        this.viagem = viagem;
    }

    public void adicionarPonto(PontoRota ponto) {
        pontos.add(ponto);
    }

    public Optional<PontoRota> encontrarPontoPorCidade(Long cidadeId) {
        return pontos.stream()
            .filter(ponto -> ponto.getCidade().getId().equals(cidadeId))
            .findFirst();
    }

    public Long getId() {
        return id;
    }

    public Viagem getViagem() {
        return viagem;
    }

    public List<PontoRota> getPontos() {
        return pontos;
    }
}
