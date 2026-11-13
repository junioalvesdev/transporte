package com.ovg.transportes.service.tela;

import com.ovg.transportes.common.tela.ValidadorFormulario;
import com.ovg.transportes.dto.comum.PaginaDTO;
import com.ovg.transportes.dto.motorista.MotoristaRequestDTO;
import com.ovg.transportes.dto.motorista.MotoristaResponseDTO;
import com.ovg.transportes.dto.tela.motoristas.LinhaMotoristaDTO;
import com.ovg.transportes.dto.tela.motoristas.ListaMotoristasDTO;
import com.ovg.transportes.service.MotoristaService;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * Regras da pagina "Motoristas": monta a lista pronta pra exibir e executa as
 * acoes (cadastrar, editar, inativar) usando o MotoristaService.
 */
@Service
public class MotoristasService {

    private static final int POR_PAGINA = 10;

    private final MotoristaService motoristaService;
    private final ValidadorFormulario validador;

    public MotoristasService(MotoristaService motoristaService, ValidadorFormulario validador) {
        this.motoristaService = motoristaService;
        this.validador = validador;
    }

    public ListaMotoristasDTO montarLista(int pagina) {
        PaginaDTO<MotoristaResponseDTO> resultado = motoristaService.listarAtivos(PageRequest.of(Math.max(pagina, 0), POR_PAGINA));
        return new ListaMotoristasDTO(
            resultado.conteudo().stream().map(MotoristasService::paraLinha).toList(),
            LayoutService.paginacao(resultado, "/motoristas?pagina="),
            resultado.paginaAtual()
        );
    }

    public void cadastrar(MotoristaRequestDTO requisicao) {
        validador.validar(requisicao);
        motoristaService.cadastrar(requisicao);
    }

    public void atualizar(Long id, MotoristaRequestDTO requisicao) {
        validador.validar(requisicao);
        motoristaService.atualizar(id, requisicao);
    }

    public void inativar(Long id) {
        // soft delete: o motorista some das listas, mas as viagens antigas continuam com ele
        motoristaService.inativar(id);
    }

    private static LinhaMotoristaDTO paraLinha(MotoristaResponseDTO motorista) {
        String modalEditarId = "editar-motorista-" + motorista.id();
        String modalInativarId = "inativar-motorista-" + motorista.id();
        return new LinhaMotoristaDTO(
            motorista.id(),
            motorista.nome(),
            motorista.telefone() == null ? "" : motorista.telefone(),
            "Editar motorista — " + motorista.nome(),
            modalEditarId,
            "#" + modalEditarId,
            modalInativarId,
            "#" + modalInativarId,
            "/motoristas/" + motorista.id(),
            "/motoristas/" + motorista.id() + "/inativar"
        );
    }
}
