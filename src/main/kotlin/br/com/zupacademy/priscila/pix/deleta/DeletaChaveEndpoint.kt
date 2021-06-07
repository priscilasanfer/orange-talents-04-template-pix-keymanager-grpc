package br.com.zupacademy.priscila.pix.deleta

import br.com.zupacademy.priscila.DeletaChavePixRequest
import br.com.zupacademy.priscila.DeletaChavePixResponse
import br.com.zupacademy.priscila.KeyManagerDeletaServiceGrpc
import br.com.zupacademy.priscila.pix.ChavePixInexistenteException
import br.com.zupacademy.priscila.pix.ChavePixRepository
import br.com.zupacademy.priscila.pix.PermissaoNegadaException
import br.com.zupacademy.priscila.pix.register.toModel
import br.com.zupacademy.priscila.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class DeletaChaveEndpoint(@Inject val repository: ChavePixRepository) :
    KeyManagerDeletaServiceGrpc.KeyManagerDeletaServiceImplBase() {

    private val logger = LoggerFactory.getLogger((this::class.java))

    override fun deleta(
        request: DeletaChavePixRequest,
        responseObserver: StreamObserver<DeletaChavePixResponse>
    ) {

        val deletaChaveRequest = request.toModel()

        val cliente =  repository.findById(deletaChaveRequest.pixId)

        if(cliente.isEmpty){
            logger.info("Chave pix ${request.pixId} não encontrada")
            throw ChavePixInexistenteException("Chave Pix '${request.pixId}' não existe")
        }

        if (cliente.get().clientId.toString() != deletaChaveRequest.clientId.toString()){
            logger.info("Cliente sem permissão para deletar a chave ${request.pixId}")
            throw PermissaoNegadaException("Cliente não tem permissão para apagar essa chave")
        }

        repository.deleteById(request.pixId.toLong())

        logger.info("Chave pix ${request.pixId} deletada")

        responseObserver.onNext(
            DeletaChavePixResponse.newBuilder()
                .setMessage("Teste")
                .build()
        )
        responseObserver.onCompleted()
    }
}
