package com.br.gsistemas.conexao.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "remetente_id", nullable = false)
    private User remetente;

    @ManyToOne
    @JoinColumn(name = "destinatario_id", nullable = false)
    private User destinatario;

    private String mensagem;
    private LocalDateTime timestamp;
}
