package com.br.gsistemas.conexao.controller;

import com.br.gsistemas.conexao.domain.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatWebSocketController {

    @MessageMapping("/enviarMensagem")
    @SendTo("/chat/public")
    public ChatMessage enviarMensagem(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        return message;
    }
}
