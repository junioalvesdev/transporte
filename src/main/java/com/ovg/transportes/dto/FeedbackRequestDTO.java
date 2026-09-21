package com.ovg.transportes.dto;

import com.ovg.transportes.model.TipoFeedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeedbackRequestDTO(
    @NotNull TipoFeedback tipo,
    @NotBlank String descricao
) {
}
