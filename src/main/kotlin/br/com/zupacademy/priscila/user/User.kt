package br.com.zupacademy.priscila.user

import br.com.zupacademy.priscila.ItauAccountType
import javax.persistence.*

@Entity
class UserDb(
    @field:Enumerated(EnumType.STRING)
    val accountType: ItauAccountType,
    @field: Embedded
    val organization: Organization,
    val agency: String,
    val accountNumber: String,
    @field: Embedded
    val holder: Holder

) {
    @Id
    @GeneratedValue
    var id: Long? = null
}

@Embeddable
class Organization(
    val organizationName: String,
    val ispb: String,
)

@Embeddable
class Holder(
    val holderId: String,
    val holderName: String,
    val holderDocument: String
)



