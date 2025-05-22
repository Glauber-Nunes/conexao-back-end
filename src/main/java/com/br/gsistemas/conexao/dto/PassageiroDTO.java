package com.br.gsistemas.conexao.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PassageiroDTO {
    private Long id;
    private String nome;
    private String email;
    private String pontoEncontro;
    private String fotoUrl;
}
