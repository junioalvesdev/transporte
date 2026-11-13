package com.ovg.transportes.dto.tela.formularios;

import com.ovg.transportes.common.tela.Opcao;

import java.util.List;
import java.util.Map;

/**
 * Opcoes do formulario de aprovar uma solicitacao (templates/fragmentos/form-aprovar.html).
 * Iguais pra todas as solicitacoes da tela, entao vem uma vez so por pagina.
 */
public record FormAprovarDTO(
    Map<String, List<Opcao>> veiculosPorTipo,   // um <optgroup> por tipo
    List<Opcao> motoristas,
    List<Opcao> statusViagem                    // null esconde o campo (a viagem nasce com o status padrao)
) {
}
