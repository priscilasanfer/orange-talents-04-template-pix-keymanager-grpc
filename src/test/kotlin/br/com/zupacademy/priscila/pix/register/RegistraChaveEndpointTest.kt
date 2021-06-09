package br.com.zupacademy.priscila.pix.register

import br.com.zupacademy.priscila.KeyManagerRegistraServiceGrpc
import br.com.zupacademy.priscila.RegistraChavePixRequest
import br.com.zupacademy.priscila.TipoDeChave
import br.com.zupacademy.priscila.TipoDeConta
import br.com.zupacademy.priscila.integration.bcb.*
import br.com.zupacademy.priscila.integration.itau.DadosDaContaResponse
import br.com.zupacademy.priscila.integration.itau.InstituicaoResponse
import br.com.zupacademy.priscila.integration.itau.ItauClient
import br.com.zupacademy.priscila.integration.itau.TitularResponse
import br.com.zupacademy.priscila.pix.ChavePix
import br.com.zupacademy.priscila.pix.ChavePixRepository
import br.com.zupacademy.priscila.pix.ContaAssociada
import br.com.zupacademy.priscila.pix.TipoChave
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerRegistraServiceGrpc.KeyManagerRegistraServiceBlockingStub,
) {

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @field:Inject
    lateinit var itauClient: ItauClient

    @field:Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val CLIENT_ID = UUID.randomUUID()
    }

    @Test
    internal fun `deve cadastrar uma nova chave pix email`() {
        val request = RegistraChavePixRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("teste@email.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val dadosDaContaRespose = DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A", "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponse("Rafael M C Ponte", "02467781054")
        )

        val bcbRequest = CreatePixKeyRequest(
            keyType = "EMAIL",
            key = "teste@email.com",
            bankAccount = BankAccout(
                participant = "60701190",
                branch = "0001",
                accountNumber = "291900",
                accountType = AccountType.CACC
            ),
            owner = Owner(
                type = OwnerType.NATURAL_PERSON,
                name = "Rafael M C Ponte",
                taxIdNumber = "02467781054"
            )
        )

        val bcbResponse = CreatePixKeyResponse(
            keyType = "EMAIL",
            key = "teste@email.com",
            bankAccount = BankAccout(
                participant = "60701190",
                branch = "0001",
                accountNumber = "291900",
                accountType = AccountType.CACC
            ),
            owner = Owner(
                type = OwnerType.NATURAL_PERSON,
                name = "Rafael M C Ponte",
                taxIdNumber = "02467781054"
            ),
            createdAt = LocalDateTime.now()
        )

        `when`(itauClient.buscaContaPorTipo(request.clientId, request.tipoDeConta.name))
            .thenReturn(HttpResponse.ok(dadosDaContaRespose))

        `when`(bcbClient.cadastraChaveBcb(bcbRequest))
            .thenReturn(HttpResponse.created(bcbResponse))

        val response = grpcClient.registra(request)

        with(response) {
            assertEquals(CLIENT_ID.toString(), clientId)
            assertNotNull(pixId)
            assertTrue(repository.existsByChave(request.chave))
        }
    }

    @Test
    internal fun `deve cadastrar uma nova chave pix telefone`() {
        val request = RegistraChavePixRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.PHONE)
            .setChave("+5511336691555")
            .setTipoDeConta(TipoDeConta.CONTA_POUPANCA)
            .build()

        val dadosDaContaRespose = DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A", "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponse("Rafael M C Ponte", "02467781054")
        )

        val bcbRequest = CreatePixKeyRequest(
            keyType = "PHONE",
            key = "+5511336691555",
            bankAccount = BankAccout(
                participant = "60701190",
                branch = "0001",
                accountNumber = "291900",
                accountType = AccountType.SVGS
            ),
            owner = Owner(
                type = OwnerType.NATURAL_PERSON,
                name = "Rafael M C Ponte",
                taxIdNumber = "02467781054"
            )
        )

        val bcbResponse = CreatePixKeyResponse(
            keyType = "PHONE",
            key = "+5511336691555",
            bankAccount = BankAccout(
                participant = "60701190",
                branch = "0001",
                accountNumber = "291900",
                accountType = AccountType.SVGS
            ),
            owner = Owner(
                type = OwnerType.NATURAL_PERSON,
                name = "Rafael M C Ponte",
                taxIdNumber = "02467781054"
            ),
            createdAt = LocalDateTime.now()
        )

        `when`(itauClient.buscaContaPorTipo(request.clientId, request.tipoDeConta.name))
            .thenReturn(HttpResponse.ok(dadosDaContaRespose))

        `when`(bcbClient.cadastraChaveBcb(bcbRequest))
            .thenReturn(HttpResponse.created(bcbResponse))

        val response = grpcClient.registra(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(repository.existsByChave(request.chave))
            assertEquals(CLIENT_ID.toString(), clientId)
        }
    }

    @Test
    internal fun `deve cadastrar uma nova chave pix Random`() {
        val request = RegistraChavePixRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.RANDOM)
            .setChave("")
            .setTipoDeConta(TipoDeConta.CONTA_POUPANCA)
            .build()

        val dadosDaContaRespose = DadosDaContaResponse(
            tipo = "CONTA_POUPANCA",
            instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A", "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponse("Rafael M C Ponte", "02467781054")
        )

        val bcbRequest = CreatePixKeyRequest(
            keyType = "RANDOM",
            key = "",
            bankAccount = BankAccout(
                participant = "60701190",
                branch = "0001",
                accountNumber = "291900",
                accountType = AccountType.SVGS
            ),
            owner = Owner(
                type = OwnerType.NATURAL_PERSON,
                name = "Rafael M C Ponte",
                taxIdNumber = "02467781054"
            )
        )

        val bcbResponse = CreatePixKeyResponse(
            keyType = "RANDOM",
            key = UUID.randomUUID().toString(),
            bankAccount = BankAccout(
                participant = "60701190",
                branch = "0001",
                accountNumber = "291900",
                accountType = AccountType.SVGS
            ),
            owner = Owner(
                type = OwnerType.NATURAL_PERSON,
                name = "Rafael M C Ponte",
                taxIdNumber = "02467781054"
            ),
            createdAt = LocalDateTime.now()
        )

        `when`(itauClient.buscaContaPorTipo(request.clientId, request.tipoDeConta.name))
            .thenReturn(HttpResponse.ok(dadosDaContaRespose))

        `when`(bcbClient.cadastraChaveBcb(bcbRequest))
            .thenReturn(HttpResponse.created(bcbResponse))

        val response = grpcClient.registra(request)

        with(response) {
            assertNotNull(pixId)
            assertEquals(CLIENT_ID.toString(), clientId)
//            spy(repository).save(any(ChavePix::class.java))
            // TODO veridicar a chave RANDOM que foi gerada

        }
    }

    @Test
    internal fun `deve cadastrar uma nova chave pix cpf`() {
        val request = RegistraChavePixRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.CPF)
            .setChave("47927074040")
            .setTipoDeConta(TipoDeConta.CONTA_POUPANCA)
            .build()

        val dadosDaContaRespose = DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A", "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponse("Rafael M C Ponte", "02467781054")
        )

        val bcbRequest = CreatePixKeyRequest(
            keyType = "CPF",
            key = "47927074040",
            bankAccount = BankAccout(
                participant = "60701190",
                branch = "0001",
                accountNumber = "291900",
                accountType = AccountType.SVGS
            ),
            owner = Owner(
                type = OwnerType.NATURAL_PERSON,
                name = "Rafael M C Ponte",
                taxIdNumber = "02467781054"
            )
        )

        val bcbResponse = CreatePixKeyResponse(
            keyType = "CPF",
            key = "47927074040",
            bankAccount = BankAccout(
                participant = "60701190",
                branch = "0001",
                accountNumber = "291900",
                accountType = AccountType.SVGS
            ),
            owner = Owner(
                type = OwnerType.NATURAL_PERSON,
                name = "Rafael M C Ponte",
                taxIdNumber = "02467781054"
            ),
            createdAt = LocalDateTime.now()
        )

        `when`(itauClient.buscaContaPorTipo(request.clientId, request.tipoDeConta.name))
            .thenReturn(HttpResponse.ok(dadosDaContaRespose))

        `when`(bcbClient.cadastraChaveBcb(bcbRequest))
            .thenReturn(HttpResponse.created(bcbResponse))

        val response = grpcClient.registra(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(repository.existsByChave(request.chave))
            assertEquals(CLIENT_ID.toString(), clientId)

        }
    }

    @Test
    internal fun `nao deve cadastrar uma nova chave pix com email errado`() {
        val request = RegistraChavePixRequest.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("testeemail.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @Test
    internal fun `nao deve cadastrar uma nova chave pix com cpf errado`() {
        val request = RegistraChavePixRequest.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoDeChave(TipoDeChave.CPF)
            .setChave("123456789")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @Test
    internal fun `nao deve cadastrar uma nova chave pix com telefone errado`() {
        val request = RegistraChavePixRequest.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoDeChave(TipoDeChave.PHONE)
            .setChave("11852658")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }

    }

    @Test
    internal fun `nao deve cadastrar uma nova chave pix preenchida quando o tipo for random`() {
        val request = RegistraChavePixRequest.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoDeChave(TipoDeChave.RANDOM)
            .setChave("11852658")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }

    }

    @Test
    internal fun `nao deve cadastrar uma nova chave pix com tipo de chave for invalida`() {
        val request = RegistraChavePixRequest.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoDeChave(TipoDeChave.UNKNOWN_TIPO_CHAVE)
            .setChave("11852658")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @Test
    internal fun `nao deve cadastrar uma nova chave pix com tipo de conta for invalida`() {
        val request = RegistraChavePixRequest.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("teste@email.com")
            .setTipoDeConta(TipoDeConta.UNKNOWN_TIPO_DE_CONTA)
            .build()


        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @Test
    internal fun `nao deve cadastrar uma nova chave pix quando o cliente nao e encontrado no itau`() {
        val request = RegistraChavePixRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("teste@email.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        `when`(itauClient.buscaContaPorTipo(request.clientId, request.tipoDeConta.name))
            .thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado no Itaú", status.description)
        }
    }

    @Test
    internal fun `nao deve cadastrar uma nova chave pix com chave ja existente`() {
        val existente = repository.save(
            ChavePix(
                clientId = CLIENT_ID,
                tipo = TipoChave.EMAIL,
                chave = "email@teste.com",
                tipoDeConta = TipoDeConta.CONTA_CORRENTE,
                conta = ContaAssociada(
                    instituicao = "ITAÚ UNIBANCO S.A",
                    nomeDoTitular = "Rafael M C Ponte",
                    cpfDoTitular = "02467781054",
                    agencia = "0001",
                    numeroDaConta = "291900",
                    ispb = "60701190"
                )
            )
        )

        val request = RegistraChavePixRequest.newBuilder()
            .setClientId(existente.clientId.toString())
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setTipoDeConta(existente.tipoDeConta)
            .setChave(existente.chave)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix '${existente.chave}' existente", status.description)
        }

    }

    @Test
    internal fun `nao deve cadastrar uma nova chave pix quando o itau retornar uma chave UUID em formato invalido`() {
        val request = RegistraChavePixRequest.newBuilder()
            .setClientId("c56dfef4790144fb84e2a2cefb157890")
            .setTipoDeChave(TipoDeChave.RANDOM)
            .setTipoDeConta(TipoDeConta.CONTA_POUPANCA)
            .build()

        val dadosDaContaRespose = DadosDaContaResponse(
            tipo = "CONTA_POUPANCA",
            instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A", "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponse("Rafael M C Ponte", "02467781054")
        )

        val bcbRequest = CreatePixKeyRequest(
            keyType = "RANDOM",
            key = "",
            bankAccount = BankAccout(
                participant = "60701190",
                branch = "0001",
                accountNumber = "291900",
                accountType = AccountType.SVGS
            ),
            owner = Owner(
                type = OwnerType.NATURAL_PERSON,
                name = "Rafael M C Ponte",
                taxIdNumber = "02467781054"
            )
        )

        val bcbResponse = CreatePixKeyResponse(
            keyType = "RANDOM",
            key = UUID.randomUUID().toString(),
            bankAccount = BankAccout(
                participant = "60701190",
                branch = "0001",
                accountNumber = "291900",
                accountType = AccountType.SVGS
            ),
            owner = Owner(
                type = OwnerType.NATURAL_PERSON,
                name = "Rafael M C Ponte",
                taxIdNumber = "02467781054"
            ),
            createdAt = LocalDateTime.now()
        )

        `when`(itauClient.buscaContaPorTipo(request.clientId, request.tipoDeConta.name))
            .thenReturn(HttpResponse.ok(dadosDaContaRespose))


        `when`(bcbClient.cadastraChaveBcb(bcbRequest))
            .thenReturn(HttpResponse.created(bcbResponse))

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Invalid UUID string: ${request.clientId}", status.description)
        }
    }

    @Test
    internal fun `nao deve cadastrar caso a chave ja exista no bcb`() {
        val request = RegistraChavePixRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.RANDOM)
            .setChave("")
            .setTipoDeConta(TipoDeConta.CONTA_POUPANCA)
            .build()

        val dadosDaContaRespose = DadosDaContaResponse(
            tipo = "CONTA_POUPANCA",
            instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A", "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponse("Rafael M C Ponte", "02467781054")
        )

        val bcbRequest = CreatePixKeyRequest(
            keyType = "RANDOM",
            key = "",
            bankAccount = BankAccout(
                participant = "60701190",
                branch = "0001",
                accountNumber = "291900",
                accountType = AccountType.SVGS
            ),
            owner = Owner(
                type = OwnerType.NATURAL_PERSON,
                name = "Rafael M C Ponte",
                taxIdNumber = "02467781054"
            )
        )

        `when`(itauClient.buscaContaPorTipo(request.clientId, request.tipoDeConta.name))
            .thenReturn(HttpResponse.ok(dadosDaContaRespose))

        `when`(bcbClient.cadastraChaveBcb(bcbRequest))
            .thenReturn(HttpResponse.unprocessableEntity())

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Chave Pix já cadastrada no BCB", status.description)
        }


    }

    @Test
    internal fun `nao deve cadastrar caso nao seja possivel cadastrar no BCB`() {
        val request = RegistraChavePixRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.RANDOM)
            .setChave("")
            .setTipoDeConta(TipoDeConta.CONTA_POUPANCA)
            .build()

        val dadosDaContaRespose = DadosDaContaResponse(
            tipo = "CONTA_POUPANCA",
            instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A", "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponse("Rafael M C Ponte", "02467781054")
        )

        val bcbRequest = CreatePixKeyRequest(
            keyType = "RANDOM",
            key = "",
            bankAccount = BankAccout(
                participant = "60701190",
                branch = "0001",
                accountNumber = "291900",
                accountType = AccountType.SVGS
            ),
            owner = Owner(
                type = OwnerType.NATURAL_PERSON,
                name = "Rafael M C Ponte",
                taxIdNumber = "02467781054"
            )
        )

        `when`(itauClient.buscaContaPorTipo(request.clientId, request.tipoDeConta.name))
            .thenReturn(HttpResponse.ok(dadosDaContaRespose))

        `when`(bcbClient.cadastraChaveBcb(bcbRequest))
            .thenReturn(HttpResponse.badRequest())

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Não foi possivel cadastrar chave no BCB", status.description)
        }


    }

    @MockBean(ItauClient::class)
    fun itauClientMock(): ItauClient {
        return mock(ItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bcbClientMock(): BcbClient {
        return mock(BcbClient::class.java)
    }

    @Factory
    class Registra {
        @Singleton
        fun blockingStub(
            @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): KeyManagerRegistraServiceGrpc.KeyManagerRegistraServiceBlockingStub {
            return KeyManagerRegistraServiceGrpc.newBlockingStub(channel)
        }
    }
}




