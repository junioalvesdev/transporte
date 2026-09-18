package com.ovg.transportes.repository;

import com.ovg.transportes.model.StatusViagem;
import com.ovg.transportes.model.Viagem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ViagemRepository extends JpaRepository<Viagem, Long> {

    // Busca simplificada por dia (decidido apos feedback de uso): a tela
    // "Viagens disponiveis" lista tudo que esta aberto naquele dia, sem exigir
    // que o usuario ja saiba origem/destino de antemao — cada card mostra sua
    // propria rota, e quem procura decide se serve.
    @Query("""
        SELECT v FROM Viagem v
        WHERE v.status = com.ovg.transportes.model.StatusViagem.ABERTA_PARA_APROVEITAMENTO
          AND v.dataHoraSaida BETWEEN :inicio AND :fim
        """)
    List<Viagem> buscarAbertasNoPeriodo(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    @Query("""
        SELECT COUNT(v) > 0 FROM Viagem v
        WHERE v.veiculo.id = :veiculoId
          AND v.status <> com.ovg.transportes.model.StatusViagem.CANCELADA
          AND v.dataHoraSaida < :fimProposto
          AND v.dataHoraChegadaEstimada > :inicioProposto
        """)
    boolean existeConflitoDeAgendaParaVeiculo(
        @Param("veiculoId") Long veiculoId,
        @Param("inicioProposto") LocalDateTime inicioProposto,
        @Param("fimProposto") LocalDateTime fimProposto
    );

    @Query("""
        SELECT COUNT(v) > 0 FROM Viagem v
        WHERE v.motorista.id = :motoristaId
          AND v.status <> com.ovg.transportes.model.StatusViagem.CANCELADA
          AND v.dataHoraSaida < :fimProposto
          AND v.dataHoraChegadaEstimada > :inicioProposto
        """)
    boolean existeConflitoDeAgendaParaMotorista(
        @Param("motoristaId") Long motoristaId,
        @Param("inicioProposto") LocalDateTime inicioProposto,
        @Param("fimProposto") LocalDateTime fimProposto
    );

    List<Viagem> findByDataHoraSaidaBetween(LocalDateTime inicio, LocalDateTime fim);
}
