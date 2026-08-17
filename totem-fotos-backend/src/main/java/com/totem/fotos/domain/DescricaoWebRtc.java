package com.totem.fotos.domain;

/** Descrição temporária usada apenas para negociar a conexão WebRTC entre os navegadores. */
public record DescricaoWebRtc(String type, String sdp) {
    public boolean validaComo(String tipoEsperado) {
        return tipoEsperado.equals(type)
                && sdp != null
                && !sdp.isBlank()
                && sdp.length() <= 65_536;
    }
}
