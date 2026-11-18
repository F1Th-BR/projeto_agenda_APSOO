package com.trabalhoapsoo.sai.dto;

import com.trabalhoapsoo.sai.model.Empresa;

/**
 * Dados da empresa logada, incluindo o código do link público (RF016).
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record EmpresaPerfilResponse(Long id, String nome, String email, String telefone, String linkPublico, boolean linkAtivo) {

    /**
     * Converte a entidade na resposta da API.
     * 
     * @param e empresa
     * @return resposta da API
     */
    public static EmpresaPerfilResponse de(Empresa e) {
        return new EmpresaPerfilResponse(e.getId(), e.getNome(), e.getEmail(), e.getTelefone(), e.getLinkPublico(), e.isLinkAtivo());
    }

} // EmpresaPerfilResponse
