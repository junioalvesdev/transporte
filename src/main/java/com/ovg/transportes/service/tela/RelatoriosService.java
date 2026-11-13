package com.ovg.transportes.service.tela;

import com.ovg.transportes.dto.relatorio.ContagemDTO;
import com.ovg.transportes.dto.relatorio.RelatorioMensalDTO;
import com.ovg.transportes.dto.tela.relatorios.BarraGraficoDTO;
import com.ovg.transportes.dto.tela.relatorios.RelatorioTelaDTO;
import com.ovg.transportes.service.RelatorioService;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Regras da pagina "Relatorios": gera o relatorio do periodo (RelatorioService)
 * e prepara o grafico por mes como barras prontas pra exibir.
 */
@Service
public class RelatoriosService {

    private final RelatorioService relatorioService;

    public RelatoriosService(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    // sem datas: o ultimo ano, ate hoje (mesmo padrao da tela antiga)
    public RelatorioTelaDTO montar(LocalDate inicio, LocalDate fim) {
        LocalDate ate = fim != null ? fim : LocalDate.now();
        LocalDate de = inicio != null ? inicio : ate.minusYears(1);
        RelatorioMensalDTO relatorio = relatorioService.gerar(de.atStartOfDay(), ate.plusDays(1).atStartOfDay());

        return new RelatorioTelaDTO(
            de.toString(),
            ate.toString(),
            relatorio.totalViagens(),
            barras(relatorio.porMes()),
            relatorio.porStatus(),
            relatorio.porMotorista(),
            relatorio.porTipoVeiculo()
        );
    }

    // largura de cada barra em % do maior mes (o maior ocupa a linha toda)
    private static List<BarraGraficoDTO> barras(List<ContagemDTO> porMes) {
        long maior = porMes.stream().mapToLong(ContagemDTO::quantidade).max().orElse(0);
        return porMes.stream()
            .map(mes -> new BarraGraficoDTO(
                mes.rotulo(),
                mes.quantidade(),
                "width: " + (maior == 0 ? 0 : Math.round(mes.quantidade() * 100.0 / maior)) + "%"))
            .toList();
    }
}
