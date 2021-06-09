package br.com.zupacademy.priscila.pix.deleta

import br.com.zupacademy.priscila.DeletaChavePixRequest
import br.com.zupacademy.priscila.KeyManagerDeletaServiceGrpc
import br.com.zupacademy.priscila.TipoDeConta
import br.com.zupacademy.priscila.integration.bcb.BcbClient
import br.com.zupacademy.priscila.integration.bcb.DeletePixKeyRequest
import br.com.zupacademy.priscila.integration.bcb.DeletePixKeyResponse
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
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@MicronautTest(transactional = false)
internal class DeletaChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerDeletaServiceGrpc.KeyManagerDeletaServiceBlockingStub
) {

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @field:Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val CLIENT_ID = UUID.randomUUID()
        val PIX_ID = UUID.randomUUID()
    }

    @Test
    internal fun `deve deletar uma chave pix existente`() {
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

        val request = DeletaChavePixRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setPixId(existente.pixId.toString())
            .build()

        val bcbRequest = DeletePixKeyRequest(
            key = existente.tipoDeConta.toString(),
            participant = existente.conta.ispb
        )

        val bcbResponse = DeletePixKeyResponse(
            key = "email@teste.com",
            participant = "60701190",
            deletedAt = LocalDateTime.now()
        )
        Mockito.`when`(bcbClient.deletaChavePixBcb(bcbRequest, existente.chave))
            .thenReturn(HttpResponse.ok(bcbResponse))

        val response = grpcClient.deleta(request)

        assertEquals(CLIENT_ID.toString(), response.clientId.toString())
        assertFalse(repository.existsByChave(existente.pixId.toString()))
    }

    @Test
    internal fun `deve lancar excecao quando a chave nao existir`() {
        val request = DeletaChavePixRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setPixId(PIX_ID.toString())
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix '${PIX_ID}' não existe", status.description)
        }
    }

    @Test
    internal fun `deve lancar excecao quando nao for o dono da chave que tenta excluir a chave`() {
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

        val request = DeletaChavePixRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setPixId(existente.pixId.toString())
            .build()

        val bcbRequest = DeletePixKeyRequest(
            key = existente.tipoDeConta.toString(),
            participant = existente.conta.ispb
        )

        Mockito.`when`(bcbClient.deletaChavePixBcb(bcbRequest, existente.chave))
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
}