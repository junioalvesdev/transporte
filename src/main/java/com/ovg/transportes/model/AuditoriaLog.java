package com.ovg.transportes.model;

import com.ovg.transportes.model.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_log")
public class AuditoriaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String entidade;

    @Column(name = "entidade_id", nullable = false)
    private Long entidadeId;

    @Column(nullable = false, length = 40)
    private String acao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "dados_antes", columnDefinition = "json")
    private String dadosAntes;

    @Column(name = "dados_depois", columnDefinition = "json")
    private String dadosDepois;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    protected AuditoriaLog() {
    }

    public AuditoriaLog(String entidade, Long entidadeId, String acao, Usuario usuario, String dadosAntes, String dadosDepois) {
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.acao = acao;
        this.usuario = usuario;
        this.dadosAntes = dadosAntes;
        this.dadosDepois = dadosDepois;
    }

    public Long getId() {
        return id;
    }

    public String getEntidade() {
        return entidade;
    }

    public Long getEntidadeId() {
        return entidadeId;
    }

    public String getAcao() {
        return acao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getDadosAntes() {
        return dadosAntes;
    }

    public String getDadosDepois() {
        return dadosDepois;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
