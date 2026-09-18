package com.ovg.transportes.repository;

import com.ovg.transportes.model.ViagemParticipante;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ViagemParticipanteRepository extends JpaRepository<ViagemParticipante, Long> {

    Optional<ViagemParticipante> findBySolicitacaoId(Long solicitacaoId);
}
