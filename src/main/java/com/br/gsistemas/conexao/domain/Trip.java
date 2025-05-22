package com.br.gsistemas.conexao.domain;

import com.br.gsistemas.conexao.enums.TripStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private TripStatus status; // DISPONÍVEL, LOTADO, FINALIZADO ETC..

    private Long qtdVagas;

    private String observacaoDinheiro;

    @ElementCollection
    private Map<String, LocalDateTime> dataEntrada = new HashMap<>();

    // Novo atributo para guardar pontos de encontro por passageiro
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "passageiro_email")
    @Column(name = "ponto_encontro")
    private Map<String, String> pontosEncontro = new HashMap<>();

    @ManyToMany
    @JoinTable(name = "trip_passengers",
            joinColumns = @JoinColumn(name = "trip_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> passageiros = new ArrayList<>();

}
