package com.br.gsistemas.conexao.repository;

import com.br.gsistemas.conexao.domain.PontoEncontro;
import com.br.gsistemas.conexao.domain.Trip;
import com.br.gsistemas.conexao.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PontoEncontroRepository extends JpaRepository<PontoEncontro, Long> {
    Optional<PontoEncontro> findByViagemAndPassageiro(Trip viagem, User passageiro);
    List<PontoEncontro> findAllByViagem(Trip viagem);

    Optional<PontoEncontro> findTopByPassageiroAndViagemOrderByIdDesc(User passageiro, Trip viagem);

}
