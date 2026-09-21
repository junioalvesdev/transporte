package com.ovg.transportes.model;

import com.ovg.transportes.model.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoFeedback tipo;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_tratamento", nullable = false, length = 20)
    private StatusTratamento statusTratamento = StatusTratamento.ABERTO;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    protected Feedback() {
    }

    public Feedback(Usuario usuario, TipoFeedback tipo, String descricao) {
        this.usuario = usuario;
        this.tipo = tipo;
        this.descricao = descricao;
    }

    public void marcarTratamento(StatusTratamento status) {
        this.statusTratamento = status;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public TipoFeedback getTipo() {
        return tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public StatusTratamento getStatusTratamento() {
        return statusTratamento;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
