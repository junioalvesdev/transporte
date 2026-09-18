package com.ovg.transportes.repository;

import com.ovg.transportes.model.Motorista;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MotoristaRepository extends JpaRepository<Motorista, Long> {

    // "do ultimo ao primeiro": id decrescente = o motorista cadastrado mais recentemente aparece primeiro.
    Page<Motorista> findByAtivoTrueOrderByIdDesc(Pageable pageable);
}
