package com.mobile.carteiravirtual.api

// Data class para desserializar a resposta JSON da API
// Ex: "USDBRL": { "code": "USD", "codein": "BRL", "name": "Dólar Comercial", "bid": "5.10" }
data class CotacaoItem(
    val code: String,
    val codein: String,
    val name: String,
    val bid: String // "bid" é o preço de compra (quanto a moeda de origem vale na de destino)
)
