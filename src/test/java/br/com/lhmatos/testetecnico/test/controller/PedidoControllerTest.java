package br.com.lhmatos.testetecnico.test.controller;

import br.com.lhmatos.testetecnico.entity.Pedido;
import br.com.lhmatos.testetecnico.entity.Produto;
import br.com.lhmatos.testetecnico.entity.Usuario;
import br.com.lhmatos.testetecnico.repository.PedidoRepository;
import br.com.lhmatos.testetecnico.repository.ProdutoRepository;
import br.com.lhmatos.testetecnico.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PedidoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Value("${jwt.secret}")
    private String secretKey;

    private Long usuarioId;
    private UUID produtoId;

    @BeforeEach
    void setUp() {

        usuarioId = 1L;
        produtoId = UUID.randomUUID();
        pedidoRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario admin = new Usuario();
        admin.setEmail("admin@ecommerce.com");
        admin.setRole("ROLE_ADMIN");
        admin.setSenha(new BCryptPasswordEncoder().encode("123456"));
        usuarioRepository.save(admin);

        Usuario user = new Usuario();
        user.setEmail("user@ecommerce.com");
        user.setRole("ROLE_USER");
        user.setSenha(new BCryptPasswordEncoder().encode("123456"));
        usuarioRepository.save(user);
    }

    private static String generateToken(Authentication authentication, String secretKey) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(authentication.getName())
                .claim("role", authentication.getAuthorities())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(key)
                .compact();
    }

    private String adminToken() {
        String adminEmail = "admin@ecommerce.com";
        String adminPassword = "123456";
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(adminEmail, adminPassword)
        );

        return generateToken(authentication, secretKey);
    }

    private String userToken() {
        String userEmail = "user@ecommerce.com";
        String userPassword = "123456";
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userEmail, userPassword)
        );

        return generateToken(authentication, secretKey);
    }

    @Test
    void deveriaCriarPedidoComSucesso() throws Exception {
        String produtoJson = """
                {
                    "nome": "Produto Teste",
                    "preco": 99.90,
                    "quantidadeEstoque": 10
                }
                """;
        String produtoResponse = mockMvc.perform(post("/produtos")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Produto produto = objectMapper.readValue(produtoResponse, Produto.class);

        String pedidoJson = """
                [
                    {
                        "produtoId": "%s",
                        "quantidade": 2
                    }
                ]
                """.formatted(produto.getId());

        mockMvc.perform(post("/pedidos")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson))
                .andExpect(status().isCreated())
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void deveriaRealizarPagamentoDoPedido() throws Exception {
        String produtoJson = """
                {
                    "nome": "Produto Teste",
                    "preco": 99.90,
                    "quantidadeEstoque": 10
                }
                """;
        String produtoResponse = mockMvc.perform(post("/produtos")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Produto produto = objectMapper.readValue(produtoResponse, Produto.class);

        String pedidoJson = """
                [
                    {
                        "produtoId": "%s",
                        "quantidade": 2
                    }
                ]
                """.formatted(produto.getId());
        String pedidoResponse = mockMvc.perform(post("/pedidos")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Pedido pedido = objectMapper.readValue(pedidoResponse, Pedido.class);

        mockMvc.perform(post("/pedidos/{id}/pagar", pedido.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGO"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void deveriaCancelarPedidoQuandoProdutoSemEstoque() throws Exception {
        String produtoJson = """
                {
                    "nome": "Produto Teste",
                    "preco": 99.90,
                    "quantidadeEstoque": 1
                }
                """;
        String produtoResponse = mockMvc.perform(post("/produtos")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Produto produto = objectMapper.readValue(produtoResponse, Produto.class);

        String pedidoJson = """
                [
                    {
                        "produtoId": "%s",
                        "quantidade": 2
                    }
                ]
                """.formatted(produto.getId());

        mockMvc.perform(post("/pedidos")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Estoque insuficiente para o produto: Produto Teste"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void deveriaCalcularValorTotalComPrecoAtualDoProduto() throws Exception {
        String produtoJson = """
                {
                    "nome": "Produto Teste",
                    "preco": 99.90,
                    "quantidadeEstoque": 10
                }
                """;
        String produtoResponse = mockMvc.perform(post("/produtos")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Produto produto = objectMapper.readValue(produtoResponse, Produto.class);

        produto.setPreco(new BigDecimal("150.00"));
        produtoRepository.save(produto);

        String pedidoJson = """
                [
                    {
                        "produtoId": "%s",
                        "quantidade": 2
                    }
                ]
                """.formatted(produto.getId());

        mockMvc.perform(post("/pedidos")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorTotal").value(300.00))
                .andReturn().getResponse().getContentAsString();

    }

    private String criarProduto() throws Exception {
        String produtoJson = """
        {
            "nome": "Produto Teste",
            "preco": 99.90,
            "quantidadeEstoque": 10
        }
        """;
        return mockMvc.perform(post("/produtos")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private Pedido criarPedido(String produtoId) throws Exception {
        String pedidoJson = """
        [
            {
                "produtoId": "%s",
                "quantidade": 2
            }
        ]
        """.formatted(produtoId);
        String pedidoResponse = mockMvc.perform(post("/pedidos")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(pedidoResponse, Pedido.class);
    }

    @Test
    void deveriaListarPedidosDoUsuarioAutenticado() throws Exception {
        String produtoResponse = criarProduto();
        Produto produto = objectMapper.readValue(produtoResponse, Produto.class);
        String produtoId = produto.getId().toString();

        criarPedido(produtoId);

        mockMvc.perform(get("/pedidos/meus-pedidos")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].usuarioId").exists())
                .andExpect(jsonPath("$[0].valorTotal").value(199.80))
                .andExpect(jsonPath("$[0].itens[0].produtoId").value(produtoId))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void deveriaFalharAoPagarPedidoJaPago() throws Exception {
        String produtoResponse = criarProduto();
        Produto produto = objectMapper.readValue(produtoResponse, Produto.class);
        String produtoId = produto.getId().toString();

        Pedido pedido = criarPedido(produtoId);

        mockMvc.perform(post("/pedidos/{id}/pagar", pedido.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGO"))
                .andDo(MockMvcResultHandlers.print());

        mockMvc.perform(post("/pedidos/{id}/pagar", pedido.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pedido já foi pago ou está em processamento"))
                .andDo(MockMvcResultHandlers.print());
    }


}