package br.com.lhmatos.testetecnico.dto;

import java.math.BigDecimal;

public record FaturamentoMensalDTO(BigDecimal valorTotal, int ano, int mes) {}
