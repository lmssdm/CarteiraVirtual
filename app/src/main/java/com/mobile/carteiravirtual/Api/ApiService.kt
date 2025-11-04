package com.mobile.carteiravirtual.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    /**
     * Busca a cotação mais recente para um par de moedas.
     * @param moedas O par de moedas, ex: "USD-BRL"
     */
    @GET("json/last/{moedas}")
    suspend fun getCotacao(
        @Path("moedas") moedas: String
    ): Response<Map<String, CotacaoItem>>
}
