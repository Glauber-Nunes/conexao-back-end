package com.br.gsistemas.conexao.domain;

import com.br.gsistemas.conexao.enums.UserType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @Email(message = "E-mail inválido")
    @Column(unique = true)
    private String email;
    @NotBlank(message = "O telefone é obrigatório")
    private String telefone;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    private String senha;

    @Enumerated(EnumType.STRING)
    private UserType tipo; // MOTORISTA ou PASSAGEIRO

    public User(Long id){
        this.id = id;
    }
}
