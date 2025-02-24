package com.br.gsistemas.conexao.service;

import com.br.gsistemas.conexao.domain.Trip;
import com.br.gsistemas.conexao.enums.TripStatus;
import com.br.gsistemas.conexao.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;

    public Trip cadastrarViagem(Trip trip) {
        trip.setStatus(TripStatus.DISPONIVEL);
        return tripRepository.save(trip);
    }

    public List<Trip> listarViagensDisponiveis() {
        return tripRepository.findByStatus(TripStatus.DISPONIVEL);
    }

    public void finalizarViagem(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada!"));
        trip.setStatus(TripStatus.FINALIZADO);
        tripRepository.save(trip);
    }
}
