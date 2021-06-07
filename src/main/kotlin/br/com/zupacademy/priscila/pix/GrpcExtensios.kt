package br.com.zupacademy.priscila.pix.register

import br.com.zupacademy.priscila.DeletaChavePixRequest
import br.com.zupacademy.priscila.RegistraChavePixRequest
import br.com.zupacademy.priscila.TipoDeChave

import br.com.zupacademy.priscila.TipoDeConta
import br.com.zupacademy.priscila.pix.TipoChave
import br.com.zupacademy.priscila.pix.deleta.DeletaChaveRequest
import java.util.*

fun RegistraChavePixRequest.toModel(): NovaChavePix {
    return NovaChavePix(
        clientId = clientId,
        tipo = when (tipoDeChave) {
            TipoDeChave.UNKNOWN_TIPO_CHAVE -> null
            else -> TipoChave.valueOf(tipoDeChave.name)
        },
        chave = chave,
        tipoDeConta = when (tipoDeConta) {
            TipoDeConta.UNKNOWN_TIPO_DE_CONTA -> null
            else -> TipoDeConta.valueOf(tipoDeConta.name)
        }
    )
}

fun DeletaChavePixRequest.toModel(): DeletaChaveRequest {
    return DeletaChaveRequest(
        clientId = UUID.fromString(clientId),
        pixId = pixId.toLong()
    )
}