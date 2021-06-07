package br.com.zupacademy.priscila.shared.grpc.handlers

import br.com.zupacademy.priscila.pix.PermissaoNegadaException
import br.com.zupacademy.priscila.shared.grpc.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class PermissaoNegadaExceptionHandler : ExceptionHandler<PermissaoNegadaException> {
    override fun handle(e: PermissaoNegadaException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.PERMISSION_DENIED
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is PermissaoNegadaException
    }

}