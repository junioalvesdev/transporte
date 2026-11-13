package com.ovg.transportes.service.tela;

import com.ovg.transportes.common.tela.Formatos;
import com.ovg.transportes.common.tela.Opcao;
import com.ovg.transportes.common.tela.ValidadorFormulario;
import com.ovg.transportes.dto.comum.PaginaDTO;
import com.ovg.transportes.dto.tela.veiculos.LinhaVeiculoDTO;
import com.ovg.transportes.dto.tela.veiculos.ListaVeiculosDTO;
import com.ovg.transportes.dto.veiculo.TipoVeiculoResponseDTO;
import com.ovg.transportes.dto.veiculo.VeiculoRequestDTO;
import com.ovg.transportes.dto.veiculo.VeiculoResponseDTO;
import com.ovg.transportes.service.TipoVeiculoService;
import com.ovg.transportes.service.VeiculoService;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Regras da pagina "Veiculos": monta a lista pronta pra exibir e executa as
 * acoes (cadastrar, editar, inativar) usando o VeiculoService.
 */
@Service
public class VeiculosService {

    private static final int POR_PAGINA = 10;

    private final VeiculoService veiculoService;
    private final TipoVeiculoService tipoVeiculoService;
    private final ValidadorFormulario validador;

    public VeiculosService(VeiculoService veiculoService, TipoVeiculoService tipoVeiculoService, ValidadorFormulario validador) {
        this.veiculoService = veiculoService;
        this.tipoVeiculoService = tipoVeiculoService;
        this.validador = validador;
    }

    public ListaVeiculosDTO montarLista(int pagina) {
        // "do ultimo ao primeiro" ja vem pronto do banco (ORDER BY id DESC)
        PaginaDTO<VeiculoResponseDTO> resultado = veiculoService.listarAtivos(PageRequest.of(Math.max(pagina, 0), POR_PAGINA));
        List<TipoVeiculoResponseDTO> tipos = tipoVeiculoService.listar();
        return new ListaVeiculosDTO(
            resultado.conteudo().stream().map(veiculo -> paraLinha(veiculo, tipos)).toList(),
            LayoutService.paginacao(resultado, "/veiculos?pagina="),
            opcoesDeTipo(tipos, null),
            resultado.paginaAtual()
        );
    }

    public void cadastrar(VeiculoRequestDTO requisicao) {
        validador.validar(requisicao);
        veiculoService.cadastrar(requisicao);
    }

    public void atualizar(Long id, VeiculoRequestDTO requisicao) {
        validador.validar(requisicao);
        veiculoService.atualizar(id, requisicao);
    }

    public void inativar(Long id) {
        // soft delete: o veiculo some das listas, mas as viagens antigas continuam com ele
        veiculoService.inativar(id);
    }

    private static LinhaVeiculoDTO paraLinha(VeiculoResponseDTO veiculo, List<TipoVeiculoResponseDTO> tipos) {
        String modalEditarId = "editar-veiculo-" + veiculo.id();
        String modalInativarId = "inativar-veiculo-" + veiculo.id();
        return new LinhaVeiculoDTO(
            veiculo.id(),
            veiculo.placa(),
            veiculo.modelo(),
            veiculo.capacidade(),
            veiculo.tipoVeiculo(),
            Formatos.iconeTipoVeiculo(veiculo.tipoVeiculo()) + " me-1 text-muted",
            "Editar veiculo — " + veiculo.placa(),
            modalEditarId,
            "#" + modalEditarId,
            modalInativarId,
            "#" + modalInativarId,
            "/veiculos/" + veiculo.id(),
            "/veiculos/" + veiculo.id() + "/inativar",
            opcoesDeTipo(tipos, veiculo.tipoVeiculo())
        );
    }

    private static List<Opcao> opcoesDeTipo(List<TipoVeiculoResponseDTO> tipos, String tipoAtual) {
        return tipos.stream()
            .map(tipo -> new Opcao(String.valueOf(tipo.id()), tipo.nome(), tipo.nome().equals(tipoAtual)))
            .toList();
    }
}
