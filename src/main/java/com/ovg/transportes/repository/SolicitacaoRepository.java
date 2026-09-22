package com.ovg.transportes.repository;

import com.ovg.transportes.model.Solicitacao;
import com.ovg.transportes.model.StatusSolicitacao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    // "do ultimo ao primeiro": id decrescente = a solicitacao mais recente
    // aparece primeiro, independente da data desejada da viagem.
    Page<Solicitacao> findBySolicitanteIdOrderByIdDesc(Long solicitanteId, Pageable pageable);

    Page<Solicitacao> findByStatusOrderByIdDesc(StatusSolicitacao status, Pageable pageable);

    // "Lista de Reservas": so o mes atual (por data_hora_desejada, o mesmo
    // campo que agrupa a tela) — a tabela tem dezenas de milhares de linhas
    // reais migradas do legado, e buscar tudo (mesmo paginado) fica pesado
    // sem necessidade, ja que a tela so mostra um mes de cada vez.
    Page<Solicitacao> findByDataHoraDesejadaBetweenOrderByIdDesc(
        LocalDateTime inicioMes,
        LocalDateTime fimMes,
        Pageable pageable
    );

    // Calendario: solicitacoes sem viagem vinculada ainda (pendente/reprovada/
    // cancelada) no periodo visivel — aprovada/atendida ja aparecem no
    // calendario atraves da propria viagem, entao ficam de fora daqui pra nao
    // duplicar. TRANSPORTE ve de todo mundo; qualquer outro usuario, so as suas.
    List<Solicitacao> findByDataHoraDesejadaBetweenAndStatusInOrderByDataHoraDesejada(
        LocalDateTime inicio,
        LocalDateTime fim,
        List<StatusSolicitacao> status
    );

    List<Solicitacao> findBySolicitanteIdAndDataHoraDesejadaBetweenAndStatusInOrderByDataHoraDesejada(
        Long solicitanteId,
        LocalDateTime inicio,
        LocalDateTime fim,
        List<StatusSolicitacao> status
    );
}
