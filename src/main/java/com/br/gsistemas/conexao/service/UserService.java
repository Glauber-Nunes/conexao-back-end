package com.br.gsistemas.conexao.service;

import com.br.gsistemas.conexao.domain.User;
import com.br.gsistemas.conexao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User cadastrarUsuario(User user) {
        // Verifica se o e-mail já existe
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("E-mail já cadastrado!");
        }

        // Aqui poderíamos encriptar a senha antes de salvar
        return userRepository.save(user);
    }

    public Optional<User> buscarPorId(Long id) {
        return userRepository.findById(id);
    }
}
