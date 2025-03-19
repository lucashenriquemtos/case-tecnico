package br.com.lhmatos.testetecnico.dto;

import java.math.BigDecimal;
import java.util.UUID;


public record PedidoProdutoDTO(
        UUID id,
        UUID produtoId,
        Integer quantidade,
        BigDecimal precoUnitario) {
}
