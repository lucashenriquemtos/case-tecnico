package br.com.lhmatos.testetecnico.dto;

import java.math.BigDecimal;

public record TopUsuarioDTO(Long usuarioId, String email, BigDecimal totalGasto) {}
