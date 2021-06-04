package br.com.zupacademy.priscila.itau

import br.com.zupacademy.priscila.ItauAccountType
import br.com.zupacademy.priscila.user.Holder
import br.com.zupacademy.priscila.user.Organization

import br.com.zupacademy.priscila.user.UserDb

data class AccountResponse(
    val tipo: ItauAccountType,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: HolderResponse
) {
    fun toModel(): UserDb {
        return UserDb(
            accountType = tipo,
            organization = Organization(instituicao.nome, instituicao.ispb),
            agency = agencia,
            accountNumber = numero,
            holder = Holder(titular.id, titular.nome, titular.cpf)
        )
    }
}

data class InstituicaoResponse(
    val nome: String,
    val ispb: String
)

data class HolderResponse(
    val id: String,
    val nome: String,
    val cpf: String
)