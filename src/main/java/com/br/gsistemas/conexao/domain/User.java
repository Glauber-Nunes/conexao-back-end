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
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usuarioId;

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

    @NotBlank(message = "A foto é obrigatória")
    private String fotoUrl;

    public User(Long id){
        this.usuarioId = id;
    }

    @Transient
    private String pontoEncontro;

    public String getPontoEncontro() {
        return pontoEncontro;
    }

    public void setPontoEncontro(String pontoEncontro) {
        this.pontoEncontro = pontoEncontro;
    }

    public User(){

    }
}
