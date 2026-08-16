package com.totem.fotos.service;

/**
 * Abstrai a integracao com a adquirente de Pix.
 * Troque a implementacao (Stone, PagBank, EFI, Mercado Pago...) sem tocar
 * no resto da aplicacao - so a implementacao concreta muda.
 */
public interface PixService {

    /**
     * Gera uma cobranca Pix e retorna o payload do QR code (copia e cola)
     * junto com o identificador da transacao (txId) para consulta posterior.
     */
    CobrancaPix gerarCobranca(String sessaoId, java.math.BigDecimal valor);

    /**
     * Consulta se uma cobranca ja foi paga.
     */
    boolean foiPaga(String txId);

    record CobrancaPix(String txId, String qrCodePayload, String qrCodeImagemBase64) {}
}
