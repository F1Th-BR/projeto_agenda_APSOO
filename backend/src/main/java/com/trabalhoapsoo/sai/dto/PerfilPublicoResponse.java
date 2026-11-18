package com.trabalhoapsoo.sai.dto;

import java.math.BigDecimal;
import java.util.List;

import com.trabalhoapsoo.sai.service.ServicosAgendamento.ItemLink;

/**
 * O que a cliente vê ao abrir um link de agendamento (RF027, RF028, RF030, RF039).
 * 'tipo' é PROFISSIONAL (a tela pode pular a escolha do profissional) ou EMPRESA.
 * 
 * @param tipo tipo do dono do link
 * @param nome nome do(a) profissional ou da empresa
 * @param itens procedimentos disponíveis, cada um com os profissionais que o realizam
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record PerfilPublicoResponse(String tipo, String nome, List<Item> itens) {

    /**
     * Item de procedimento (possivelmente agrupado).
     * 
     * @param chave identificador do item ("qualquer profissional")
     * @param nome nome mostrado
     * @param opcoes profissionais que realizam o item
     */
    public record Item(String chave, String nome, List<Opcao> opcoes) {
    } // Item

    /**
     * Procedimento de um(a) profissional específico, com a duração e o preço dele(a) (RN03).
     * 
     * @param procedimentoId id do procedimento (use ao agendar)
     * @param profissional nome do(a) profissional
     * @param duracaoMinutos duração em minutos
     * @param preco preço
     */
    public record Opcao(Long procedimentoId, String profissional, long duracaoMinutos, BigDecimal preco) {
    } // Opcao

    /**
     * Converte os itens do serviço na resposta da API.
     * 
     * @param tipo tipo do dono do link
     * @param nome nome do dono
     * @param itens itens do link
     * @return resposta pública
     */
    public static PerfilPublicoResponse de(String tipo, String nome, List<ItemLink> itens) {
        return new PerfilPublicoResponse(tipo, nome, itens.stream()
            .map(i -> new Item(i.chave(), i.nome(), i.opcoes().stream()
                .map(p -> new Opcao(p.getId(), p.getProfissional().getNome(),
                    p.getDuracaoProcedimento().toMinutes(), p.getPreco()))
                .toList()))
            .toList());
    }

} // PerfilPublicoResponse
