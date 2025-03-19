package br.com.lhmatos.testetecnico.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProdutoResponseDTO(
        UUID id,
        String nome,
        String descricao,
        BigDecimal preco,
        String categoria,
        Integer quantidadeEstoque,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {}