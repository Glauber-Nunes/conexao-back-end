package com.br.gsistemas.conexao.domain;

import com.br.gsistemas.conexao.enums.TripStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trips")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "motorista_id", nullable = false)
    private User motorista;

    private String origem;
    private String destino;
    @Column(nullable = false)
    private LocalDateTime horario;
    private Double preco;
    private String formaPagamento; // PIX, Dinheiro

    private String detalhes; // Exemplo: "Levo encomendas"

    private String carro;
    private String motoristaEmail;

    @Enumerated(EnumType.STRING)
    private TripStatus status; // DISPONÍVEL, LOTADO, FINALIZADO

    private Long qtdVagas;

    private String observacaoDinheiro;

    @ManyToMany
    @JoinTable(name = "trip_passengers",
            joinColumns = @JoinColumn(name = "trip_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> passageiros = new ArrayList<>();

}
