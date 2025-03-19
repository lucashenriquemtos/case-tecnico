package br.com.lhmatos.testetecnico.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PedidoProdutoResponseDTO(
        UUID produtoId,
        Integer quantidade,
        BigDecimal precoUnitario
) {}
