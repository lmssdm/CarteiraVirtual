package com.mobile.carteiravirtual

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Locale

enum class Moeda {
    BRL, USD, BTC
}

class WalletViewModel : ViewModel() {

    val saldoBRL: LiveData<Double> = Companion.saldoBRL
    val saldoUSD: LiveData<Double> = Companion.saldoUSD
    val saldoBTC: LiveData<Double> = Companion.saldoBTC


    fun temSaldoSuficiente(moeda: Moeda, valor: Double) = Companion.temSaldoSuficiente(moeda, valor)
    fun realizarTransacao(mO: Moeda, vO: Double, mD: Moeda, vD: Double) = Companion.realizarTransacao(mO, vO, mD, vD)
    fun formatarValor(moeda: Moeda, valor: Double) = Companion.formatarValor(moeda, valor)


    companion object {

        private val _saldoBRL = MutableLiveData(100000.00)
        val saldoBRL: LiveData<Double> = _saldoBRL

        private val _saldoUSD = MutableLiveData(50000.00)
        val saldoUSD: LiveData<Double> = _saldoUSD

        private val _saldoBTC = MutableLiveData(0.5000)
        val saldoBTC: LiveData<Double> = _saldoBTC

        fun temSaldoSuficiente(moeda: Moeda, valor: Double): Boolean {
            return when (moeda) {
                Moeda.BRL -> (_saldoBRL.value ?: 0.0) >= valor
                Moeda.USD -> (_saldoUSD.value ?: 0.0) >= valor
                Moeda.BTC -> (_saldoBTC.value ?: 0.0) >= valor
            }
        }
        fun realizarTransacao(
            moedaOrigem: Moeda,
            valorOrigem: Double,
            moedaDestino: Moeda,
            valorDestino: Double
        ) {

            when (moedaOrigem) {
                Moeda.BRL -> _saldoBRL.value = (_saldoBRL.value ?: 0.0) - valorOrigem
                Moeda.USD -> _saldoUSD.value = (_saldoUSD.value ?: 0.0) - valorOrigem
                Moeda.BTC -> _saldoBTC.value = (_saldoBTC.value ?: 0.0) - valorOrigem
            }


            when (moedaDestino) {
                Moeda.BRL -> _saldoBRL.value = (_saldoBRL.value ?: 0.0) + valorDestino
                Moeda.USD -> _saldoUSD.value = (_saldoUSD.value ?: 0.0) + valorDestino
                Moeda.BTC -> _saldoBTC.value = (_saldoBTC.value ?: 0.0) + valorDestino
            }
        }
        fun formatarValor(moeda: Moeda, valor: Double): String {
            return when (moeda) {
                // Usando Locale.getDefault() para formatar corretamente a moeda local (R$)
                Moeda.BRL -> String.format(Locale.getDefault(), "R$ %.2f", valor)
                Moeda.USD -> String.format(Locale.getDefault(), "$ %.2f", valor)
                Moeda.BTC -> String.format(Locale.getDefault(), "BTC %.4f", valor)
            }
        }
    }
}

