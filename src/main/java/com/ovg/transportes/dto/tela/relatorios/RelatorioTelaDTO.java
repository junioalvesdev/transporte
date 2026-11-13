package com.ovg.transportes.dto.tela.relatorios;

import com.ovg.transportes.dto.relatorio.ContagemDTO;

import java.util.List;

/** Tudo que a pagina de relatorios mostra (vai pro HTML como ${pagina}). */
public record RelatorioTelaDTO(
    String inicio,              // valor do campo "De" (yyyy-MM-dd)
    String fim,                 // valor do campo "Ate"
    long totalViagens,
    List<BarraGraficoDTO> porMes,
    List<ContagemDTO> porStatus,
    List<ContagemDTO> porMotorista,
    List<ContagemDTO> porTipoVeiculo
) {
}
