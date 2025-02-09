package br.com.zupacademy.priscila.pix

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(
        name = "uk_chave_pix",
        columnNames = ["chave"]
    )]
)
class ChavePix(
    @field:NotNull
    @Column(nullable = false)
    val clientId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipo: TipoDeChave,

    @field:NotBlank
    @Column(unique = true, nullable = false)
    var chave: String,

    @field:Valid
    @Embedded
    val conta: ContaAssociada,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoDeConta: TipoDeConta
) {

    @Id
    @GeneratedValue
    val id: Long? = null

    val pixId: UUID = UUID.randomUUID()

    @Column(nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

    /**
     * Verifica se esta chave pertence a este cliente
     */
    fun pertenceAo(clientId: UUID) = this.clientId.equals(clientId)

    fun isAleatoria(): Boolean {
        return tipo == TipoDeChave.RANDOM
    }

    fun atualiza(chave: String): Boolean {
        if (isAleatoria()) {
            this.chave = chave
            return true
        }
        return false
    }
}