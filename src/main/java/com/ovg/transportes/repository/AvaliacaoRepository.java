package com.ovg.transportes.repository;

import com.ovg.transportes.model.Avaliacao;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    boolean existsByViagemParticipanteId(Long viagemParticipanteId);
}
