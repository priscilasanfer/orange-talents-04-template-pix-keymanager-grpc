package br.com.zupacademy.priscila.pix.deleta

import br.com.zupacademy.priscila.DeletaChavePixRequest
import br.com.zupacademy.priscila.KeyManagerDeletaServiceGrpc
import br.com.zupacademy.priscila.integration.bcb.BcbClient
import br.com.zupacademy.priscila.integration.bcb.DeletePixKeyRequest
import br.com.zupacademy.priscila.integration.bcb.DeletePixKeyResponse
import br.com.zupacademy.priscila.pix.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@MicronautTest(transactional = false)
internal class DeletaChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerDeletaServiceGrpc.KeyManagerDeletaServiceBlockingStub
) {

    @field:Inject
    lateinit var bcbClient: BcbClient

    lateinit var CHAVE_EXISTENTE: ChavePix

    @BeforeEach
    fun setup() {
        CHAVE_EXISTENTE = repository.save(
            chave(
                tipo = TipoDeChave.EMAIL,
                chave = "rponte@gmail.com",
                clienteId = UUID.randomUUID()
            )
        )
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    internal fun `deve deletar uma chave pix existente`() {
        val request = DeletaChavePixRequest.newBuilder()
            .setClientId(CHAVE_EXISTENTE.clientId.toString())
            .setPixId(CHAVE_EXISTENTE.pixId.toString())
            .build()

        val bcbResponse = DeletePixKeyResponse(
            key = "email@teste.com",
            participant = "60701190",
            deletedAt = LocalDateTime.now()
        )
        `when`(
            bcbClient.deletaChavePixBcb(
                DeletePixKeyRequest(
                    key = "rponte@gmail.com"
                ), "rponte@gmail.com"
            )
        )
            .thenReturn(HttpResponse.ok(bcbResponse))

        val response = grpcClient.deleta(request)

        assertEquals(CHAVE_EXISTENTE.clientId.toString(), response.clientId.toString())
        assertFalse(repository.existsByChave(CHAVE_EXISTENTE.pixId.toString()))
    }

    @Test
    internal fun `deve lancar excecao quando a chave nao existir`() {
        val randomUUID = UUID.randomUUID().toString()

        val request = DeletaChavePixRequest.newBuilder()
            .setClientId(CHAVE_EXISTENTE.clientId.toString())
            .setPixId(randomUUID)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix '${randomUUID}' não existe", status.description)
        }
    }

    @Test
    internal fun `deve lancar excecao quando nao for o dono da chave que tenta excluir a chave`() {
        val existente = repository.save(
            ChavePix(
                clientId = CHAVE_EXISTENTE.clientId,
                tipo = TipoDeChave.EMAIL,
                chave = "email@teste.com",
                tipoDeConta = TipoDeConta.CONTA_CORRENTE,
                conta = ContaAssociada(
                    instituicao = "ITAÚ UNIBANCO S.A",
                    nomeDoTitular = "Rafael M C Ponte",
                    cpfDoTitular = "02467781054",
                    agencia = "0001",
                    numeroDaConta = "291900"
                )
            )
        )

        val request = DeletaChavePixRequest.newBuilder()
            .setClientId(UUID.randomUUID().toString())
            .setPixId(existente.pixId.toString())
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(request)
        }

        with(error) {
            assertEquals(Status.PERMISSION_DENIED.code, status.code)
            assertEquals("Cliente não tem permissão para apagar essa chave", status.description)
        }
    }

    @Test
    internal fun `deve lancar excecao quando possivel remover chave no bcb`() {
        val request = DeletaChavePixRequest.newBuilder()
            .setClientId(CHAVE_EXISTENTE.clientId.toString())
            .setPixId(CHAVE_EXISTENTE.pixId.toString())
            .build()

        `when`(
            bcbClient.deletaChavePixBcb(
                DeletePixKeyRequest(
                    key = "rponte@gmail.com"
                ), "rponte@gmail.com"
            )
        )
            .thenReturn(HttpResponse.unprocessableEntity())


        val error = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(request)
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Falha na remoção de chave no BCB", status.description)
        }
    }

    @MockBean(BcbClient::class)
    fun bcbClientMock(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class Deleta {
        @Singleton
        fun blockingStub(
            @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): KeyManagerDeletaServiceGrpc.KeyManagerDeletaServiceBlockingStub {
            return KeyManagerDeletaServiceGrpc.newBlockingStub(channel)
        }
    }


    private fun chave(
        tipo: TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clientId = clienteId,
            tipo = tipo,
            chave = chave,
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "12345678900",
                agencia = "1218",
                numeroDaConta = "123456"
            )
        )
    }

}