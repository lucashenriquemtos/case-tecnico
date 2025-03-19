package br.com.lhmatos.testetecnico.service;

import br.com.lhmatos.testetecnico.dto.ProdutoRequestDTO;
import br.com.lhmatos.testetecnico.dto.ProdutoResponseDTO;
import br.com.lhmatos.testetecnico.entity.Produto;
import br.com.lhmatos.testetecnico.repository.ProdutoRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public ProdutoResponseDTO adicionarProduto(ProdutoRequestDTO request) {
        Produto produto = mapToEntity(request);
        produto.setDataCriacao(LocalDateTime.now());
        Produto salvo = produtoRepository.save(produto);
        return mapToDTO(salvo);
    }

    @Cacheable(value = "produtos", key = "#id")
    public ProdutoResponseDTO buscarProdutoPorId(UUID id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        return mapToDTO(produto);
    }

    @Transactional
    @CacheEvict(value = "produtos", key = "#id")
    public ProdutoResponseDTO atualizarProduto(UUID id, ProdutoRequestDTO request) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setPreco(request.preco());
        produto.setCategoria(request.categoria());
        produto.setQuantidadeEstoque(request.quantidadeEstoque());
        produto.setDataAtualizacao(LocalDateTime.now());
        Produto salvo = produtoRepository.save(produto);
        return mapToDTO(salvo);
    }

    @Transactional
    @CacheEvict(value = "produtos", key = "#id")
    public void deletarProduto(UUID id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        produtoRepository.delete(produto);
    }

    private Produto mapToEntity(ProdutoRequestDTO request) {
        return Produto.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .preco(request.preco())
                .categoria(request.categoria())
                .quantidadeEstoque(request.quantidadeEstoque())
                .build();
    }

    private ProdutoResponseDTO mapToDTO(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getCategoria(),
                produto.getQuantidadeEstoque(),
                produto.getDataCriacao(),
                produto.getDataAtualizacao()
        );
    }
}