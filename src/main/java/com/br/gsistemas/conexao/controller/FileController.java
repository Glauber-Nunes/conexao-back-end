package com.br.gsistemas.conexao.controller;

import com.br.gsistemas.conexao.domain.User;
import com.br.gsistemas.conexao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class FileController {


    private final UserRepository userRepository;
    private final String BASE_URL = "http://192.168.3.4:8080/conexao/api/uploads/";

    private final String BASE_UPLOAD_DIR = "/uploads/";

    @GetMapping("/usuario/{id}")
    public ResponseEntity<?> getUserPhoto(@PathVariable Long id) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        String fotoUrl = user.getFotoUrl();

        if (fotoUrl == null || fotoUrl.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"message\": \"Usuário não possui uma foto cadastrada.\"}");
        }

        // Extrai apenas o nome do arquivo
        String nomeArquivo = Paths.get(fotoUrl).getFileName().toString(); // pega só o nome do arquivo
        String urlCompleta = BASE_URL + nomeArquivo;

        return ResponseEntity.ok().body(Map.of("fotoUrl", urlCompleta));
    }


    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> serveArquivo(@PathVariable String filename) {
        try {
            // Garante que estamos usando apenas o nome do arquivo
            String nomeArquivo = filename.replace("/uploads/", "").replace("uploads/", "");

            Path caminho = Paths.get(System.getProperty("user.dir"), "uploads").resolve(nomeArquivo).normalize();
            Resource recurso = new UrlResource(caminho.toUri());

            if (!recurso.exists() || !recurso.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header("Content-Type", Files.probeContentType(caminho))
                    .body(recurso);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}
