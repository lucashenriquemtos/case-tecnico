package br.com.lhmatos.testetecnico.controller;

import br.com.lhmatos.testetecnico.dto.ProdutoRequestDTO;
import br.com.lhmatos.testetecnico.dto.ProdutoResponseDTO;
import br.com.lhmatos.testetecnico.service.ProdutoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/produtos")
public class ProdutoController {
    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }


    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> adicionarProduto(@RequestBody ProdutoRequestDTO produto) {
        ProdutoResponseDTO novoProduto = produtoService.adicionarProduto(produto);
        return new ResponseEntity<>(novoProduto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ProdutoResponseDTO> buscarProdutoPorId(@PathVariable UUID id) {
        try {
            ProdutoResponseDTO produto = produtoService.buscarProdutoPorId(id);
            return ResponseEntity.ok(produto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizarProduto(@PathVariable UUID id, @RequestBody ProdutoRequestDTO produto) {
        ProdutoResponseDTO produtoAtualizado = produtoService.atualizarProduto(id, produto);
        return ResponseEntity.ok(produtoAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable UUID id) {
        produtoService.deletarProduto(id);
        return ResponseEntity.noContent().build();
    }

}