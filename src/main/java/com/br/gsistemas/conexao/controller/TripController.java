package com.br.gsistemas.conexao.controller;

import com.br.gsistemas.conexao.config.JwtUtil;
import com.br.gsistemas.conexao.domain.PontoEncontro;
import com.br.gsistemas.conexao.domain.Trip;
import com.br.gsistemas.conexao.domain.User;
import com.br.gsistemas.conexao.dto.PassageiroDTO;
import com.br.gsistemas.conexao.dto.TripDTO;
import com.br.gsistemas.conexao.dto.TripResponseDTO;
import com.br.gsistemas.conexao.enums.TripStatus;
import com.br.gsistemas.conexao.enums.UserType;
import com.br.gsistemas.conexao.repository.PontoEncontroRepository;
import com.br.gsistemas.conexao.repository.TripRepository;
import com.br.gsistemas.conexao.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
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
    private final PontoEncontroRepository pontoEncontroRepository;

    // 🔹 Listar viagens disponíveis
    @GetMapping("/disponiveis")
    public ResponseEntity<?> listarViagens(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String origem,
            @RequestParam(required = false) String destino,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime horario
    ) {
        String email = jwtUtil.extrairEmail(token.replace("Bearer ", ""));
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        List<Trip> viagens;

        if (usuario.getTipo().equals(UserType.PASSAGEIRO)) {
            if (origem == null && destino == null && horario == null) {
                // 🔹 Sem filtros → retorna tudo
                viagens = tripRepository.findByStatus(TripStatus.DISPONIVEL);
            } else {
                // 🔹 Com filtros
                viagens = tripRepository.buscarComFiltros(origem, destino, horario);
            }
        } else {
            viagens = tripRepository.findByMotorista(usuario);
        }

        List<TripResponseDTO> resposta = viagens.stream()
                .map(trip -> new TripResponseDTO(trip, "http://192.168.3.4:8080/conexao/api/uploads/"))
                .toList();

        return ResponseEntity.ok(resposta);
    }



    // 🔹 Entrar na viagem (reduz vagas automaticamente)
    @PostMapping("/{id}/entrar")
    public ResponseEntity<?> entrarNaViagem(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String token,
            @RequestBody(required = false) Map<String, String> requestBody
    ) {
        if (id == null) {
            return ResponseEntity.badRequest().body("O ID da viagem não pode ser nulo!");
        }

        String email = jwtUtil.extrairEmail(token.replace("Bearer ", ""));
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        Trip viagem = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada!"));

        if (viagem.getPassageiros().contains(usuario)) {
            return ResponseEntity.badRequest().body("Você já está nesta viagem!");
        }

        if (viagem.getQtdVagas() <= 0) {
            return ResponseEntity.badRequest().body("Não há mais vagas disponíveis nesta viagem!");
        }

        String localEncontro = requestBody != null ? requestBody.get("localEncontro") : "Não informado";

        // Salvar a entrada na viagem
        viagem.getPassageiros().add(usuario);
        viagem.getDataEntrada().put(email, LocalDateTime.now());
        viagem.setQtdVagas(viagem.getQtdVagas() - 1);
        tripRepository.save(viagem);

        // Criar e salvar o ponto de encontro
        PontoEncontro ponto = new PontoEncontro();
        ponto.setViagem(viagem);
        ponto.setPassageiro(usuario);
        ponto.setLocalEncontro(localEncontro.trim() != null ? localEncontro : "Local não informado");
        pontoEncontroRepository.save(ponto);

        return ResponseEntity.ok("Usuário entrou na viagem e o ponto de encontro foi registrado.");
    }


    // 🔹 Sair da viagem (repõe vaga se for dentro do tempo permitido)
    @DeleteMapping("/{id}/sair")
    public ResponseEntity<?> sairDaViagem(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extrairEmail(token.replace("Bearer ", ""));
        User passageiro = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        Trip viagem = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada!"));

        // 🔹 Verificar se o passageiro está na viagem
        if (!viagem.getPassageiros().contains(passageiro)) {
            return ResponseEntity.badRequest().body("Você não está nesta viagem!");
        }

        // 🔹 Verificar se já passou 1 hora desde a entrada
        LocalDateTime dataEntrada = viagem.getDataEntrada().get(email);
        if (dataEntrada != null && dataEntrada.plusHours(1).isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Você não pode sair da viagem após 1 hora.");
        }

        viagem.getPassageiros().remove(passageiro);
        viagem.getDataEntrada().remove(email); // 🔹 Remove o registro de entrada
        viagem.setQtdVagas(viagem.getQtdVagas() + 1); // 🔹 Repor a vaga disponível
        tripRepository.save(viagem);

        return ResponseEntity.ok("Você saiu da viagem!");
    }

    // 🔹 Obter detalhes da viagem
    @GetMapping("/{id}")
    public ResponseEntity<?> detalhesViagem(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT não fornecido ou inválido.");
        }

        String jwt = token.replace("Bearer ", "");
        String email = jwtUtil.extrairEmail(jwt);

        Trip viagem = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada!"));

        // Monta lista de passageiros com foto e ponto
        List<PassageiroDTO> passageirosDTO = viagem.getPassageiros().stream().map(p -> {
            String ponto = pontoEncontroRepository
                    .findTopByPassageiroAndViagemOrderByIdDesc(p, viagem)
                    .map(PontoEncontro::getLocalEncontro)
                    .orElse("Não informado");

            String fotoUrlCompleta = p.getFotoUrl() != null
                    ? "http://192.168.3.4:8080/conexao/api" + p.getFotoUrl()
                    : null;

            System.out.println("👤 Passageiro: " + p.getNome() + " | Foto: " + fotoUrlCompleta);


            return new PassageiroDTO(p.getUsuarioId(), p.getNome(), p.getEmail(), ponto, fotoUrlCompleta);
        }).toList();

        TripDTO dto = new TripDTO(
                viagem.getId(),
                viagem.getOrigem(),
                viagem.getDestino(),
                viagem.getHorario(),
                email,
                passageirosDTO,
                viagem.getDataEntrada()
        );

        return ResponseEntity.ok(dto);
    }


    // 🔹 Criar viagem
    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarViagem(@RequestBody Trip viagem, @RequestHeader("Authorization") String token) {
        System.out.println("🔹 Recebendo requisição para cadastrar viagem...");

        if (token == null || !token.startsWith("Bearer ")) {
            System.err.println("❌ Token JWT inválido.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT não fornecido ou inválido.");
        }

        String jwt = token.replace("Bearer ", "");

        Claims claims = jwtUtil.extrairClaims(jwt);
        String email = claims.getSubject();

        // 🔹 Forma correta de extrair as authorities:
        List<String> authorities = claims.get("authorities", List.class);

        if (authorities == null || !authoritiesContemRoleMotorista(claims)) {
            System.err.println("❌ Usuário não tem ROLE_MOTORISTA.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas motoristas podem cadastrar viagens.");
        }

        System.out.println("✅ Role MOTORISTA confirmada para: " + email);

        User motorista = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Motorista não encontrado!"));

        viagem.setMotorista(motorista);
        viagem.setMotoristaEmail(motorista.getEmail());
        viagem.setStatus(TripStatus.DISPONIVEL);

        Trip viagemSalva = tripRepository.save(viagem);
        return ResponseEntity.status(HttpStatus.CREATED).body(viagemSalva);
    }

    private boolean authoritiesContemRoleMotorista(Claims claims) {
        List<?> authorities = claims.get("authorities", List.class);
        if (authorities == null) {
            return false;
        }
        return authorities.stream()
                .anyMatch(auth -> "ROLE_MOTORISTA".equals(auth.toString()));
    }

    @GetMapping("/{id}/pontos-encontro")
    public ResponseEntity<?> listarPontosEncontro(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        String email = jwtUtil.extrairEmail(token.replace("Bearer ", ""));
        Trip viagem = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada!"));

        if (!viagem.getMotoristaEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Apenas o motorista pode visualizar os pontos de encontro.");
        }

        List<PontoEncontro> pontos = pontoEncontroRepository.findAllByViagem(viagem);

        return ResponseEntity.ok(pontos);
    }

    // 🔹 Encerrar viagem (somente motorista)
    @PatchMapping("/{id}/encerrar")
    public ResponseEntity<?> encerrarViagem(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT não fornecido ou inválido.");
        }

        String email = jwtUtil.extrairEmail(token.replace("Bearer ", ""));
        User motorista = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        Trip viagem = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada!"));

        if (!viagem.getMotoristaEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas o motorista pode encerrar esta viagem.");
        }

        viagem.setStatus(TripStatus.ENCERRADA);
        tripRepository.save(viagem);

        return ResponseEntity.ok("Viagem encerrada com sucesso.");
    }

}
