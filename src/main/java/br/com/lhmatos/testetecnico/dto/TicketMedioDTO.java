package br.com.lhmatos.testetecnico.dto;

import java.math.BigDecimal;

public record TicketMedioDTO(Long usuarioId, String email, BigDecimal ticketMedio) {}
