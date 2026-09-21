package com.ovg.transportes.service;

import com.ovg.transportes.common.NegocioException;
import com.ovg.transportes.common.RecursoNaoEncontradoException;
import com.ovg.transportes.dto.PaginaDTO;
import com.ovg.transportes.dto.VeiculoRequestDTO;
import com.ovg.transportes.dto.VeiculoResponseDTO;
import com.ovg.transportes.model.LocalAdministrativo;
import com.ovg.transportes.model.TipoVeiculo;
import com.ovg.transportes.model.Veiculo;
import com.ovg.transportes.repository.LocalAdministrativoRepository;
import com.ovg.transportes.repository.TipoVeiculoRepository;
import com.ovg.transportes.repository.VeiculoRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final TipoVeiculoRepository tipoVeiculoRepository;
    private final LocalAdministrativoRepository localAdministrativoRepository;

    public VeiculoService(
        VeiculoRepository veiculoRepository,
        TipoVeiculoRepository tipoVeiculoRepository,
        LocalAdministrativoRepository localAdministrativoRepository
    ) {
        this.veiculoRepository = veiculoRepository;
        this.tipoVeiculoRepository = tipoVeiculoRepository;
        this.localAdministrativoRepository = localAdministrativoRepository;
    }

    public PaginaDTO<VeiculoResponseDTO> listarAtivos(Pageable pageable) {
        return PaginaDTO.de(veiculoRepository.findByAtivoTrueOrderByIdDesc(pageable), VeiculoResponseDTO::de);
    }

    @Transactional
    public VeiculoResponseDTO cadastrar(VeiculoRequestDTO requisicao) {
        veiculoRepository.findByPlacaIgnoreCase(requisicao.placa()).ifPresent(existente -> {
            throw new NegocioException("Ja existe um veiculo cadastrado com a placa " + requisicao.placa());
        });

        TipoVeiculo tipoVeiculo = buscarTipoVeiculo(requisicao.tipoVeiculoId());
        LocalAdministrativo local = buscarLocalSeInformado(requisicao.localId());

        Veiculo veiculo = new Veiculo(requisicao.placa(), requisicao.modelo(), requisicao.capacidade(), tipoVeiculo, local);
        return VeiculoResponseDTO.de(veiculoRepository.save(veiculo));
    }

    @Transactional
    public VeiculoResponseDTO atualizar(Long id, VeiculoRequestDTO requisicao) {
        Veiculo veiculo = buscarPorId(id);
        TipoVeiculo tipoVeiculo = buscarTipoVeiculo(requisicao.tipoVeiculoId());
        LocalAdministrativo local = buscarLocalSeInformado(requisicao.localId());

        veiculo.atualizar(requisicao.modelo(), requisicao.capacidade(), tipoVeiculo, local);
        return VeiculoResponseDTO.de(veiculo);
    }

    @Transactional
    public void inativar(Long id) {
        // soft delete: nunca DELETE fisico, ao contrario do legado (excluirVeiculo.php),
        // que apagava a linha mesmo que houvesse viagens historicas vinculadas.
        buscarPorId(id).inativar();
    }

    private Veiculo buscarPorId(Long id) {
        return veiculoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo nao encontrado: " + id));
    }

    private TipoVeiculo buscarTipoVeiculo(Long id) {
        return tipoVeiculoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de veiculo nao encontrado: " + id));
    }

    private LocalAdministrativo buscarLocalSeInformado(Long id) {
        if (id == null) {
            return null;
        }
        return localAdministrativoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Local administrativo nao encontrado: " + id));
    }
}
