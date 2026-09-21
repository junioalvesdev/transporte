package com.ovg.transportes.repository;

import com.ovg.transportes.model.LocalAdministrativo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LocalAdministrativoRepository extends JpaRepository<LocalAdministrativo, Long> {

    // Traz cidade e estado junto: o nome da unidade sempre e exibido com a cidade completa.
    @Query("SELECT l FROM LocalAdministrativo l JOIN FETCH l.cidade c JOIN FETCH c.estado")
    List<LocalAdministrativo> listarComCidade();
}
