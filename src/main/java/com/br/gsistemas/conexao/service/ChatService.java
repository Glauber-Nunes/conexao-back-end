package com.br.gsistemas.conexao.service;

import com.br.gsistemas.conexao.domain.ChatMessage;
import com.br.gsistemas.conexao.domain.User;
import com.br.gsistemas.conexao.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage enviarMensagem(ChatMessage mensagem) {
        mensagem.setTimestamp(LocalDateTime.now());
        return chatMessageRepository.save(mensagem);
    }


}
