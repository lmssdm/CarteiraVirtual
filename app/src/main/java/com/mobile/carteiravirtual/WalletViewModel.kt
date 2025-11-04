package com.mobile.carteiravirtual

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Locale

// Enum para padronizar os tipos de moeda
enum class Moeda {
    BRL, USD, BTC
}

class WalletViewModel : ViewModel() {

    // LiveData para os saldos
    // Inicia com os valores definidos no PDF (100k BRL, 50k USD, 0.5 BTC)
    private val _saldoBRL = MutableLiveData(100000.00)
    val saldoBRL: LiveData<Double> = _saldoBRL

    private val _saldoUSD = MutableLiveData(50000.00)
    val saldoUSD: LiveData<Double> = _saldoUSD

    private val _saldoBTC = MutableLiveData(0.5000)
    val saldoBTC: LiveData<Double> = _saldoBTC

    /**
     * Verifica se há saldo suficiente para uma transação.
     */
    fun temSaldoSuficiente(moeda: Moeda, valor: Double): Boolean {
        return when (moeda) {
            Moeda.BRL -> (_saldoBRL.value ?: 0.0) >= valor
            Moeda.USD -> (_saldoUSD.value ?: 0.0) >= valor
            Moeda.BTC -> (_saldoBTC.value ?: 0.0) >= valor
        }
    }

    /**
     * Realiza a transação, atualizando os saldos de origem e destino.
     */
    fun realizarTransacao(
        moedaOrigem: Moeda,
        valorOrigem: Double,
        moedaDestino: Moeda,
        valorDestino: Double
    ) {
        // Subtrai da origem
        when (moedaOrigem) {
            Moeda.BRL -> _saldoBRL.value = (_saldoBRL.value ?: 0.0) - valorOrigem
            Moeda.USD -> _saldoUSD.value = (_saldoUSD.value ?: 0.0) - valorOrigem
            Moeda.BTC -> _saldoBTC.value = (_saldoBTC.value ?: 0.0) - valorOrigem
        }

        // Adiciona ao destino
        when (moedaDestino) {
            Moeda.BRL -> _saldoBRL.value = (_saldoBRL.value ?: 0.0) + valorDestino
            Moeda.USD -> _saldoUSD.value = (_saldoUSD.value ?: 0.0) + valorDestino
            Moeda.BTC -> _saldoBTC.value = (_saldoBTC.value ?: 0.0) + valorDestino
        }
    }

    /**
     * Formata o valor monetário de acordo com a moeda.
     * BRL/USD: 2 casas decimais
     * BTC: 4 casas decimais
     */
    fun formatarValor(moeda: Moeda, valor: Double): String {
        return when (moeda) {
            // Usando Locale.getDefault() para formatar corretamente a moeda local (R$)
            Moeda.BRL -> String.format(Locale.getDefault(), "R$ %.2f", valor)
            Moeda.USD -> String.format(Locale.getDefault(), "$ %.2f", valor)
            Moeda.BTC -> String.format(Locale.getDefault(), "BTC %.4f", valor)
        }
    }
}
