package com.trabalhoapsoo.sai.dto;

import com.trabalhoapsoo.sai.model.Profissional;

/**
 * Dados do perfil do(a) profissional logado(a).
 * 'linkPublico' é o código usado na URL de agendamento: /publico/{linkPublico}.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record PerfilResponse(Long id, String nome, String email, String telefone, String linkPublico,
                             int antecedenciaLembreteMinutos, int prazoCancelamentoHoras) {

    /**
     * Converte a entidade na resposta da API.
     * 
     * @param p profissional
     * @return resposta da API
     */
    public static PerfilResponse de(Profissional p) {
        return new PerfilResponse(p.getId(), p.getNome(), p.getEmail(), p.getTelefone(), p.getLinkPublico(),
            p.getAntecedenciaLembreteMinutos(), p.getPrazoCancelamentoHoras());
    }

} // PerfilResponse
