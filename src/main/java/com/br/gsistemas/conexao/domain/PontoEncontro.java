package com.br.gsistemas.conexao.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pontos_encontro")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PontoEncontro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "viagem_id", nullable = false)
    private Trip viagem;

    @ManyToOne
    @JoinColumn(name = "passageiro_id", nullable = false)
    private User passageiro;

    private String localEncontro;
}
