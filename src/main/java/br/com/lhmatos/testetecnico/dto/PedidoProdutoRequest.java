package br.com.lhmatos.testetecnico.dto;

import java.util.UUID;


public record PedidoProdutoRequest(
        UUID produtoId,
        Integer quantidade) {
}
