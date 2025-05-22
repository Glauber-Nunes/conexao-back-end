package com.br.gsistemas.conexao.controller;

import com.br.gsistemas.conexao.domain.ChatMessage;
import com.br.gsistemas.conexao.domain.Trip;
import com.br.gsistemas.conexao.domain.User;
import com.br.gsistemas.conexao.repository.ChatMessageRepository;
import com.br.gsistemas.conexao.repository.TripRepository;
import com.br.gsistemas.conexao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Controller
public class ChatWebSocketController {

    private final ChatMessageRepository chatMessageRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @MessageMapping("/chat/{viagemId}")
    @SendTo("/topic/chat/{viagemId}")
    public ChatMessage sendMessage(ChatMessage message) {
        // Salvar a mensagem no banco de dados
        Trip viagem = tripRepository.findById(message.getViagem().getId())
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada!"));

        User usuario = userRepository.findById(message.getUsuario().getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        message.setViagem(viagem);
        message.setUsuario(usuario);
        message.setTimestamp(LocalDateTime.now());

        return chatMessageRepository.save(message);
    }
}
