package com.br.gsistemas.conexao.repository;

import com.br.gsistemas.conexao.domain.Trip;
import com.br.gsistemas.conexao.domain.User;
import com.br.gsistemas.conexao.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip,Long> {
    List<Trip> findByStatus(TripStatus status);
    List<Trip> findByMotorista(User motorista);

    @Query("SELECT t FROM Trip t WHERE t.status = 'DISPONIVEL' " +
            "AND (:origem IS NULL OR LOWER(t.origem) LIKE LOWER(CONCAT('%', :origem, '%'))) " +
            "AND (:destino IS NULL OR LOWER(t.destino) LIKE LOWER(CONCAT('%', :destino, '%'))) " +
            "AND (:horario IS NULL OR t.horario >= :horario)")
    List<Trip> buscarComFiltros(@Param("origem") String origem,
                                @Param("destino") String destino,
                                @Param("horario") LocalDateTime horario);

}
