package com.totem.fotos.web;

import com.totem.fotos.domain.Sessao;
import com.totem.fotos.domain.ProdutoFoto;
import com.totem.fotos.service.SessaoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessoes")
public class SessaoController {

    private final SessaoService sessaoService;

    public SessaoController(SessaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    @PostMapping
    public Sessao criar(@RequestBody CriarSessaoRequest request) {
        if (request == null || request.produto() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um produto");
        }
        return sessaoService.criar(request.produto());
    }

    public record CriarSessaoRequest(ProdutoFoto produto) {}

    @GetMapping("/{id}")
    public Sessao buscar(@PathVariable String id) {
        return sessaoService.buscar(id);
    }

    /** Chamado apos a impressao; encerra os metadados temporarios da sessao. */
    @PostMapping("/{id}/finalizar")
    public void finalizar(@PathVariable String id) {
        sessaoService.finalizar(id);
    }
}
