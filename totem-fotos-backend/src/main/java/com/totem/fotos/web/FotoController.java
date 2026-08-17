package com.totem.fotos.web;

import com.totem.fotos.domain.DescricaoWebRtc;
import com.totem.fotos.domain.Sessao;
import com.totem.fotos.domain.SessaoEstado;
import com.totem.fotos.service.SessaoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

/**
 * Negocia a conexão direta entre celular e totem. Nenhum byte da foto passa
 * por este controller; somente descrições SDP temporárias ficam em memória.
 */
@RestController
@RequestMapping("/api/sessoes/{sessaoId}/foto/celular")
public class FotoController {

    private static final int VALIDADE_SEGUNDOS = 300;
    private final SessaoService sessaoService;

    public FotoController(SessaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    @PostMapping("/iniciar")
    public ConexaoIniciada iniciar(@PathVariable String sessaoId,
                                   @RequestBody DescricaoWebRtc oferta) {
        if (oferta == null || !oferta.validaComo("offer")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Oferta WebRTC inválida.");
        }

        Sessao sessao = sessaoService.buscar(sessaoId);
        synchronized (sessao) {
            String token = UUID.randomUUID().toString();
            sessao.iniciarConexaoCelular(
                    token,
                    Instant.now().plusSeconds(VALIDADE_SEGUNDOS),
                    oferta
            );
            sessaoService.transicionar(sessaoId, SessaoEstado.AGUARDANDO_CONEXAO_CELULAR);
            return new ConexaoIniciada(token, VALIDADE_SEGUNDOS);
        }
    }

    @GetMapping("/conexao")
    public OfertaConexao obterOferta(@PathVariable String sessaoId,
                                     @RequestParam String token) {
        Sessao sessao = sessaoService.buscar(sessaoId);
        synchronized (sessao) {
            validarToken(sessao, token);
            return new OfertaConexao(sessao.getOfertaWebRtc());
        }
    }

    @PostMapping("/conexao/responder")
    public void responder(@PathVariable String sessaoId,
                          @RequestBody RespostaCelular request) {
        if (request == null || request.resposta() == null
                || !request.resposta().validaComo("answer")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resposta WebRTC inválida.");
        }

        Sessao sessao = sessaoService.buscar(sessaoId);
        synchronized (sessao) {
            validarToken(sessao, request.token());
            sessao.setRespostaWebRtc(request.resposta());
        }
    }

    @GetMapping("/conexao/resposta")
    public RespostaConexao obterResposta(@PathVariable String sessaoId) {
        Sessao sessao = sessaoService.buscar(sessaoId);
        synchronized (sessao) {
            if (sessao.tokenExpirado()) {
                throw new ResponseStatusException(HttpStatus.GONE, "O link de envio expirou.");
            }
            return new RespostaConexao(sessao.getRespostaWebRtc());
        }
    }

    @PostMapping("/conexao/concluir")
    public void concluir(@PathVariable String sessaoId) {
        Sessao sessao = sessaoService.buscar(sessaoId);
        synchronized (sessao) {
            sessao.invalidarConexaoCelular();
            sessaoService.transicionar(sessaoId, SessaoEstado.REVISANDO_FOTO);
        }
    }

    private void validarToken(Sessao sessao, String token) {
        if (sessao.tokenExpirado()) {
            throw new ResponseStatusException(HttpStatus.GONE, "O link de envio expirou.");
        }
        if (token == null || !token.equals(sessao.getTokenConexaoCelular())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Link de envio inválido.");
        }
        if (sessao.getEstado() != SessaoEstado.AGUARDANDO_CONEXAO_CELULAR) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A sessão não aguarda uma conexão.");
        }
    }

    public record ConexaoIniciada(String token, int expiraEmSegundos) {}
    public record OfertaConexao(DescricaoWebRtc oferta) {}
    public record RespostaCelular(String token, DescricaoWebRtc resposta) {}
    public record RespostaConexao(DescricaoWebRtc resposta) {}
}
