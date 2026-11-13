package com.ovg.transportes.repository;

import com.ovg.transportes.model.Cidade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CidadeRepository extends JpaRepository<Cidade, Long> {

    // Traz o estado junto (JOIN FETCH) porque o nome da cidade sempre e exibido com a UF.
    @Query("SELECT c FROM Cidade c JOIN FETCH c.estado WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Cidade> buscarPorNome(@Param("nome") String nome);

    // lista completa pro datalist do formulario de nova reserva (Lista de Reservas)
    @Query("SELECT c FROM Cidade c JOIN FETCH c.estado ORDER BY c.nome")
    List<Cidade> listarTodasComEstado();

    @Query("SELECT c FROM Cidade c JOIN FETCH c.estado e WHERE LOWER(c.nome) = LOWER(:nome) AND e.uf = :uf")
    Optional<Cidade> buscarPorNomeEUf(@Param("nome") String nome, @Param("uf") String uf);
}
