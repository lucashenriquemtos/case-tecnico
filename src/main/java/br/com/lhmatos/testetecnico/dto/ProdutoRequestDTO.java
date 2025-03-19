package br.com.lhmatos.testetecnico.dto;

import java.math.BigDecimal;

public record ProdutoRequestDTO(
        String nome,
        String descricao,
        BigDecimal preco,
        String categoria,
        Integer quantidadeEstoque
) {
}
