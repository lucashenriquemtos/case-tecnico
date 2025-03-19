package br.com.lhmatos.testetecnico.test.controller;

import br.com.lhmatos.testetecnico.dto.ProdutoResponseDTO;
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
import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class ProdutoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${jwt.secret}")
    private String secretKey;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @BeforeEach
    void setUp() {
        pedidoRepository.deleteAll();
        produtoRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario usuario = new Usuario();
        usuario.setEmail("admin@ecommerce.com");
        usuario.setRole("ROLE_ADMIN");
        usuario.setSenha(new BCryptPasswordEncoder().encode("123456"));
        usuarioRepository.save(usuario);

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

    @Test
    void deveriaCriarProdutoComPerfilAdmin() throws Exception {
        String adminEmail = "admin@ecommerce.com";
        String adminPassword = "123456";
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(adminEmail, adminPassword)
        );

        String token = generateToken(authentication, secretKey);

        String produtoJson = """
        {
            "nome": "Produto Teste",
            "descricao": "Descrição do produto",
            "preco": 99.90,
            "categoria": "Eletrônicos",
            "quantidadeEstoque": 10
        }
        """;

        mockMvc.perform(post("/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Produto Teste"))
                .andExpect(jsonPath("$.preco").value(99.90))
                .andExpect(jsonPath("$.categoria").value("Eletrônicos"))
                .andExpect(jsonPath("$.quantidadeEstoque").value(10))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void naoDeveriaCriarProdutoSemPerfilAdmin() throws Exception {
        String userEmail = "user@ecommerce.com";
        String userPassword = "123456";
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userEmail, userPassword)
        );

        String token = generateToken(authentication, secretKey);

        String produtoJson = """
            {
                "nome": "Produto Teste",
                "descricao": "Descrição do produto",
                "preco": 99.90,
                "categoria": "Eletrônicos",
                "quantidadeEstoque": 10
            }
            """;

        mockMvc.perform(post("/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveriaBuscarProdutoPorIdComPerfilUser() throws Exception {

        String produtoJson = """
        {
            "nome": "Produto Teste",
            "descricao": "Descrição do produto",
            "preco": 99.90,
            "categoria": "Eletrônicos",
            "quantidadeEstoque": 10
        }
        """;

        String response = mockMvc.perform(post("/produtos")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Produto produtoCriado = objectMapper.readValue(response, Produto.class);
        UUID produtoId = produtoCriado.getId();

        String userEmail = "user@ecommerce.com";
        String userPassword = "123456";
        Authentication userAuth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userEmail, userPassword)
        );
        String userToken = generateToken(userAuth, secretKey);

        mockMvc.perform(get("/produtos/" + produtoId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Produto Teste"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void deveriaBuscarProdutoPorIdExistente() throws Exception {

        String produtoJson = """
        {
            "nome": "Produto Teste",
            "descricao": "Descrição do produto",
            "preco": 99.90,
            "categoria": "Eletrônicos",
            "quantidadeEstoque": 10
        }
        """;

        String response = mockMvc.perform(post("/produtos")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Produto produtoCriado = objectMapper.readValue(response, Produto.class);
        UUID produtoId = produtoCriado.getId();

        mockMvc.perform(get("/produtos/" + produtoId)
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Produto Teste"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void deveriaLancarExcecaoAoBuscarProdutoPorIdInexistente() throws Exception {

        UUID produtoIdInexistente = UUID.randomUUID();
        mockMvc.perform(get("/produtos/" + produtoIdInexistente)
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void deveriaAtualizarProdutoComPerfilAdmin() throws Exception {
        String produtoJson = """
            {
                "nome": "Produto Teste",
                "descricao": "Descrição do produto",
                "preco": 99.90,
                "categoria": "Eletrônicos",
                "quantidadeEstoque": 10
            }
            """;

        String response = mockMvc.perform(post("/produtos")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Produto produtoCriado = objectMapper.readValue(response, Produto.class);
        UUID produtoId = produtoCriado.getId();

        String produtoAtualizadoJson = """
            {
                "nome": "Produto Teste",
                "descricao": "Descrição do produto",
                "preco": 100.00,
                "categoria": "Eletrônicos",
                "quantidadeEstoque": 10
            }
            """;

        mockMvc.perform(put("/produtos/{id}", produtoId)
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoAtualizadoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preco").value(100.00))
                .andExpect(jsonPath("$.nome").value("Produto Teste"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void naoDeveriaAtualizarProdutoComPerfilUser() throws Exception {
        String produtoJson = """
            {
                "nome": "Produto Teste",
                "descricao": "Descrição do produto",
                "preco": 99.90,
                "categoria": "Eletrônicos",
                "quantidadeEstoque": 10
            }
            """;

        String response = mockMvc.perform(post("/produtos")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Produto produtoCriado = objectMapper.readValue(response, Produto.class);
        UUID produtoId = produtoCriado.getId();

        String produtoAtualizadoJson = """
            {
                "nome": "Produto Teste",
                "descricao": "Descrição do produto",
                "preco": 100.00,
                "categoria": "Eletrônicos",
                "quantidadeEstoque": 10
            }
            """;

        mockMvc.perform(put("/produtos/{id}", produtoId)
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoAtualizadoJson))
                .andExpect(status().isForbidden())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void deveriaDeletarProdutoComPerfilAdmin() throws Exception {
        String produtoJson = """
            {
                "nome": "Produto Teste",
                "descricao": "Descrição do produto",
                "preco": 99.90,
                "categoria": "Eletrônicos",
                "quantidadeEstoque": 10
            }
            """;

        String response = mockMvc.perform(post("/produtos")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Produto produtoCriado = objectMapper.readValue(response, Produto.class);
        UUID produtoId = produtoCriado.getId();

        mockMvc.perform(delete("/produtos/{id}", produtoId)
                .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void naoDeveriaDeletarProdutoComPerfilUser() throws Exception {
        String produtoJson = """
            {
                "nome": "Produto Teste",
                "descricao": "Descrição do produto",
                "preco": 99.90,
                "categoria": "Eletrônicos",
                "quantidadeEstoque": 10
            }
            """;

        String response = mockMvc.perform(post("/produtos")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Produto produtoCriado = objectMapper.readValue(response, Produto.class);
        UUID produtoId = produtoCriado.getId();

        mockMvc.perform(delete("/produtos/{id}", produtoId)
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isForbidden());
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
    void deveriaUsarCacheAoBuscarProdutoPorId() throws Exception {
        // Criar produto
        String produtoJson = """
        {
            "nome": "Produto Teste",
            "descricao": "Descrição do produto",
            "preco": 99.90,
            "categoria": "Eletrônicos",
            "quantidadeEstoque": 10
        }
        """;
        String response = mockMvc.perform(post("/produtos")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ProdutoResponseDTO produtoCriado = objectMapper.readValue(response, ProdutoResponseDTO.class);
        UUID produtoId = produtoCriado.id();

        mockMvc.perform(get("/produtos/{id}", produtoId)
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Produto Teste"))
                .andDo(MockMvcResultHandlers.print());

        mockMvc.perform(get("/produtos/{id}", produtoId)
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Produto Teste"))
                .andDo(MockMvcResultHandlers.print());

        String produtoAtualizadoJson = """
        {
            "nome": "Produto Teste Atualizado",
            "descricao": "Descrição do produto",
            "preco": 100.00,
            "categoria": "Eletrônicos",
            "quantidadeEstoque": 10
        }
        """;
        mockMvc.perform(put("/produtos/{id}", produtoId)
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoAtualizadoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Produto Teste Atualizado"))
                .andDo(MockMvcResultHandlers.print());

        mockMvc.perform(get("/produtos/{id}", produtoId)
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Produto Teste Atualizado"))
                .andDo(MockMvcResultHandlers.print());
    }
}