package com.totem.fotos.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa uma sessao de uso do totem, do inicio ate a impressao.
 * Nao persiste em banco. A foto permanece somente no navegador do totem.
 */
public class Sessao {

    private final String id;
    private final Instant criadaEm;
    private final ProdutoFoto produto;
    private SessaoEstado estado;
    private String tokenConexaoCelular;
    private Instant tokenExpiraEm;
    private DescricaoWebRtc ofertaWebRtc;
    private DescricaoWebRtc respostaWebRtc;
    private String pixTxId;

    public Sessao(ProdutoFoto produto) {
        this.id = UUID.randomUUID().toString();
        this.criadaEm = Instant.now();
        this.produto = produto;
        this.estado = SessaoEstado.IDLE;
    }

    public String getId() { return id; }
    public Instant getCriadaEm() { return criadaEm; }
    public ProdutoFoto getProduto() { return produto; }
    public SessaoEstado getEstado() { return estado; }
    public void setEstado(SessaoEstado estado) { this.estado = estado; }
    @JsonIgnore
    public String getTokenConexaoCelular() { return tokenConexaoCelular; }
    public void iniciarConexaoCelular(String token, Instant expiraEm, DescricaoWebRtc oferta) {
        this.tokenConexaoCelular = token;
        this.tokenExpiraEm = expiraEm;
        this.ofertaWebRtc = oferta;
        this.respostaWebRtc = null;
    }
    public void invalidarConexaoCelular() {
        this.tokenConexaoCelular = null;
        this.tokenExpiraEm = null;
        this.ofertaWebRtc = null;
        this.respostaWebRtc = null;
    }
    public boolean tokenExpirado() {
        return tokenExpiraEm == null || Instant.now().isAfter(tokenExpiraEm);
    }
    @JsonIgnore
    public DescricaoWebRtc getOfertaWebRtc() { return ofertaWebRtc; }
    @JsonIgnore
    public DescricaoWebRtc getRespostaWebRtc() { return respostaWebRtc; }
    public void setRespostaWebRtc(DescricaoWebRtc respostaWebRtc) { this.respostaWebRtc = respostaWebRtc; }
    @JsonIgnore
    public String getPixTxId() { return pixTxId; }
    public void setPixTxId(String pixTxId) { this.pixTxId = pixTxId; }
}
