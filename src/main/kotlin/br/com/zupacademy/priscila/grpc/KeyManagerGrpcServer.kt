package br.com.zupacademy.priscila.grpc

import br.com.zupacademy.priscila.*
import br.com.zupacademy.priscila.bcb.BcbClient
import br.com.zupacademy.priscila.itau.ErpItauClient
import br.com.zupacademy.priscila.user.UserRepository
import com.google.protobuf.Any
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyManagerGrpcServer(
    val bcbClient: BcbClient,
    val erpItauClient: ErpItauClient,
    val userRepository: UserRepository
) : KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    private val logger = LoggerFactory.getLogger((KeyManagerGrpcServer::class.java))

    override fun getAccountInfo(
        request: GetAccountInfoRequest,
        responseObserver: StreamObserver<GetAccountInfoResponse>
    ) {

        logger.info("Consultando dados do cliente: ${request.clienteId}")

        try {
            val itauResponse = erpItauClient.consultaDados(request.clienteId, request.tipo.name)

            if (itauResponse.status == HttpStatus.NOT_FOUND) {
                val statusProto = com.google.rpc.Status.newBuilder()
                    .setCode(Code.NOT_FOUND.number)
                    .setMessage("Cliente não encontrado")
                    .addDetails(
                        Any.pack(
                            ErrorDetials.newBuilder()
                                .setCode(404)
                                .setMessage("Cliente não encontrado")
                                .build()
                        )
                    )
                    .build()

                val e = StatusProto.toStatusRuntimeException(statusProto)

                responseObserver?.onError(e)
            }

            val user = itauResponse.body()!!.toModel()
            userRepository.save(user)

            responseObserver.onNext(
                GetAccountInfoResponse.newBuilder()
                    .setTipo(user.accountType)
                    .setAgencia(user.agency)
                    .setNumero(user.accountNumber)
                    .setInstituicao(
                        Instituicao.newBuilder()
                            .setNome(user.organization.organizationName)
                            .setIspb(user.organization.ispb)
                            .build()

                    ).setTitular(
                        Titular.newBuilder()
                            .setId(user.holder.holderId)
                            .setNome(user.holder.holderName)
                            .setCpf(user.holder.holderDocument)
                            .build()
                    )
                    .build()
            )
            responseObserver.onCompleted()
            logger.info("Dados do cliente foram salvos: ${request.clienteId}")

        } catch (e: Exception) {
            responseObserver?.onError(
                Status.INTERNAL
                    .withDescription(e.message)
                    .withCause(e)
                    .asRuntimeException()
            )
        }
    }
}
