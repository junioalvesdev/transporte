package com.ovg.transportes.repository;

import com.ovg.transportes.model.AuditoriaLog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, Long> {

    List<AuditoriaLog> findByEntidadeAndEntidadeIdOrderByCriadoEmDesc(String entidade, Long entidadeId);
}
