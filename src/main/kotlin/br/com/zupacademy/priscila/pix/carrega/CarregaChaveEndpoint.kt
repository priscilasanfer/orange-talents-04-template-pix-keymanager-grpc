package br.com.zupacademy.priscila.pix.carrega

import br.com.zupacademy.priscila.CarregaChavePixRequest
import br.com.zupacademy.priscila.CarregaChavePixResponse
import br.com.zupacademy.priscila.KeymanagerCarregaGrpcServiceGrpc
import br.com.zupacademy.priscila.integration.bcb.BcbClient
import br.com.zupacademy.priscila.pix.ChavePixRepository
import br.com.zupacademy.priscila.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class CarregaChaveEndpoint(
    @Inject private val repository: ChavePixRepository, // 1
    @Inject private val bcbClient: BcbClient, // 1
    @Inject private val validator: Validator,
) : KeymanagerCarregaGrpcServiceGrpc.KeymanagerCarregaGrpcServiceImplBase() {

    override fun carrega(
        request: CarregaChavePixRequest,
        responseObserver: StreamObserver<CarregaChavePixResponse>
    ) {
        val filtro = request.toModel(validator) // 2
        val chaveInfo = filtro.filtra(repository = repository, bcbClient = bcbClient)

        responseObserver.onNext(CarregaChavePixResponseConverter().convert(chaveInfo)) // 1
        responseObserver.onCompleted()

    }

}
