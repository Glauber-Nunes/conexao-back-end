package com.br.gsistemas.conexao.dto;

import com.br.gsistemas.conexao.enums.UserType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserDto {

    private Long usuarioId;

    private String nome;

    private String email;
    private String telefone;

    private String senha;
    private UserType tipo; // MOTORISTA ou PASSAGEIRO
}
