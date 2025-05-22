package com.br.gsistemas.conexao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserWithPontoEncontroDTO {

    private String nome;
    private String email;
    private String pontoEncontro;
}
