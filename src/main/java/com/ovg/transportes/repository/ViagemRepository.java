package com.ovg.transportes.repository;

import com.ovg.transportes.model.StatusViagem;
import com.ovg.transportes.model.Viagem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ViagemRepository extends JpaRepository<Viagem, Long> {

    // Trava a linha da viagem ate o fim da transacao (SELECT ... FOR UPDATE) —
    // usada antes de checar/ocupar vaga, pra duas pessoas nao conseguirem
    // "passar" pela checagem de vaga disponivel ao mesmo tempo e as duas
    // ganharem a ultima vaga (a segunda fica esperando a primeira transacao
    // terminar, e ai ve a ocupacao ja atualizada).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Viagem v WHERE v.id = :id")
    Optional<Viagem> buscarPorIdComTravaDeEscrita(@Param("id") Long id);

    // Busca simplificada por dia (decidido apos feedback de uso): a tela
    // "Viagens disponiveis" lista tudo que esta aberto naquele dia, sem exigir
    // que o usuario ja saiba origem/destino de antemao — cada card mostra sua
    // propria rota, e quem procura decide se serve. Inclui LOTADA tambem —
    // sem isso, a viagem some da lista assim que a ultima vaga e ocupada,
    // mesmo pra quem acabou de pegar essa vaga (o service filtra na sequencia
    // quem realmente pode ver: tem vaga OU ja e participante).
    @Query("""
        SELECT v FROM Viagem v
        WHERE v.status IN (
            com.ovg.transportes.model.StatusViagem.ABERTA_PARA_APROVEITAMENTO,
            com.ovg.transportes.model.StatusViagem.LOTADA
        )
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

    // Calendario: JOIN FETCH veiculo/motorista pra nao virar uma consulta
    // separada por viagem (essas duas relacoes sao usadas SEMPRE, pra toda
    // viagem, ao montar o card/pilula) — participantes e rota continuam lazy,
    // mas o default_batch_fetch_size (application.yml) agrupa essas em lotes
    // em vez de uma consulta por viagem.
    @Query("""
        SELECT v FROM Viagem v
        JOIN FETCH v.veiculo
        JOIN FETCH v.motorista
        WHERE v.dataHoraSaida BETWEEN :inicio AND :fim
        """)
    List<Viagem> findByDataHoraSaidaBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
