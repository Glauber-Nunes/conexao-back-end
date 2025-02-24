package com.br.gsistemas.conexao.repository;

import com.br.gsistemas.conexao.domain.ChatMessage;
import com.br.gsistemas.conexao.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {
    List<ChatMessage> findByRemetenteAndDestinatario(User remetente, User destinatario);
}
