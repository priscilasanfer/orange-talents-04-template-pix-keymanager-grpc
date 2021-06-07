package br.com.zupacademy.priscila.shared.grpc.handlers

import br.com.zupacademy.priscila.pix.ChavePixInexistenteException
import br.com.zupacademy.priscila.shared.grpc.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixInexistenteExceptionHandler : ExceptionHandler<ChavePixInexistenteException> {
    override fun handle(e: ChavePixInexistenteException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.NOT_FOUND
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixInexistenteException
    }

}