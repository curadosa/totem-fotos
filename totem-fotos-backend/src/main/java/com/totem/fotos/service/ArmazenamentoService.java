package com.totem.fotos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Grava e remove as fotos em disco - nao ha banco de dados.
 * Estrutura: {diretorioBase}/{aaaa-mm-dd}/{sessaoId}/foto.jpg
 */
@Service
public class ArmazenamentoService {

    private final Path diretorioBase;

    public ArmazenamentoService(@Value("${totem.armazenamento.diretorio-base}") String diretorioBase) {
        this.diretorioBase = Path.of(diretorioBase);
    }

    public Path salvarFoto(String sessaoId, MultipartFile arquivo) throws IOException {
        String pasta = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        Path diretorioSessao = diretorioBase.resolve(pasta).resolve(sessaoId);
        Files.createDirectories(diretorioSessao);

        Path caminhoFoto = diretorioSessao.resolve("foto.jpg");
        arquivo.transferTo(caminhoFoto);
        return caminhoFoto;
    }

    /** Usado tanto no "refazer" quanto na limpeza ao encerrar a sessao. */
    public void removerFoto(Path caminhoFoto) {
        if (caminhoFoto == null) return;
        try {
            Files.deleteIfExists(caminhoFoto);
        } catch (IOException e) {
            // Nao interrompe o fluxo por falha de limpeza - so loga.
            // TODO: considerar um job de limpeza periodico como rede de seguranca.
        }
    }
}
