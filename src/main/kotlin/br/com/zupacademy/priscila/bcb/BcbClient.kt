package br.com.zupacademy.priscila.bcb

import io.micronaut.http.client.annotation.Client

@Client(value = "\${bcb.url}")
interface BcbClient {

//    @Post(
//        consumes = [MediaType.APPLICATION_XML],
//        produces = [MediaType.APPLICATION_XML]
//    )
//    fun cadastraChave(request: RegisterKeyRequest): HttpResponse<RegisterKeyResponse>
}