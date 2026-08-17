package com.totem.fotos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * Grava e remove as fotos em disco - nao ha banco de dados.
 * Estrutura: {diretorioBase}/{aaaa-mm-dd}/{sessaoId}/foto.{jpg|png}
 */
@Service
public class ArmazenamentoService {

    public static final long TAMANHO_MAXIMO_BYTES = 10L * 1024 * 1024;
    private static final long PIXELS_MAXIMOS = 40_000_000L;
    private static final Set<String> FORMATOS_ACEITOS = Set.of("JPEG", "PNG");

    private final Path diretorioBase;

    public ArmazenamentoService(@Value("${totem.armazenamento.diretorio-base}") String diretorioBase) {
        this.diretorioBase = Path.of(diretorioBase);
    }

    public Path salvarFoto(String sessaoId, MultipartFile arquivo) throws IOException {
        FotoValidada foto = validar(arquivo);
        String pasta = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        Path diretorioSessao = diretorioBase.resolve(pasta).resolve(sessaoId);
        Files.createDirectories(diretorioSessao);

        removerFotosAnteriores(diretorioSessao);
        Path caminhoFoto = diretorioSessao.resolve("foto." + foto.extensao());
        arquivo.transferTo(caminhoFoto);
        return caminhoFoto;
    }

    private FotoValidada validar(MultipartFile arquivo) throws IOException {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new FotoInvalidaException("Selecione uma foto para enviar.");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new FotoInvalidaException("A foto deve ter no máximo 10 MB.");
        }

        try (ImageInputStream entrada = ImageIO.createImageInputStream(arquivo.getInputStream())) {
            if (entrada == null) {
                throw new FotoInvalidaException("O arquivo enviado não é uma imagem válida.");
            }

            Iterator<ImageReader> leitores = ImageIO.getImageReaders(entrada);
            if (!leitores.hasNext()) {
                throw new FotoInvalidaException("Envie uma foto JPEG ou PNG válida.");
            }

            ImageReader leitor = leitores.next();
            try {
                leitor.setInput(entrada, true, true);
                String formato = leitor.getFormatName().toUpperCase(Locale.ROOT);
                if (!FORMATOS_ACEITOS.contains(formato)) {
                    throw new FotoInvalidaException("Formato não aceito. Envie uma foto JPEG ou PNG.");
                }

                int largura = leitor.getWidth(0);
                int altura = leitor.getHeight(0);
                if (largura <= 0 || altura <= 0 || (long) largura * altura > PIXELS_MAXIMOS) {
                    throw new FotoInvalidaException("A resolução da foto não é suportada.");
                }
                return new FotoValidada(formato.equals("PNG") ? "png" : "jpg");
            } finally {
                leitor.dispose();
            }
        }
    }

    private void removerFotosAnteriores(Path diretorioSessao) throws IOException {
        Files.deleteIfExists(diretorioSessao.resolve("foto.jpg"));
        Files.deleteIfExists(diretorioSessao.resolve("foto.png"));
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

    private record FotoValidada(String extensao) {}

    public static class FotoInvalidaException extends IllegalArgumentException {
        public FotoInvalidaException(String mensagem) {
            super(mensagem);
        }
    }
}
