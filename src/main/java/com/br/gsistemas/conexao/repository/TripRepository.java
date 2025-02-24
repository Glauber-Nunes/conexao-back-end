package com.br.gsistemas.conexao.repository;

import com.br.gsistemas.conexao.domain.Trip;
import com.br.gsistemas.conexao.domain.User;
import com.br.gsistemas.conexao.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip,Long> {
    List<Trip> findByStatus(TripStatus status);
    List<Trip> findByMotorista(User motorista);
}
