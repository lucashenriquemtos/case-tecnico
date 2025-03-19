package br.com.lhmatos.testetecnico.dto;

import br.com.lhmatos.testetecnico.entity.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record PedidoResponseDTO(
        UUID id,
        Long usuarioId,
        LocalDateTime dataCriacao,
        StatusPedido status,
        BigDecimal valorTotal,
        Set<PedidoProdutoResponseDTO> itens
) {}
