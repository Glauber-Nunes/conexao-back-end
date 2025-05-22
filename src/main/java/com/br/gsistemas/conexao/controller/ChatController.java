package com.br.gsistemas.conexao.controller;

import com.br.gsistemas.conexao.config.JwtUtil;
import com.br.gsistemas.conexao.domain.ChatMessage;
import com.br.gsistemas.conexao.domain.Trip;
import com.br.gsistemas.conexao.domain.User;
import com.br.gsistemas.conexao.repository.ChatMessageRepository;
import com.br.gsistemas.conexao.repository.TripRepository;
import com.br.gsistemas.conexao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final TripRepository tripRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;


    //Endpoint para enviar mensagem no chat da viagem
    @PostMapping("/enviar")
    public ResponseEntity<?> enviarMensagem(@RequestHeader("Authorization") String token,
                                            @RequestBody ChatMessage mensagem) {
        String email = jwtUtil.extrairEmail(token.replace("Bearer ", ""));
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        Trip viagem = tripRepository.findById(mensagem.getViagem().getId())
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada!"));

        mensagem.setUsuario(usuario);
        mensagem.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(mensagem);

        // Retorna o nome do usuário no JSON da resposta
        Map<String, Object> response = new HashMap<>();
        response.put("mensagem", mensagem.getMensagem());
        response.put("usuario", Map.of("nome", usuario.getNome())); // Nome do usuário
        response.put("timestamp", mensagem.getTimestamp());

        return ResponseEntity.ok(response);
    }


    //Endpoint para listar mensagens de um chat de uma viagem específica
    @GetMapping("/{viagemId}")
    public ResponseEntity<List<ChatMessage>> listarMensagens(@PathVariable Long viagemId) {
        List<ChatMessage> mensagens = chatMessageRepository.findByViagemId(viagemId);
        return ResponseEntity.ok(mensagens);
    }
}
