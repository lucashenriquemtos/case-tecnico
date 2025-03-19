package br.com.lhmatos.testetecnico.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;

    @Value("${jwt.secret}")
    private String secretKey;

    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void generateHash() {
        log.info("Novo hash para 123456: {}", passwordEncoder.encode("123456"));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        try {
            log.info("Tentando autenticar: {}", loginRequest.getEmail());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getSenha())
            );

            log.info("Autenticação bem-sucedida para: {}", authentication.getName());

            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            String token = Jwts.builder()
                    .subject(authentication.getName())
                    .claim("role", authentication.getAuthorities())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                    .signWith(key)
                    .compact();

            return ResponseEntity.ok("Bearer " + token);
        } catch (BadCredentialsException ex) {
            log.error("Credenciais inválidas para: {}", loginRequest.getEmail(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        } catch (Exception ex) {
            log.error("Erro ao autenticar: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno");
        }
    }

}

@Getter
@Setter
class LoginRequest {
    private String email;
    private String senha;
}


