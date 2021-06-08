package br.com.zupacademy.priscila.pix.register

import br.com.zupacademy.priscila.integration.bcb.BcbClient
import br.com.zupacademy.priscila.integration.itau.ItauClient
import br.com.zupacademy.priscila.pix.ChavePix
import br.com.zupacademy.priscila.pix.ChavePixExistenteException
import br.com.zupacademy.priscila.pix.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val itauClient: ItauClient,
    @Inject val bcbClient: BcbClient
) {

    private val logger = LoggerFactory.getLogger((this::class.java))

    @Transactional
    fun registra(@Valid novaChave: NovaChavePix): ChavePix {

        // 1. Verifica se chave já existe na API
        if (repository.existsByChave(novaChave.chave))
            throw ChavePixExistenteException("Chave Pix '${novaChave.chave}' existente")

        // 2. Busca dados da conta no ERP do Itau
        val responseItau = itauClient.buscaContaPorTipo(
            novaChave.clientId!!,
            novaChave.tipoDeConta!!.name
        )
        check(responseItau.status != HttpStatus.NOT_FOUND) { "Cliente não encontrado no Itaú" }
        check(responseItau.status == HttpStatus.OK) { "Erro ao buscar dados da conta no Itaú" }

        val conta = responseItau.body()!!.toModel()

        // 3 Registra a Chave Globalmente no BCB
        val responseBcb = bcbClient.cadastraChaveBcb(novaChave.toBcbModel(conta))
        check(responseBcb.status != HttpStatus.UNPROCESSABLE_ENTITY) { "Chave Pix já cadastrada no BCB" }
        check(responseBcb.status == HttpStatus.CREATED) { "Não foi possivel cadastrar chave no BCB" }

        // 4 Grava no banco de dados
        val chave = novaChave.toModel(conta, responseBcb.body()!!)

        repository.save(chave)

        return chave
    }

}
