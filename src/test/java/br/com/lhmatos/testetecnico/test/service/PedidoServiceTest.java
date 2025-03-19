package br.com.lhmatos.testetecnico.test.service;

import br.com.lhmatos.testetecnico.dto.PedidoProdutoRequest;
import br.com.lhmatos.testetecnico.dto.PedidoResponseDTO;
import br.com.lhmatos.testetecnico.entity.*;
import br.com.lhmatos.testetecnico.repository.PedidoRepository;
import br.com.lhmatos.testetecnico.repository.ProdutoRepository;
import br.com.lhmatos.testetecnico.repository.UsuarioRepository;
import br.com.lhmatos.testetecnico.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Long usuarioId;
    private UUID produtoId;
    private UUID pedidoId;
    private Usuario usuario;
    private Produto produto;

    @BeforeEach
    void setUp() {

        usuarioId = 1L;
        produtoId = UUID.randomUUID();
        pedidoId = UUID.randomUUID();

        usuario = Usuario.builder()
                .id(usuarioId)
                .email("user@ecommerce.com")
                .senha("encodedPassword")
                .role("ROLE_USER").build();

        produto = Produto.builder()
                .id(produtoId)
                .nome("Produto Teste")
                .preco(new BigDecimal("99.90"))
                .quantidadeEstoque(10)
                .build();
    }

    @Test
    void deveriaCriarPedidoComStatusPendente() {
        PedidoProdutoRequest itemRequest = new PedidoProdutoRequest(produtoId, 2);
        List<PedidoProdutoRequest> itens = List.of(itemRequest);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoResponseDTO pedidoCriado = pedidoService.criarPedido(usuarioId, itens);

        assertEquals(StatusPedido.PENDENTE, pedidoCriado.status(), "O status do pedido deve ser PENDENTE");
        verify(usuarioRepository).findById(usuarioId);
        verify(produtoRepository).findById(produtoId);
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    void deveriaAtualizarEstoqueApenasAposPagamento() {
        PedidoProdutoRequest itemRequest = new PedidoProdutoRequest(produtoId, 2);
        List<PedidoProdutoRequest> itens = List.of(itemRequest);

        Pedido pedido = Pedido.builder()
                .id(pedidoId)
                .usuario(usuario)
                .dataCriacao(LocalDateTime.now())
                .status(StatusPedido.PENDENTE)
                .itens(Collections.emptySet())
                .valorTotal(new BigDecimal("199.80"))
                .build();

        Set<PedidoProduto> pedidoItens = Set.of(
                PedidoProduto.builder()
                        .produto(produto)
                        .quantidade(2)
                        .precoUnitario(produto.getPreco())
                        .pedido(pedido)
                        .build()
        );

        pedido.setItens(pedidoItens);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        PedidoResponseDTO pedidoCriado = pedidoService.criarPedido(usuarioId, itens);

        assertEquals(10, produto.getQuantidadeEstoque(), "O estoque não deve ser atualizado ao criar o pedido");
        verify(produtoRepository, never()).save(any(Produto.class));

        PedidoResponseDTO pedidoPago = pedidoService.realizarPagamento(pedidoId);

        assertEquals(StatusPedido.PAGO, pedidoPago.status(), "O status do pedido deve ser PAGO");
        assertEquals(8, produto.getQuantidadeEstoque(), "O estoque deve ser atualizado após o pagamento");
        verify(produtoRepository, times(1)).save(produto);
    }

}