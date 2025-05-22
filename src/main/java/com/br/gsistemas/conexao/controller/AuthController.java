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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    //metodo para testar extração do token
    @GetMapping("/decode-token")
    public ResponseEntity<?> decodeToken(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT inválido.");
        }

        String jwt = token.replace("Bearer ", "");
        Map<String, Object> claims = jwtUtil.extrairClaims(jwt);

        return ResponseEntity.ok(claims);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            String token = jwtUtil.gerarToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("tipoUsuario", user.getTipo().name());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Credenciais inválidas"));
        }
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarUsuario(
            @RequestParam("nome") String nome,
            @RequestParam("email") String email,
            @RequestParam("telefone") String telefone,
            @RequestParam("senha") String senha,
            @RequestParam("tipo") String tipo,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        if (foto == null || foto.isEmpty()) {
            return ResponseEntity.badRequest().body("A foto do usuário é obrigatória!");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("E-mail já cadastrado!");
        }

        if (tipo == null) {
            return ResponseEntity.badRequest().body("O tipo de usuário é obrigatório!");
        }

        User user = new User();
        user.setNome(nome);
        user.setEmail(email);
        user.setTelefone(telefone);
        user.setSenha(passwordEncoder.encode(senha));
        user.setTipo(UserType.valueOf(tipo));

        // Corrige o caminho absoluto para o diretório de uploads
        String uploadDir = System.getProperty("user.dir") + "/uploads/"; // Caminho absoluto correto
        File diretorio = new File(uploadDir);

        // Se não existir, criar a pasta de uploads
        if (!diretorio.exists()) {
            boolean criado = diretorio.mkdirs();
            System.out.println("📂 Criando diretório uploads/: " + (criado ? "SUCESSO" : "FALHA"));
        }

        // Se o diretório não foi criado, retorna erro
        if (!diretorio.exists()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao acessar o diretório de upload.");
        }

        // Gera nome do arquivo e salva a foto
        if (foto != null && !foto.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + foto.getOriginalFilename();
            File uploadFile = new File(uploadDir, fileName); // Usa o caminho absoluto correto

            try {
                foto.transferTo(uploadFile);
                user.setFotoUrl("/uploads/" + fileName); // Salva a URL no banco de dados
                System.out.println("✅ Foto salva com sucesso em: " + uploadFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao salvar a imagem: " + e.getMessage());
            }
        }

        userRepository.save(user);
        String token = jwtUtil.gerarToken(user);

        return ResponseEntity.ok().body(Map.of(
                "token", token,
                "tipoUsuario", user.getTipo().name(),
                "fotoUrl", user.getFotoUrl() // Retorna a URL da foto no JSON
        ));
    }


}
