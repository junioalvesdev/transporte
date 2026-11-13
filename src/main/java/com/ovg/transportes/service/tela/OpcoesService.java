package com.ovg.transportes.service.tela;

import com.ovg.transportes.common.tela.Opcao;
import com.ovg.transportes.dto.veiculo.VeiculoResponseDTO;
import com.ovg.transportes.model.StatusViagem;
import com.ovg.transportes.service.LocalAdministrativoService;
import com.ovg.transportes.service.MotoristaService;
import com.ovg.transportes.service.VeiculoService;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Listas de opcoes usadas nos &lt;select&gt; de varias paginas (veiculos,
 * motoristas, unidades, status da viagem), ja no formato Opcao.
 */
@Service
public class OpcoesService {

    private final VeiculoService veiculoService;
    private final MotoristaService motoristaService;
    private final LocalAdministrativoService localAdministrativoService;

    public OpcoesService(
        VeiculoService veiculoService,
        MotoristaService motoristaService,
        LocalAdministrativoService localAdministrativoService
    ) {
        this.veiculoService = veiculoService;
        this.motoristaService = motoristaService;
        this.localAdministrativoService = localAdministrativoService;
    }

    // veiculos ativos agrupados por tipo (um <optgroup> por tipo), no lugar do
    // filtro por tipo que era feito em JS
    public Map<String, List<Opcao>> veiculosPorTipo() {
        return veiculoService.listarAtivos(PageRequest.of(0, 1000)).conteudo().stream()
            .collect(Collectors.groupingBy(
                VeiculoResponseDTO::tipoVeiculo,
                TreeMap::new,
                Collectors.mapping(
                    veiculo -> Opcao.de(veiculo.id(), "%s - %s (%d lugares)".formatted(veiculo.placa(), veiculo.modelo(), veiculo.capacidade())),
                    Collectors.toList())));
    }

    public List<Opcao> motoristas() {
        return motoristaService.listarAtivos(PageRequest.of(0, 1000)).conteudo().stream()
            .map(motorista -> Opcao.de(motorista.id(), motorista.nome()))
            .toList();
    }

    // valor = nome da unidade (a ordem de trafego procura a unidade pelo nome)
    public List<Opcao> unidadesPorNome() {
        return localAdministrativoService.listar().stream()
            .map(local -> Opcao.de(local.nome(), local.nome()))
            .toList();
    }

    // status com que a viagem pode nascer ao aprovar (padrao: ABERTA_PARA_APROVEITAMENTO)
    public List<Opcao> statusViagemParaAprovar() {
        return Arrays.stream(StatusViagem.values())
            .map(status -> new Opcao(status.name(), status.name(), status == StatusViagem.ABERTA_PARA_APROVEITAMENTO))
            .toList();
    }
}
