package br.com.lhmatos.testetecnico.test.controller;

import br.com.lhmatos.testetecnico.dto.PedidoResponseDTO;
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
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RelatorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

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
        produtoRepository.deleteAll();

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
    void deveriaCalcularFaturamentoMensal() throws Exception {
        String produtoResponse = criarProduto();
        Produto produto = objectMapper.readValue(produtoResponse, Produto.class);
        Pedido pedido = criarPedido(produto.getId().toString());

        mockMvc.perform(post("/pedidos/{id}/pagar", pedido.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/relatorio/faturamento-mensal")
                        .param("ano", "2025")
                        .param("mes", "3")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(199.80))
                .andExpect(jsonPath("$.ano").value(2025))
                .andExpect(jsonPath("$.mes").value(3))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void deveriaCalcularTicketMedioPorUsuario() throws Exception {
        String produtoResponse = criarProduto();
        Produto produto = objectMapper.readValue(produtoResponse, Produto.class);
        criarPedido(produto.getId().toString());

        mockMvc.perform(get("/relatorio/ticket-medio-por-usuario")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].usuarioId").isNotEmpty())
                .andExpect(jsonPath("$[0].email").isNotEmpty())
                .andExpect(jsonPath("$[0].ticketMedio").value(199.80))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void deveriaListarTop5UsuariosQueMaisCompraram() throws Exception {
        String produtoJson = """
                {
                    "nome": "Produto Teste",
                    "preco": 99.90,
                    "quantidadeEstoque": 100
                }
                """;
        String produtoResponse = mockMvc.perform(post("/produtos")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Produto produto = objectMapper.readValue(produtoResponse, Produto.class);
        String produtoId = produto.getId().toString();

        Map<Long, String> usuarioIdsETokens = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            Usuario user = new Usuario();
            user.setEmail("user" + i + "@ecommerce.com");
            user.setRole("ROLE_USER");
            user.setSenha(new BCryptPasswordEncoder().encode("123456"));
            usuarioRepository.save(user);

            String token = gerarToken(user.getEmail(), user.getId());
            usuarioIdsETokens.put(user.getId(), token);

            int numPedidos = new Random().nextInt(5) + 1;
            for (int j = 0; j < numPedidos; j++) {
                String pedidoResponse = mockMvc.perform(post("/pedidos")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        [
                                            {"produtoId": "%s", "quantidade": 2}
                                        ]
                                        """.formatted(produtoId)))
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString();

                PedidoResponseDTO pedido = objectMapper.readValue(pedidoResponse, PedidoResponseDTO.class);
                mockMvc.perform(post("/pedidos/{id}/pagar", pedido.id())
                                .header("Authorization", "Bearer " + token))
                        .andExpect(status().isOk());
            }
        }

        mockMvc.perform(get("/relatorio/top-usuarios")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[*].usuarioId").exists())
                .andExpect(jsonPath("$[*].email").exists())
                .andExpect(jsonPath("$[*].totalGasto").exists())
                .andDo(MockMvcResultHandlers.print());
    }

    private String gerarToken(String email, Long id) {
        SecretKey key = Keys.hmacShaKeyFor("4Ej_u5bMpyApkkpoy3FiLNjEgn7p19fTyiiK-MuqXzw=".getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(email)
                .claim("id", id)
                .claim("role", List.of("ROLE_USER"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(key)
                .compact();
    }

}