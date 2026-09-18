package com.ovg.transportes.repository;

import com.ovg.transportes.model.Veiculo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    Optional<Veiculo> findByPlacaIgnoreCase(String placa);

    // "do ultimo ao primeiro": id decrescente = o veiculo cadastrado mais recentemente aparece primeiro.
    Page<Veiculo> findByAtivoTrueOrderByIdDesc(Pageable pageable);
}
