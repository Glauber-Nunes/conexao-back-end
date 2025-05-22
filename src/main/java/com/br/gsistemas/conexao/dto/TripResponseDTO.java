package com.br.gsistemas.conexao.dto;

import com.br.gsistemas.conexao.domain.Trip;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class TripResponseDTO {

    private Long id;
    private String origem;
    private String destino;
    private LocalDateTime horario;
    private Double preco;
    private String formaPagamento;
    private String motoristaNome;
    private String motoristaFotoUrl;
    private Long qtdVagas;
    private String status;

    public TripResponseDTO(Trip trip, String baseUrl) {
        this.id = trip.getId();
        this.origem = trip.getOrigem();
        this.destino = trip.getDestino();
        this.horario = trip.getHorario();
        this.preco = trip.getPreco();
        this.formaPagamento = trip.getFormaPagamento();
        this.motoristaNome = trip.getMotorista().getNome();
        this.qtdVagas = trip.getQtdVagas();
        this.status = trip.getStatus().name();

        String foto = trip.getMotorista().getFotoUrl();
        if (foto != null && !foto.isEmpty()) {
            String nomeFoto = foto.replace("/uploads/", "");
            this.motoristaFotoUrl = baseUrl + nomeFoto;
        } else {
            this.motoristaFotoUrl = null;
        }
    }
}
