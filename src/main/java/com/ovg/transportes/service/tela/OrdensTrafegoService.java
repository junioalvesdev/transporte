package com.ovg.transportes.service.tela;

import com.ovg.transportes.common.tela.ValidadorFormulario;
import com.ovg.transportes.dto.ordemtrafego.OrdemTrafegoRequestDTO;
import com.ovg.transportes.dto.tela.formularios.FormOrdemTrafegoDTO;
import com.ovg.transportes.service.OrdemTrafegoService;

import org.springframework.stereotype.Service;

/**
 * Modal "Gerar Ordem de Trafego" (Lista de Reservas e Calendario): opcoes do
 * formulario e geracao do PDF (OrdemTrafegoService).
 */
@Service
public class OrdensTrafegoService {

    private final OrdemTrafegoService ordemTrafegoService;
    private final OpcoesService opcoesService;
    private final ValidadorFormulario validador;

    public OrdensTrafegoService(OrdemTrafegoService ordemTrafegoService, OpcoesService opcoesService, ValidadorFormulario validador) {
        this.ordemTrafegoService = ordemTrafegoService;
        this.opcoesService = opcoesService;
        this.validador = validador;
    }

    public FormOrdemTrafegoDTO formulario() {
        return new FormOrdemTrafegoDTO(opcoesService.veiculosPorTipo(), opcoesService.motoristas(), opcoesService.unidadesPorNome());
    }

    public byte[] gerarPdf(OrdemTrafegoRequestDTO requisicao) {
        validador.validar(requisicao);
        return ordemTrafegoService.gerarPdf(requisicao);
    }
}
