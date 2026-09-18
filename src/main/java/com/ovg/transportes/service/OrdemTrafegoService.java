package com.ovg.transportes.service;

import com.ovg.transportes.common.RecursoNaoEncontradoException;
import com.ovg.transportes.dto.OrdemTrafegoRequestDTO;
import com.ovg.transportes.model.Motorista;
import com.ovg.transportes.model.Veiculo;
import com.ovg.transportes.repository.MotoristaRepository;
import com.ovg.transportes.repository.VeiculoRepository;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Gera a "Ordem de Trafego" (Relatorio Diario de Movimento de Viatura - RMV):
 * o mesmo documento em PDF que o Departamento de Transportes usava no sistema
 * legado pra autorizar motorista+veiculo numa data. Pagina 1 tem o cabecalho
 * completo (logo, dados da viagem, equipamento obrigatorio); pagina 2 e so
 * mais espaco de tabela, sem repetir o cabecalho — igual ao documento original.
 * A tabela de log (paradas/odometro/combustivel) fica em branco pro motorista
 * preencher a mao, ja que o sistema novo nao registra esses dados.
 *
 * As imagens (banner institucional e diagrama do veiculo) sao opcionais:
 * ficam em src/main/resources/img/ (fora de static/, entao nao viram URL
 * publica) e sao embutidas como base64 SE existirem no classpath. Isso
 * permite manter esses arquivos fora do git (institucionais) sem quebrar a
 * geracao do PDF — se o arquivo nao existir, o espaco da imagem so fica em branco.
 */
