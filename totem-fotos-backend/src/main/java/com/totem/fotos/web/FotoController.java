package com.totem.fotos.web;

import com.totem.fotos.domain.Sessao;
import com.totem.fotos.domain.SessaoEstado;
import com.totem.fotos.service.ArmazenamentoService;
import com.totem.fotos.service.SessaoService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessoes/{sessaoId}/foto")
public class FotoController {

    private final SessaoService sessaoService;
    private final ArmazenamentoService armazenamentoService;

    public FotoController(SessaoService sessaoService, ArmazenamentoService armazenamentoService) {
        this.sessaoService = sessaoService;
        this.armazenamentoService = armazenamentoService;
    }

    /** Recebe o frame capturado pela camera do proprio totem. */
    @PostMapping
    public Sessao capturar(@PathVariable String sessaoId, @RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        Sessao sessao = sessaoService.buscar(sessaoId);
        var caminho = salvarFoto(sessaoId, arquivo);
        sessao.setCaminhoFoto(caminho);
        sessaoService.transicionar(sessaoId, SessaoEstado.REVISANDO_FOTO);
        return sessao;
    }

    /** Retorna a foto da sessao para as telas de revisao do totem. */
    @GetMapping
    public ResponseEntity<Resource> visualizar(@PathVariable String sessaoId) {
        Sessao sessao = sessaoService.buscar(sessaoId);
        if (sessao.getCaminhoFoto() == null) {
            return ResponseEntity.notFound().build();
        }
        Resource foto = new FileSystemResource(sessao.getCaminhoFoto());
        MediaType tipo = sessao.getCaminhoFoto().toString().endsWith(".png")
                ? MediaType.IMAGE_PNG
                : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(tipo)
                .body(foto);
    }

    /** Gera o token de curta duracao usado no QR code de "enviar do celular". */
    @PostMapping("/celular/iniciar")
    public Map<String, Object> iniciarUploadCelular(@PathVariable String sessaoId) {
        Sessao sessao = sessaoService.buscar(sessaoId);
        String token = UUID.randomUUID().toString();
        sessao.setTokenUploadCelular(token, Instant.now().plusSeconds(300)); // 5 minutos
        sessaoService.transicionar(sessaoId, SessaoEstado.AGUARDANDO_UPLOAD_CELULAR);
        return Map.of("token", token, "expiraEmSegundos", 300);
    }

    /** Endpoint que a pagina aberta no celular do usuario chama para enviar a foto. */
    @PostMapping("/celular/upload")
    public Sessao uploadDoCelular(@PathVariable String sessaoId,
                                   @RequestParam("token") String token,
                                   @RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        Sessao sessao = sessaoService.buscar(sessaoId);
        synchronized (sessao) {
            if (sessao.tokenExpirado()) {
                throw new ResponseStatusException(HttpStatus.GONE, "O link de envio expirou.");
            }
            if (token == null || !token.equals(sessao.getTokenUploadCelular())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Link de envio inválido.");
            }
            if (sessao.getEstado() != SessaoEstado.AGUARDANDO_UPLOAD_CELULAR) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta sessão não está aguardando uma foto.");
            }

            var caminho = salvarFoto(sessaoId, arquivo);
            sessao.setCaminhoFoto(caminho);
            sessao.invalidarTokenUploadCelular();
            sessaoService.transicionar(sessaoId, SessaoEstado.REVISANDO_FOTO);
        }
        return sessao;
    }

    private java.nio.file.Path salvarFoto(String sessaoId, MultipartFile arquivo) throws IOException {
        try {
            return armazenamentoService.salvarFoto(sessaoId, arquivo);
        } catch (ArmazenamentoService.FotoInvalidaException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    /** Botao "Refazer" na tela de revisao - descarta a foto atual e volta pra captura. */
    @DeleteMapping
    public Sessao refazer(@PathVariable String sessaoId) {
        Sessao sessao = sessaoService.buscar(sessaoId);
        armazenamentoService.removerFoto(sessao.getCaminhoFoto());
        sessao.setCaminhoFoto(null);
        sessaoService.transicionar(sessaoId, SessaoEstado.CAPTURANDO_FOTO);
        return sessao;
    }
}
