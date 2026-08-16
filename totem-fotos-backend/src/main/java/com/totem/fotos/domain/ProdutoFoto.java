package com.totem.fotos.domain;

import java.math.BigDecimal;

/** Produtos disponiveis no totem e seus valores unitarios. */
public enum ProdutoFoto {
    POLAROID("Polaroid", new BigDecimal("4.50")),
    NORMAL_10X15("Normal (10x15)", new BigDecimal("5.50")),
    SEIS_FOTOS_3X4("6 fotos 3x4", new BigDecimal("19.90"));

    private final String descricao;
    private final BigDecimal valor;

    ProdutoFoto(String descricao, BigDecimal valor) {
        this.descricao = descricao;
        this.valor = valor;
    }

    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
}
