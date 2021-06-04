package br.com.zupacademy.priscila.pix.register

import br.com.zupacademy.priscila.TipoDeConta
import br.com.zupacademy.priscila.pix.ContaAssociada
import br.com.zupacademy.priscila.pix.ChavePix
import br.com.zupacademy.priscila.pix.TipoChave
import br.com.zupacademy.priscila.shared.validation.ValidPixKey
import br.com.zupacademy.priscila.shared.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePix(
    @ValidUUID
    @field:NotBlank
    val clientId: String?,

    @field:NotNull
    val tipo: TipoChave?,

    @field:Size(max = 77)
    val chave: String?,

    @field:NotNull
    val tipoDeConta: TipoDeConta?
) {

    fun toModel(conta: ContaAssociada): ChavePix {
        return ChavePix(
            clientId = UUID.fromString(this.clientId),
            tipo = TipoChave.valueOf(this.tipo!!.name),
            chave = if (this.tipo == TipoChave.RANDOM) UUID.randomUUID().toString() else this.chave!!,
            tipoDeConta = TipoDeConta.valueOf(this.tipoDeConta!!.name),
            conta = conta
        )
    }
}
