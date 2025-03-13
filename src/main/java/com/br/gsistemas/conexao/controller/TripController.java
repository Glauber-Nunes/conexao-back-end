package com.br.gsistemas.conexao.controller;

import com.br.gsistemas.conexao.config.JwtUtil;
import com.br.gsistemas.conexao.domain.Trip;
import com.br.gsistemas.conexao.dto.TripDTO;
import com.br.gsistemas.conexao.domain.User;
import com.br.gsistemas.conexao.enums.TripStatus;
import com.br.gsistemas.conexao.enums.UserType;
import com.br.gsistemas.conexao.repository.TripRepository;
import com.br.gsistemas.conexao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public ResponseEntity<?> entrarNaViagem(@PathVariable("id") Long id, @RequestHeader("Authorization") String token) {
        if (id == null) {
            return ResponseEntity.badRequest().body("O ID da viagem não pode ser nulo!");
        }

        String email = jwtUtil.extrairEmail(token.replace("Bearer ", ""));

        User usuario = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        Trip viagem = tripRepository.findById(id).orElseThrow(() -> new RuntimeException("Viagem não encontrada!"));


        if (viagem.getPassageiros().contains(usuario)) {
            return ResponseEntity.badRequest().body("Você já está nesta viagem!");
        }

        viagem.getPassageiros().add(usuario);
        tripRepository.save(viagem);

        return ResponseEntity.ok("Usuário entrou na viagem com sucesso!");
    }


    @DeleteMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarViagem(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extrairEmail(token.replace("Bearer ", ""));
        Trip viagem = tripRepository.findById(id).orElseThrow(() -> new RuntimeException("Viagem não encontrada"));

        if (!viagem.getMotoristaEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Você não tem permissão para cancelar esta viagem."));
        }

        tripRepository.delete(viagem);
        return ResponseEntity.ok(Collections.singletonMap("message", "Viagem cancelada com sucesso!"));
    }

    @GetMapping("/{id}/detalhes")
    public ResponseEntity<?> detalhesViagem(@PathVariable Long id) {

        Trip viagem = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada!"));

        Map<String, Object> response = new HashMap<>();
        response.put("id", viagem.getId());
        response.put("origem", viagem.getOrigem());
        response.put("destino", viagem.getDestino());
        response.put("horario", viagem.getHorario());
        response.put("preco", viagem.getPreco());
        response.put("formaPagamento", viagem.getFormaPagamento());
        response.put("carro", viagem.getCarro());
        response.put("motoristaEmail", viagem.getMotoristaEmail());

        // 🔹 Retornar passageiros com EMAIL para garantir comparação no frontend
        List<Map<String, String>> passageiros = viagem.getPassageiros().stream()
                .map(p -> Map.of("id", p.getUsuarioId().toString(), "nome", p.getNome(), "email", p.getEmail()))
                .collect(Collectors.toList());

        response.put("passageiros", passageiros);
        response.put("quantidadePassageiros", passageiros.size());

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}/sair")
    public ResponseEntity<?> sairDaViagem(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extrairEmail(token.replace("Bearer ", ""));
        User passageiro = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        Trip viagem = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada!"));

        if (!viagem.getPassageiros().contains(passageiro)) {
            return ResponseEntity.badRequest().body("Você não está nesta viagem!");
        }

        viagem.getPassageiros().remove(passageiro);
        tripRepository.save(viagem);

        return ResponseEntity.ok("Você saiu da viagem!");
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarViagem(@RequestBody Trip viagem, @RequestHeader("Authorization") String token) {
        System.out.println("🚀 Recebendo requisição para cadastrar viagem...");

        if (token == null || !token.startsWith("Bearer ")) {
            System.err.println("❌ Token JWT não fornecido ou inválido.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT não fornecido ou inválido.");
        }

        String email = jwtUtil.extrairEmail(token.replace("Bearer ", ""));
        System.out.println("📧 Email extraído do token: " + email);

        User motorista = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Motorista não encontrado!"));

        System.out.println("✅ Motorista encontrado: " + motorista.getNome());

        // 🚀 Verificar a data antes de salvar
        try {
            System.out.println("📌 Data e hora recebida (String): " + viagem.getHorario());
            viagem.setHorario(viagem.getHorario().atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("America/Sao_Paulo")).toLocalDateTime());
            System.out.println("✅ Data e hora convertida corretamente: " + viagem.getHorario());
        } catch (Exception e) {
            System.err.println("❌ Erro ao converter data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao converter data: " + e.getMessage());
        }

        //  Configura a viagem
        viagem.setMotorista(motorista);
        viagem.setMotoristaEmail(motorista.getEmail());
        viagem.setStatus(TripStatus.DISPONIVEL);
        viagem.setObservacaoDinheiro("Em Especie esta trocado");

        Trip viagemSalva = tripRepository.save(viagem);
        System.out.println("✅ Viagem cadastrada com sucesso: " + viagemSalva.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(viagemSalva);
    }


        /*
        @PostMapping("/cadastrar")
        public ResponseEntity<?> cadastrarViagem(@RequestBody Trip viagem, @RequestHeader("Authorization") String token) {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT não fornecido ou inválido.");
            }

            String email = jwtUtil.extrairEmail(token.replace("Bearer ", ""));
            User motorista = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Motorista não encontrado!"));

            //  Configura a viagem
            viagem.setMotorista(motorista);
            viagem.setMotoristaEmail(motorista.getEmail());
            viagem.setStatus(TripStatus.DISPONIVEL);

            Trip viagemSalva = tripRepository.save(viagem);

            return ResponseEntity.status(HttpStatus.CREATED).body(viagemSalva);
}
         */

    }

