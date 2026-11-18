package com.trabalhoapsoo.sai.dto;

import java.time.LocalDateTime;

import com.trabalhoapsoo.sai.model.VinculoEmpresa;

/**
 * Vínculo (ou convite) entre empresa e profissional.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record VinculoResponse(
    Long id,
    Long empresaId,
    String empresa,
    Long profissionalId,
    String profissional,
    String emailProfissional,
    String status,
    LocalDateTime criadoEm,
    LocalDateTime atualizadoEm
) {

    /**
     * Converte a entidade na resposta da API.
     * 
     * @param v vínculo
     * @return resposta da API
     */
    public static VinculoResponse de(VinculoEmpresa v) {
        return new VinculoResponse(v.getId(), v.getEmpresa().getId(), v.getEmpresa().getNome(),
            v.getProfissional().getId(), v.getProfissional().getNome(), v.getProfissional().getEmail(),
            v.getStatus().name(), v.getCriadoEm(), v.getAtualizadoEm());
    }

} // VinculoResponse
