package com.ovg.transportes.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Busca so por DIA e quantidade de passageiros — sem exigir origem/destino
 * de antemao (decidido apos feedback de uso: forcar a pessoa a ja saber a
 * rota antes de olhar o que existe era atrito desnecessario; cada viagem
 * encontrada ja mostra sua propria rota no card).
 */
public record BuscaViagemRequestDTO(
    @NotNull @FutureOrPresent LocalDate data,
    @NotNull @Min(1) Integer qtdPassageiros
) {
}
