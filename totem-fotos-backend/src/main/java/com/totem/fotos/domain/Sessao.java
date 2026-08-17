package com.totem.fotos.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

/**
 * Representa uma sessao de uso do totem, do inicio ate a impressao.
 * Nao persiste em banco - vive em memoria e referencia um arquivo em disco.
 */
public class Sessao {

    private final String id;
    private final Instant criadaEm;
    private final ProdutoFoto produto;
    private SessaoEstado estado;
    private Path caminhoFoto;
    private String tokenUploadCelular;
    private Instant tokenExpiraEm;
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
    public Path getCaminhoFoto() { return caminhoFoto; }
    public void setCaminhoFoto(Path caminhoFoto) { this.caminhoFoto = caminhoFoto; }
    @JsonIgnore
    public String getTokenUploadCelular() { return tokenUploadCelular; }
    public void setTokenUploadCelular(String token, Instant expiraEm) {
        this.tokenUploadCelular = token;
        this.tokenExpiraEm = expiraEm;
    }
    public void invalidarTokenUploadCelular() {
        this.tokenUploadCelular = null;
        this.tokenExpiraEm = null;
    }
    public boolean tokenExpirado() {
        return tokenExpiraEm == null || Instant.now().isAfter(tokenExpiraEm);
    }
    @JsonIgnore
    public String getPixTxId() { return pixTxId; }
    public void setPixTxId(String pixTxId) { this.pixTxId = pixTxId; }
}
