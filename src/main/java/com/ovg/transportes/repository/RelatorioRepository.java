package com.ovg.transportes.repository;

import com.ovg.transportes.dto.ContagemDTO;
import com.ovg.transportes.model.Viagem;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;
import java.util.List;

public interface RelatorioRepository extends Repository<Viagem, Long> {

    @Query("""
        SELECT new com.ovg.transportes.dto.ContagemDTO(m.nome, COUNT(v))
        FROM Viagem v JOIN v.motorista m
        WHERE v.dataHoraSaida BETWEEN :inicio AND :fim
        GROUP BY m.nome
        ORDER BY COUNT(v) DESC
        """)
    List<ContagemDTO> contarPorMotorista(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("""
        SELECT new com.ovg.transportes.dto.ContagemDTO(tv.nome, COUNT(v))
        FROM Viagem v JOIN v.veiculo ve JOIN ve.tipoVeiculo tv
        WHERE v.dataHoraSaida BETWEEN :inicio AND :fim
        GROUP BY tv.nome
        ORDER BY COUNT(v) DESC
        """)
    List<ContagemDTO> contarPorTipoVeiculo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("""
        SELECT new com.ovg.transportes.dto.ContagemDTO(CAST(v.status AS string), COUNT(v))
        FROM Viagem v
        WHERE v.dataHoraSaida BETWEEN :inicio AND :fim
        GROUP BY v.status
        ORDER BY COUNT(v) DESC
        """)
    List<ContagemDTO> contarPorStatus(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query(
        value = "SELECT DATE_FORMAT(v.data_hora_saida, '%Y-%m') AS rotulo, COUNT(*) AS quantidade "
            + "FROM viagem v WHERE v.data_hora_saida BETWEEN :inicio AND :fim "
            + "GROUP BY rotulo ORDER BY rotulo",
        nativeQuery = true)
    List<Object[]> contarPorMesBruto(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(v) FROM Viagem v WHERE v.dataHoraSaida BETWEEN :inicio AND :fim")
    long contarTotal(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
