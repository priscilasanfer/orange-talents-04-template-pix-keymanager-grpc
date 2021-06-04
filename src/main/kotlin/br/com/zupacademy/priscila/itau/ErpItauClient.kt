package br.com.zupacademy.priscila.itau

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client(value = "\${itau.url}")
interface ErpItauClient {

    @Get("/{clientId}/contas")
    fun consultaDados(
        @PathVariable clientId: String,
        @QueryValue tipo: String
    ): HttpResponse<AccountResponse>

}