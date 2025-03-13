package com.br.gsistemas.conexao.dto;

import com.br.gsistemas.conexao.domain.User;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
public class ChatMessageDto {

    private Long id;
    private User remetente;
    private User destinatario;

    private String mensagem;
    private LocalDateTime timestamp;
}
