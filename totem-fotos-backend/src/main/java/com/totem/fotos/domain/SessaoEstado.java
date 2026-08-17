package com.totem.fotos.domain;

public enum SessaoEstado {
    IDLE,
    CAPTURANDO_FOTO,
    AGUARDANDO_CONEXAO_CELULAR,
    REVISANDO_FOTO,
    AGUARDANDO_PAGAMENTO,
    PAGAMENTO_CONFIRMADO,
    IMPRIMINDO,
    ERRO,
    FINALIZADA
}
