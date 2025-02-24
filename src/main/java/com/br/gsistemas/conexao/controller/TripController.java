package com.br.gsistemas.conexao.controller;

import com.br.gsistemas.conexao.config.JwtUtil;
import com.br.gsistemas.conexao.domain.Trip;
import com.br.gsistemas.conexao.domain.User;
import com.br.gsistemas.conexao.enums.TripStatus;
import com.br.gsistemas.conexao.enums.UserType;
import com.br.gsistemas.conexao.repository.TripRepository;
import com.br.gsistemas.conexao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/viagens")
@RequiredArgsConstructor
public class TripController {

    private final TripRepository tripRepository;

    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;

    // Listar viagens disponíveis
    @GetMapping("/disponiveis")
    public ResponseEntity<?> listarViagens(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT não fornecido ou inválido.");
        }

        String email = jwtUtil.extrairEmail(token.replace("Bearer ", ""));
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        List<Trip> viagens;

        if (usuario.getTipo().equals(UserType.PASSAGEIRO)) {
            viagens = tripRepository.findByStatus(TripStatus.DISPONIVEL);
        } else {
            viagens = tripRepository.findByMotorista(usuario);
        }

        return ResponseEntity.ok(viagens);
    }


    @PostMapping("/{id}/entrar")
    public ResponseEntity<?> entrarNaViagem(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long usuarioId = body.get("usuarioId");

        Trip viagem = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada!"));

        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        // Verifica se o usuário já está na viagem
        if (viagem.getPassageiros().contains(usuario)) {
            return ResponseEntity.badRequest().body("Você já está nesta viagem!");
        }

        // Adiciona o passageiro à lista da viagem
        viagem.getPassageiros().add(usuario);
        tripRepository.save(viagem);

        return ResponseEntity.ok().body("Usuário entrou na viagem com sucesso!");
    }


}
