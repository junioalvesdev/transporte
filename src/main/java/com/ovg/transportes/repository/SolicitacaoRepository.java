package com.ovg.transportes.repository;

import com.ovg.transportes.model.Solicitacao;
import com.ovg.transportes.model.StatusSolicitacao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    // "do ultimo ao primeiro": id decrescente = a solicitacao mais recente
    // aparece primeiro, independente da data desejada da viagem.
    Page<Solicitacao> findBySolicitanteIdOrderByIdDesc(Long solicitanteId, Pageable pageable);

    Page<Solicitacao> findByStatusOrderByIdDesc(StatusSolicitacao status, Pageable pageable);
}
