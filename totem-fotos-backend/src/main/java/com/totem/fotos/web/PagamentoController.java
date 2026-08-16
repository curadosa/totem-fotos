package com.totem.fotos.web;

import com.totem.fotos.domain.Sessao;
import com.totem.fotos.domain.SessaoEstado;
import com.totem.fotos.service.PixService;
import com.totem.fotos.service.SessaoService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sessoes/{sessaoId}/pagamento")
public class PagamentoController {

    private final SessaoService sessaoService;
    private final PixService pixService;

    public PagamentoController(SessaoService sessaoService, PixService pixService) {
        this.sessaoService = sessaoService;
        this.pixService = pixService;
    }

    /** Gera a cobranca Pix e coloca a sessao em AGUARDANDO_PAGAMENTO. */
    @PostMapping
    public PixService.CobrancaPix gerarCobranca(@PathVariable String sessaoId) {
        Sessao sessao = sessaoService.buscar(sessaoId);
        try {
            PixService.CobrancaPix cobranca = pixService.gerarCobranca(
                    sessaoId,
                    sessao.getProduto().getValor()
            );
            sessao.setPixTxId(cobranca.txId());
            sessaoService.transicionar(sessaoId, SessaoEstado.AGUARDANDO_PAGAMENTO);
            return cobranca;
        } catch (Exception e) {
            // Falha de rede ao gerar cobranca: sessao vai para ERRO,
            // o front mostra a tela de "sem conexao, tentar novamente".
            sessaoService.marcarErro(sessaoId);
            throw e;
        }
    }

    /** Consultado em polling pelo front enquanto o QR code esta na tela. */
    @GetMapping("/status")
    public Map<String, Object> consultarStatus(@PathVariable String sessaoId) {
        Sessao sessao = sessaoService.buscar(sessaoId);
        if (sessao.getPixTxId() == null) {
            return Map.of("pago", false, "estado", sessao.getEstado());
        }
        boolean pago = pixService.foiPaga(sessao.getPixTxId());
        if (pago && sessao.getEstado() != SessaoEstado.PAGAMENTO_CONFIRMADO) {
            sessaoService.transicionar(sessaoId, SessaoEstado.PAGAMENTO_CONFIRMADO);
        }
        return Map.of("pago", pago, "estado", sessao.getEstado());
    }
}
