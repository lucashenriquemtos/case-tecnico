package br.com.lhmatos.testetecnico.test.controller;

import br.com.lhmatos.testetecnico.entity.Usuario;
import br.com.lhmatos.testetecnico.repository.PedidoRepository;
import br.com.lhmatos.testetecnico.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @BeforeEach
    void setUp() {
        pedidoRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario usuario = new Usuario();
        usuario.setEmail("admin@ecommerce.com");
        usuario.setRole("ROLE_ADMIN");
        usuario.setSenha(new BCryptPasswordEncoder().encode("123456"));
        usuarioRepository.save(usuario);
    }

    @Test
    void deveriaAutenticarUsuarioComCredenciaisValidas() throws Exception {
        String loginJson = """
        {
            "email": "admin@ecommerce.com",
            "senha": "123456"
        }
        """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString();
                    assert response.startsWith("Bearer ");
                    assert response.length() > 7;
                });
    }


    @Test
    void deveriaFalharAutenticacaoComCredenciaisInvalidas() throws Exception {
        String loginJson = """
        {
            "email": "usuarioInexistente@ecommerce.com",
            "senha": "123456"
        }
        """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciais inválidas"));
    }
}