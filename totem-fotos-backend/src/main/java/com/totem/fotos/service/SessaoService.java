package com.totem.fotos.service;

import com.totem.fotos.domain.Sessao;
import com.totem.fotos.domain.SessaoEstado;
import com.totem.fotos.domain.ProdutoFoto;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guarda as sessoes ativas em memoria (sem banco de dados).
 * Uma unica instancia de totem tem poucas sessoes simultaneas,
 * entao um Map concorrente resolve sem sobre-engenharia.
 */
@Service
public class SessaoService {

    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();

    public Sessao criar(ProdutoFoto produto) {
        Sessao sessao = new Sessao(produto);
        sessoes.put(sessao.getId(), sessao);
        return sessao;
    }

    public Sessao buscar(String id) {
        Sessao sessao = sessoes.get(id);
        if (sessao == null) {
            throw new IllegalArgumentException("Sessao nao encontrada: " + id);
        }
        return sessao;
    }

    /**
     * Transicao de estado central - qualquer falha em qualquer parte do fluxo
     * cai aqui, o que permite a auto-recuperacao: a sessao vai para ERRO,
     * e apos um curto periodo o totem descarta a sessao e volta pra tela inicial.
     */
    public void transicionar(String id, SessaoEstado novoEstado) {
        Sessao sessao = buscar(id);
        sessao.setEstado(novoEstado);
    }

    public void marcarErro(String id) {
        transicionar(id, SessaoEstado.ERRO);
    }

    /** Chamado apos a impressao ou cancelamento; a foto local é limpa pelo navegador. */
    public void finalizar(String id) {
        Sessao sessao = buscar(id);
        sessao.setEstado(SessaoEstado.FINALIZADA);
        sessoes.remove(id);
    }

    public void remover(String id) {
        sessoes.remove(id);
    }
}
