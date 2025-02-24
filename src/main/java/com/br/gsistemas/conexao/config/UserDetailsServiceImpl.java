package com.br.gsistemas.conexao.config;

import com.br.gsistemas.conexao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired // 🔹 Garante que o Spring Boot injete automaticamente o UserRepository
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        com.br.gsistemas.conexao.domain.User usuario = userRepository.findByEmail(email).orElseThrow(() ->
                new RuntimeException("Usuario Not Found"));


        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getSenha())
                .roles(usuario.getTipo().name()) // Define roles (MOTORISTA ou PASSAGEIRO)
                .build();
    }
}
