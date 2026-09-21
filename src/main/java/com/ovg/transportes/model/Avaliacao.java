package com.ovg.transportes.model;

import com.ovg.transportes.model.ViagemParticipante;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "avaliacao")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viagem_participante_id", nullable = false, unique = true)
    private ViagemParticipante viagemParticipante;

    @Column(nullable = false)
    private Integer nota;

    @Column(length = 100)
    private String categoria;

    @Column(length = 500)
    private String comentario;

    protected Avaliacao() {
    }

    public Avaliacao(ViagemParticipante viagemParticipante, Integer nota, String categoria, String comentario) {
        this.viagemParticipante = viagemParticipante;
        this.nota = nota;
        this.categoria = categoria;
        this.comentario = comentario;
    }

    public Long getId() {
        return id;
    }

    public ViagemParticipante getViagemParticipante() {
        return viagemParticipante;
    }

    public Integer getNota() {
        return nota;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getComentario() {
        return comentario;
    }
}
