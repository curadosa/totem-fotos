package com.totem.fotos.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementacao de desenvolvimento - simula pagamento sem chamar adquirente
 * de verdade. Troque por uma implementacao real (StoneApiPixService, etc.)
 * quando integrar com a adquirente escolhida. Ativa apenas no profile "local".
 */
@Service
@Profile("local")
public class PixServiceStub implements PixService {

    @Override
    public CobrancaPix gerarCobranca(String sessaoId, BigDecimal valor) {
        String txId = UUID.randomUUID().toString();
        String payloadFake = "00020126...PIXFAKE..." + txId;
        return new CobrancaPix(txId, payloadFake, null);
    }

    @Override
    public boolean foiPaga(String txId) {
        // Em dev, considera pago apos alguns segundos de existencia do txId.
        return true;
    }
}
