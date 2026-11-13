package com.ovg.transportes.dto.tela.formularios;

import com.ovg.transportes.common.tela.Opcao;

import java.util.List;
import java.util.Map;

/** Opcoes do modal "Gerar Ordem de Trafego" (templates/fragmentos/modal-ordem-trafego.html). */
public record FormOrdemTrafegoDTO(
    Map<String, List<Opcao>> veiculosPorTipo,
    List<Opcao> motoristas,
    List<Opcao> unidades
) {
}
