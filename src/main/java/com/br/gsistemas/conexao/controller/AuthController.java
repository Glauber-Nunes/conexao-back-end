package com.br.gsistemas.conexao.controller;

import com.br.gsistemas.conexao.config.JwtUtil;
import com.br.gsistemas.conexao.domain.User;
import com.br.gsistemas.conexao.dto.LoginRequestDto;
import com.br.gsistemas.conexao.enums.UserType;
import com.br.gsistemas.conexao.repository.UserRepository;
import com.br.gsistemas.conexao.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 🔹 Libera acesso CORS para o frontend
public class AuthController {

    private final AuthService authService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto loginRequest) {
        String token = authService.autenticarUsuario(loginRequest);
        return ResponseEntity.ok(token);
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
