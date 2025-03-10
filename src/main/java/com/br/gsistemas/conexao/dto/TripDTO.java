package com.br.gsistemas.conexao.dto;

import lombok.*;

@Data
@RequiredArgsConstructor
@Getter
@Setter
public class TripDTO {

    private String origem;
    private String destino;
    private String horario; // String para garantir o parsing correto
    private Double preco;
    private String formaPagamento;
    private String carro;
    private String detalhes;

    private Long qtdVagas;
}
