package com.ovg.transportes.repository;

import com.ovg.transportes.model.ViagemParticipante;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ViagemParticipanteRepository extends JpaRepository<ViagemParticipante, Long> {

    Optional<ViagemParticipante> findBySolicitacaoId(Long solicitacaoId);

    // Versao "em lote" de findBySolicitacaoId, pra telas que listam varias
    // solicitacoes de uma vez (ex.: Lista de Reservas) — sem isso, cada linha
    // da lista dispara sua propria consulta pra achar a placa vinculada (N+1
    // que cresce junto com o tamanho da lista, ao contrario desta aqui).
    @Query("SELECT vp FROM ViagemParticipante vp JOIN FETCH vp.viagem v JOIN FETCH v.veiculo WHERE vp.solicitacao.id IN :solicitacaoIds")
    List<ViagemParticipante> findBySolicitacaoIdIn(@Param("solicitacaoIds") Collection<Long> solicitacaoIds);
}
