package br.com.lhmatos.testetecnico.controller;

import br.com.lhmatos.testetecnico.dto.PedidoProdutoRequest;
import br.com.lhmatos.testetecnico.dto.PedidoResponseDTO;
import br.com.lhmatos.testetecnico.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criarPedido(@RequestBody List<PedidoProdutoRequest> itens, Authentication authentication) {
        String email = authentication.getName();
        Long usuarioId = pedidoService.findUsuarioIdByEmail(email);
        PedidoResponseDTO pedido = pedidoService.criarPedido(usuarioId, itens);
        return new ResponseEntity<>(pedido, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<PedidoResponseDTO> realizarPagamento(@PathVariable UUID id) {
        PedidoResponseDTO pedidoAtualizado = pedidoService.realizarPagamento(id);
        return ResponseEntity.ok(pedidoAtualizado);
    }

    @GetMapping("/meus-pedidos")
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidosDoUsuario(Authentication authentication) {
        String usuarioId = authentication.getName();
        List<PedidoResponseDTO> pedidos = pedidoService.listarPedidosPorUsuario(usuarioId);
        return ResponseEntity.ok(pedidos);
    }




}
