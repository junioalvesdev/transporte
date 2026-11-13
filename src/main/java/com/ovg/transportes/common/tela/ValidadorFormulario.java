package com.ovg.transportes.common.tela;

import com.ovg.transportes.common.NegocioException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Valida os DTOs que chegam dos formularios das paginas com as mesmas regras
 * (@NotNull, @Future, @Min...) que a API aplica com @Valid, e transforma os
 * erros numa frase em portugues pra tela (NegocioException).
 */
@Component
public class ValidadorFormulario {

    private final Validator validator;

    public ValidadorFormulario(Validator validator) {
        this.validator = validator;
    }

    public void validar(Object requisicao) {
        Set<ConstraintViolation<Object>> violacoes = validator.validate(requisicao);
        if (!violacoes.isEmpty()) {
            throw new NegocioException(violacoes.stream()
                .map(ValidadorFormulario::mensagem)
                .sorted()
                .collect(Collectors.joining("; ")));
        }
    }

    // a mensagem padrao do Bean Validation sai no idioma do servidor (ingles
    // no container) — aqui ela vira uma frase em portugues
    private static String mensagem(ConstraintViolation<Object> violacao) {
        String campo = nomeDoCampo(violacao.getPropertyPath().toString());
        return switch (violacao.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName()) {
            case "NotNull", "NotBlank", "NotEmpty" -> "Informe " + campo;
            case "Future", "FutureOrPresent" -> campo + " precisa ser uma data futura";
            case "Min" -> campo + " precisa ser pelo menos " + violacao.getConstraintDescriptor().getAttributes().get("value");
            default -> campo + ": " + violacao.getMessage();
        };
    }

    private static String nomeDoCampo(String campo) {
        return switch (campo) {
            case "veiculoId" -> "Veiculo";
            case "motoristaId" -> "Motorista";
            case "tipoVeiculoId" -> "Tipo";
            case "dataHoraSaida", "dataHoraDesejada" -> "Saida";
            case "dataHoraChegadaEstimada", "dataHoraRetornoDesejada" -> "Retorno";
            case "qtdPassageiros" -> "Passageiros";
            case "telefoneContato" -> "Telefone";
            case "finalidade" -> "Finalidade";
            case "placa" -> "Placa";
            case "modelo" -> "Modelo";
            case "capacidade" -> "Capacidade";
            case "nome" -> "Nome";
            case "data" -> "Data";
            case "unidade" -> "Unidade";
            default -> campo;
        };
    }
}
