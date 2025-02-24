package com.br.gsistemas.conexao.service;

import com.br.gsistemas.conexao.config.JwtUtil;
import com.br.gsistemas.conexao.domain.User;
import com.br.gsistemas.conexao.dto.LoginRequestDto;
import com.br.gsistemas.conexao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;

    public String autenticarUsuario(LoginRequestDto loginRequest) {
        Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());

        if (user.isPresent() && user.get().getSenha().equals(loginRequest.getSenha())) {
            return jwtUtil.gerarToken(user.get().getEmail());
        } else {
            throw new RuntimeException("Credenciais inválidas!");
        }
    }
}
