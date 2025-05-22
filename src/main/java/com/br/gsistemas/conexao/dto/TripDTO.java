package com.br.gsistemas.conexao.dto;

import com.br.gsistemas.conexao.domain.Trip;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
@Getter
@Setter
public class TripDTO {

    private Long id;
    private String origem;
    private String destino;
    private LocalDateTime horario; // String para garantir o parsing correto
    private Double preco;
    private String formaPagamento;
    private String carro;
    private String detalhes;
    private  String motoristaEmail;

    private Long qtdVagas;
    private List<PassageiroDTO> passageiros;

    private Map<String, LocalDateTime> dataEntrada;


    public TripDTO(Long id, String origem, String destino, LocalDateTime horario, String motoristaEmail, List<PassageiroDTO> passageiros, Map dataEntrada) {
        this.id = id;
        this.origem = origem;
        this.destino = destino;
        this.horario = horario;
        this.motoristaEmail = motoristaEmail;
        this.passageiros = passageiros;
        this.dataEntrada = dataEntrada;
    }

}
