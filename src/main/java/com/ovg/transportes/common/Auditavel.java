package com.ovg.transportes.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Colunas de auditoria basica (quem/quando criou e alterou por ultimo).
 * Toda entidade principal (Solicitacao, Viagem, Veiculo, Motorista) estende esta classe
 * em vez de repetir essas quatro colunas em cada uma.
 *
 * O preenchimento e automatico via Spring Data JPA Auditing (ver AuditoriaConfig),
 * que le o usuario autenticado do SecurityContext a cada insert/update.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditavel {

    @CreatedBy
    @Column(name = "criado_por", updatable = false, length = 100)
    private String criadoPor;

    @CreatedDate
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @LastModifiedBy
    @Column(name = "atualizado_por", length = 100)
    private String atualizadoPor;

    @LastModifiedDate
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public String getCriadoPor() {
        return criadoPor;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public String getAtualizadoPor() {
        return atualizadoPor;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