@Service
@Transactional(readOnly = true)
public class OrdemTrafegoService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final int LINHAS_TABELA_PAGINA_1 = 15;
    private static final int LINHAS_TABELA_PAGINA_2 = 26;
    private static final String CAMINHO_BANNER = "/img/assinatura2.png";

    private final VeiculoRepository veiculoRepository;
    private final MotoristaRepository motoristaRepository;

    public OrdemTrafegoService(VeiculoRepository veiculoRepository, MotoristaRepository motoristaRepository) {
        this.veiculoRepository = veiculoRepository;
        this.motoristaRepository = motoristaRepository;
    }

    public byte[] gerarPdf(OrdemTrafegoRequestDTO requisicao) {
        Veiculo veiculo = veiculoRepository.findById(requisicao.veiculoId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo nao encontrado: " + requisicao.veiculoId()));
        Motorista motorista = motoristaRepository.findById(requisicao.motoristaId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Motorista nao encontrado: " + requisicao.motoristaId()));

        String html = montarHtml(requisicao, veiculo, motorista);
        return renderizarPdf(html);
    }

    private String montarHtml(OrdemTrafegoRequestDTO requisicao, Veiculo veiculo, Motorista motorista) {
        String unidade = valorOu(requisicao.unidade(), veiculo.getLocal() != null ? veiculo.getLocal().getNome() : "");
        String autorizacao = "SEDE".equalsIgnoreCase(unidade) ? "GALT" : unidade;
        String periodo = formatarPeriodo(requisicao.data(), requisicao.dataFim());
        String dataHoraGeracao = LocalDateTime.now().format(FORMATO_DATA_HORA);

        String tagBanner = tagImagemOpcional(CAMINHO_BANNER, "width:330px", "OVG - Sistema do Transporte");
        String tagIconeVeiculo = tagImagemOpcional(caminhoIconeVeiculo(veiculo.getTipoVeiculo().getNome()), "width:260px", "diagrama do veiculo");

        String pagina1 = montarCabecalhoPagina(periodo, veiculo, motorista, autorizacao, unidade, tagBanner, tagIconeVeiculo)
            + montarTabelaLog(LINHAS_TABELA_PAGINA_1);
        // pagina 2: so mais tabela, sem repetir cabecalho/logo/dados — igual ao documento original
        String pagina2 = "<div style=\"page-break-before: always;\"></div>" + montarTabelaLog(LINHAS_TABELA_PAGINA_2);

        return """
            <html>
            <head>
            <style>
                @page {
                    size: A4 landscape;
                    margin: 12mm 14mm 18mm 14mm;
                    @bottom-right { content: "%s   Pagina " counter(page) " de " counter(pages); font-size: 9px; }
                }
                body { font-family: Helvetica, Arial, sans-serif; font-size: 11px; color: #000; }
                h1 { font-size: 14px; text-align: center; margin: 0; }
                .topo { display: table; width: 100%%; margin-bottom: 6px; }
                .topo-linha { display: table-row; }
                .topo-celula { display: table-cell; vertical-align: top; }
                .topo-celula.titulo { vertical-align: bottom; }
                .bloco-equipamento { text-align: center; font-size: 9px; }
                .bloco-equipamento div { margin-top: 2px; }
                .cabecalho { margin-bottom: 6px; }
                .cabecalho-linha { display: table; width: 100%%; margin-bottom: 2px; }
                .cabecalho-celula { display: table-cell; padding: 1px 0; }
                table.log { width: 100%%; height: 1px; border-collapse: collapse; margin-top: 4px; }
                table.log th, table.log td { border: 1px solid #000; padding: 3px; text-align: center; }
                table.log thead th { background: #ef82b3; color: #fff; }
                table.log tbody tr { height: 22px; }
                .assinaturas { display: table; width: 100%%; margin-top: 6px; }
                .assinaturas-linha { display: table-row; }
                .assinaturas-celula { display: table-cell; border: 1px solid #000; padding: 6px; text-align: center; }
            </style>
            </head>
            <body>
                %s
                %s
            </body>
            </html>
            """.formatted(dataHoraGeracao, pagina1, pagina2);
    }

    private String montarCabecalhoPagina(
        String periodo, Veiculo veiculo, Motorista motorista, String autorizacao, String unidade,
        String tagBanner, String tagIconeVeiculo
    ) {
        return """
            <div class="topo">
                <div class="topo-linha">
                    <div class="topo-celula" style="width:30%%">%s</div>
                    <div class="topo-celula titulo" style="width:44%%; text-align:center">
                        <h1>RELATORIO DIARIO DE MOVIMENTO DE VIATURA - RMV<br/>ORDEM DE TRAFEGO</h1>
                    </div>
                    <div class="topo-celula" style="width:26%%">
                        <div class="bloco-equipamento">
                            %s
                            <div>EQUIPAMENTO OBRIGATORIO</div>
                            <div>Pneu de Estepe [&#160;&#160;] Triangulo [&#160;&#160;] Macaco [&#160;&#160;] Chave de Roda [&#160;&#160;]</div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="cabecalho">
                <div class="cabecalho-linha">
                    <div class="cabecalho-celula" style="width:60%%"><b>DATA:</b> %s</div>
                    <div class="cabecalho-celula" style="width:40%%"><b>PLACA DO VEICULO:</b> %s</div>
                </div>
                <div class="cabecalho-linha">
                    <div class="cabecalho-celula"><b>ORDEM %s:</b> ________________________________________</div>
                </div>
                <div class="cabecalho-linha">
                    <div class="cabecalho-celula" style="width:60%%"><b>UNIDADE:</b> %s</div>
                    <div class="cabecalho-celula" style="width:40%%"><b>KM INICIAL:</b> _____________ &#160;&#160; <b>KM FINAL:</b> _____________</div>
                </div>
                <div class="cabecalho-linha">
                    <div class="cabecalho-celula"><b>MOTORISTA:</b> %s</div>
                </div>
                <div class="cabecalho-linha">
                    <div class="cabecalho-celula"><b>TIPO CARGA / PASSAGEIRO:</b> __________________________________________________________</div>
                </div>
                <div class="cabecalho-linha">
                    <div class="cabecalho-celula"><b>ASSUNTO / MOTIVO:</b> __________________________________________________________________</div>
                </div>
            </div>
            """.formatted(
            tagBanner,
            tagIconeVeiculo,
            periodo,
            veiculo.getPlaca(),
            autorizacao,
            unidade,
            motorista.getNome().toUpperCase()
        );
    }

    private String montarTabelaLog(int quantidadeLinhas) {
        return """
            <table class="log">
                <thead>
                    <tr>
                        <th colspan="3">SAIDA</th>
                        <th colspan="3">CHEGADA</th>
                        <th rowspan="2">DEPARTAMENTO<br/>SOLICITANTE</th>
                    </tr>
                    <tr>
                        <th>LOCAL</th><th>DATA E HORA</th><th>ODOMETRO</th>
                        <th>LOCAL</th><th>DATA E HORA</th><th>ODOMETRO</th>
                    </tr>
                </thead>
                <tbody>
                    %s
                </tbody>
            </table>

            <div class="assinaturas">
                <div class="assinaturas-linha">
                    <div class="assinaturas-celula" style="width:34%%">ASSINATURA DO MOTORISTA</div>
                    <div class="assinaturas-celula" style="width:33%%">NIVEL INICIAL DO COMBUSTIVEL</div>
                    <div class="assinaturas-celula" style="width:33%%">KM RODADO</div>
                </div>
            </div>
            """.formatted(linhasEmBranco(quantidadeLinhas));
    }

    private String linhasEmBranco(int quantidadeLinhas) {
        String linha = "<tr><td>&#160;</td><td>___/___&#160;&#160;:</td><td>&#160;</td>"
            + "<td>&#160;</td><td>___/___&#160;&#160;:</td><td>&#160;</td><td>&#160;</td></tr>";
        return linha.repeat(quantidadeLinhas);
    }

    // mesmo mapeamento do sistema legado (geraOrdem.php): cada tipo de veiculo
    // tinha um diagrama proprio; tipos sem equivalente caem no diagrama de carro
    private String caminhoIconeVeiculo(String tipoVeiculo) {
        String nome = tipoVeiculo == null ? "" : tipoVeiculo.toLowerCase();
        if (nome.contains("caminh")) {
            return "/img/caminhao.png";
        }
        if (nome.contains("moto")) {
            return "/img/moto.png";
        }
        if (nome.contains("kombi")) {
            return "/img/kombi.png";
        }
        if (nome.contains("van") || nome.contains("furg") || nome.contains("minibus") || nome.contains("ambul") || nome.contains("onibus") || nome.contains("bus")) {
            return "/img/van.png";
        }
        return "/img/trafego.png";
    }

    // busca a imagem no classpath e embute como base64 — se o arquivo nao
    // existir (removido de proposito, por exemplo), so devolve string vazia
    // e o espaco da imagem fica em branco no PDF, sem quebrar nada.
    private String tagImagemOpcional(String caminhoClasspath, String estilo, String textoAlternativo) {
        try (InputStream entrada = getClass().getResourceAsStream(caminhoClasspath)) {
            if (entrada == null) {
                return "";
            }
            String base64 = Base64.getEncoder().encodeToString(entrada.readAllBytes());
            return "<img src=\"data:image/png;base64,%s\" style=\"%s\" alt=\"%s\"/>".formatted(base64, estilo, textoAlternativo);
        } catch (IOException excecao) {
            return "";
        }
    }

    private String formatarPeriodo(LocalDate data, LocalDate dataFim) {
        String dataFormatada = data.format(FORMATO_DATA);
        if (dataFim == null || dataFim.isEqual(data)) {
            return dataFormatada;
        }
        return dataFormatada + " ate " + dataFim.format(FORMATO_DATA);
    }

    private String valorOu(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor;
    }

    private byte[] renderizarPdf(String html) {
        try (ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(saida);
            builder.run();
            return saida.toByteArray();
        } catch (Exception excecao) {
            throw new IllegalStateException("Falha ao gerar PDF da ordem de trafego", excecao);
        }
    }
}
