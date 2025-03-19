package br.com.lhmatos.testetecnico.service;

import br.com.lhmatos.testetecnico.dto.*;
import br.com.lhmatos.testetecnico.entity.*;
import br.com.lhmatos.testetecnico.repository.PedidoRepository;
import br.com.lhmatos.testetecnico.repository.ProdutoRepository;
import br.com.lhmatos.testetecnico.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository, UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    @CacheEvict(value = "faturamentoMensal", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + '-' + T(java.time.YearMonth).now().getYear() + '-' + T(java.time.YearMonth).now().getMonthValue()")
    public PedidoResponseDTO criarPedido(Long usuarioId, List<PedidoProdutoRequest> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("A lista de itens não pode ser nula ou vazia");
        }

        Usuario usuario = buscarUsuario(usuarioId);
        Set<PedidoProduto> pedidoItens = criarItensDoPedido(itens);
        BigDecimal valorTotal = calcularValorTotal(pedidoItens);
        Pedido pedido = montarPedido(usuario, pedidoItens, valorTotal);

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        return mapToDTO(pedidoSalvo);
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + usuarioId));
    }

    private Set<PedidoProduto> criarItensDoPedido(List<PedidoProdutoRequest> itens) {
        return itens.stream()
                .map(this::criarPedidoProduto)
                .collect(Collectors.toSet());
    }

    private PedidoProduto criarPedidoProduto(PedidoProdutoRequest item) {
        Produto produto = produtoRepository.findById(item.produtoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + item.produtoId()));

        validarEstoque(produto, item.quantidade());

        return PedidoProduto.builder()
                .produto(produto)
                .quantidade(item.quantidade())
                .precoUnitario(produto.getPreco())
                .build();
    }

    private void validarEstoque(Produto produto, int quantidadeSolicitada) {
        if (produto.getQuantidadeEstoque() < quantidadeSolicitada) {
            throw new IllegalStateException("Estoque insuficiente para o produto: " + produto.getNome());
        }
    }

    private BigDecimal calcularValorTotal(Set<PedidoProduto> pedidoItens) {
        return pedidoItens.stream()
                .map(PedidoProduto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Pedido montarPedido(Usuario usuario, Set<PedidoProduto> pedidoItens, BigDecimal valorTotal) {
        Pedido pedido = Pedido.builder()
                .usuario(usuario)
                .dataCriacao(LocalDateTime.now())
                .status(StatusPedido.PENDENTE)
                .valorTotal(valorTotal)
                .itens(pedidoItens)
                .build();

        pedidoItens.forEach(item -> item.setPedido(pedido));
        return pedido;
    }

    private void atualizarEstoque(Set<PedidoProduto> pedidoItens) {
        pedidoItens.forEach(item -> {
            Produto produto = item.getProduto();
            int novaQuantidade = produto.getQuantidadeEstoque() - item.getQuantidade();
            if (novaQuantidade < 0) {
                throw new IllegalStateException("Estoque insuficiente para o produto: " + produto.getNome());
            }
            produto.setQuantidadeEstoque(novaQuantidade);
            produtoRepository.save(produto);
        });
    }

    @Transactional
    public PedidoResponseDTO realizarPagamento(UUID pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + pedidoId));

        validarStatusParaPagamento(pedido);
        atualizarStatusPedido(pedido);
        atualizarEstoque(pedido.getItens());

        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        return mapToDTO(pedidoSalvo);
    }

    private void validarStatusParaPagamento(Pedido pedido) {
        if (pedido.getStatus() == StatusPedido.PROCESSANDO || pedido.getStatus() == StatusPedido.PAGO) {
            throw new IllegalStateException("Pedido já foi pago ou está em processamento");
        }
        if (pedido.getStatus() != StatusPedido.PENDENTE) {
            throw new IllegalStateException("O pedido só pode ser pago se estiver no status PENDENTE");
        }
    }

    private void atualizarStatusPedido(Pedido pedido) {
        pedido.atualizarStatus(StatusPedido.PAGO);
    }

    public Long findUsuarioIdByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"))
                .getId();
    }

    private PedidoResponseDTO mapToDTO(Pedido pedido) {
        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getUsuario().getId(),
                pedido.getDataCriacao(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getItens().stream()
                        .map(item -> new PedidoProdutoResponseDTO(
                                item.getProduto().getId(),
                                item.getQuantidade(),
                                item.getPrecoUnitario()))
                        .collect(Collectors.toSet())
        );
    }

    public List<PedidoResponseDTO> listarPedidosPorUsuario(String usuarioId) {
        List<Pedido> pedidos = pedidoRepository.findByUsuarioEmail(usuarioId);
        return pedidos.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "topUsuarios")
    public List<TopUsuarioDTO> listarTop5Usuarios() {
        return pedidoRepository.findTop5UsuariosByPedidos();
    }

    @Cacheable(value = "ticketMedio")
    public List<TicketMedioDTO> listarTicketMedioPorUsuario() {
        return pedidoRepository.findTicketMedioPorUsuario();
    }

    @Cacheable(value = "faturamentoMensal", key = "#ano + '-' + #mes")
    public FaturamentoMensalDTO calcularFaturamentoMensal(int ano, int mes) {
        BigDecimal valorTotal = pedidoRepository.calcularFaturamentoMensal(ano, mes);
        return new FaturamentoMensalDTO(valorTotal, ano, mes);
    }
}
