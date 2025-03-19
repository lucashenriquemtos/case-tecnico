package br.com.lhmatos.testetecnico.controller;

import br.com.lhmatos.testetecnico.dto.FaturamentoMensalDTO;
import br.com.lhmatos.testetecnico.dto.TicketMedioDTO;
import br.com.lhmatos.testetecnico.dto.TopUsuarioDTO;
import br.com.lhmatos.testetecnico.service.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/relatorio")
public class RelatorioController {

    private final PedidoService pedidoService;

    public RelatorioController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/top-usuarios")
    public ResponseEntity<List<TopUsuarioDTO>> listarTopUsuarios() {
        List<TopUsuarioDTO> topUsuarios = pedidoService.listarTop5Usuarios();
        return ResponseEntity.ok(topUsuarios);
    }

    @GetMapping("/ticket-medio-por-usuario")
    public ResponseEntity<List<TicketMedioDTO>> listarTicketMedioPorUsuario() {
        List<TicketMedioDTO> tickets = pedidoService.listarTicketMedioPorUsuario();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/faturamento-mensal")
    public ResponseEntity<FaturamentoMensalDTO> getFaturamentoMensal(
            @RequestParam("ano") int ano,
            @RequestParam("mes") int mes) {
        FaturamentoMensalDTO faturamento = pedidoService.calcularFaturamentoMensal(ano, mes);
        return ResponseEntity.ok(faturamento);
    }
}
