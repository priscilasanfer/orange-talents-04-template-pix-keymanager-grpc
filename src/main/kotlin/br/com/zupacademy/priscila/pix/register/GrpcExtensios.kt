package br.com.zupacademy.priscila.pix.register

import br.com.zupacademy.priscila.RegistraChavePixRequest
import br.com.zupacademy.priscila.TipoDeChave.UNKNOWN_TIPO_CHAVE
import br.com.zupacademy.priscila.TipoDeConta.UNKNOWN_TIPO_DE_CONTA
import br.com.zupacademy.priscila.pix.TipoDeChave
import br.com.zupacademy.priscila.pix.TipoDeConta

fun RegistraChavePixRequest.toModel(): NovaChavePix {
    return NovaChavePix(
        clientId = clientId,
        tipo = when (tipoDeChave) {
            UNKNOWN_TIPO_CHAVE -> null
            else -> TipoDeChave.valueOf(tipoDeChave.name)
        },
        chave = chave,
        tipoDeConta = when (tipoDeConta) {
            UNKNOWN_TIPO_DE_CONTA -> null
            else -> TipoDeConta.valueOf(tipoDeConta.name)
        }
    )
}