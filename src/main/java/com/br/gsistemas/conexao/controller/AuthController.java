package com.br.gsistemas.conexao.controller;

import com.br.gsistemas.conexao.config.JwtUtil;
import com.br.gsistemas.conexao.domain.User;
import com.br.gsistemas.conexao.dto.LoginRequestDto;
import com.br.gsistemas.conexao.enums.UserType;
import com.br.gsistemas.conexao.repository.UserRepository;
import com.br.gsistemas.conexao.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 🔹 Libera acesso CORS para o frontend
public class AuthController {

    private final AuthService authService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            String token = jwtUtil.gerarToken(user.getEmail());

            // 🔹 Retornando um JSON em vez de apenas uma string
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("tipoUsuario", user.getTipo().name());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Credenciais inválidas"));
        }
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarUsuario(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("E-mail já cadastrado!");
        }

        if (user.getTipo() == null) {
            return ResponseEntity.badRequest().body("O tipo de usuário é obrigatório!");
        }

        // Encriptar senha antes de salvar
        user.setSenha(passwordEncoder.encode(user.getSenha()));
        userRepository.save(user);

        // Gerar token JWT para o usuário recém-cadastrado
        String token = jwtUtil.gerarToken(user.getEmail());

        return ResponseEntity.ok().body(Map.of(
                "token", token,
                "tipoUsuario", user.getTipo().name()
        ));
    }
}
