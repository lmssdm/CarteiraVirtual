package com.mobile.carteiravirtual

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.carteiravirtual.api.RetrofitClient
import kotlinx.coroutines.launch
import java.util.Locale

// Enum para padronizar os tipos de moeda
enum class Moeda {
    BRL, USD, BTC
}

// Classe selada para gerenciar o estado da UI de conversão
sealed class EstadoConversao {
    object Ocioso : EstadoConversao()
    object Carregando : EstadoConversao()
    data class Sucesso(val moedaDestino: Moeda, val valorDestino: Double) : EstadoConversao()
    data class Erro(val mensagem: Int) : EstadoConversao() // Usando Int para R.string
}

class WalletViewModel : ViewModel() {

    // --- Gestão de Estado da Conversão ---
    private val _estadoConversao = MutableLiveData<EstadoConversao>(EstadoConversao.Ocioso)
    val estadoConversao: LiveData<EstadoConversao> = _estadoConversao

    /**
     * Ponto de entrada principal para a Activity iniciar uma conversão.
     */
    fun iniciarConversao(moedaOrigem: Moeda, moedaDestino: Moeda, valorOrigemStr: String) {
        // 1. Validar entradas
        val valorOrigem = valorOrigemStr.toDoubleOrNull()
        if (valorOrigem == null || valorOrigem <= 0.0) {
            _estadoConversao.value = EstadoConversao.Erro(R.string.erro_valor_invalido)
            return
        }

        if (moedaOrigem == moedaDestino) {
            _estadoConversao.value = EstadoConversao.Erro(R.string.erro_mesma_moeda)
            return
        }

        // 2. Verificar saldo
        if (!temSaldoSuficiente(moedaOrigem, valorOrigem)) {
            _estadoConversao.value = EstadoConversao.Erro(R.string.erro_saldo_insuficiente)
            return
        }

        // 3. Buscar cotação
        buscarCotacao(moedaOrigem, moedaDestino, valorOrigem)
    }

    /**
     * Busca a cotação na API, tratando a lógica de inversão do Bitcoin.
     */
    private fun buscarCotacao(moedaOrigem: Moeda, moedaDestino: Moeda, valorOrigem: Double) {
        _estadoConversao.value = EstadoConversao.Carregando

        viewModelScope.launch {
            try {
                // Lógica de inversão: Se não houver par direto (ex: BRL-BTC),
                // buscamos o inverso (BTC-BRL) e definimos 'inverterTaxa'.
                var parMoedaApi: String
                var chaveRespostaApi: String
                var inverterTaxa = false

                if (moedaOrigem == Moeda.BTC || moedaDestino == Moeda.BTC) {
                    if (moedaOrigem == Moeda.BTC) {
                        // API suporta BTC-BRL e BTC-USD
                        parMoedaApi = "$moedaOrigem-$moedaDestino"
                        chaveRespostaApi = "$moedaOrigem$moedaDestino"
                    } else {
                        // API *não* suporta BRL-BTC ou USD-BTC.
                        // Devemos inverter a chamada.
                        parMoedaApi = "$moedaDestino-$moedaOrigem"
                        chaveRespostaApi = "$moedaDestino$moedaOrigem"
                        inverterTaxa = true
                    }
                } else {
                    // Lógica normal para BRL-USD ou USD-BRL
                    parMoedaApi = "$moedaOrigem-$moedaDestino"
                    chaveRespostaApi = "$moedaOrigem$moedaDestino"
                }

                // Chamada de Rede
                val response = RetrofitClient.instance.getCotacao(parMoedaApi)

                if (response.isSuccessful && response.body() != null) {
                    val cotacaoItem = response.body()!![chaveRespostaApi]

                    if (cotacaoItem != null) {
                        val taxaApi = cotacaoItem.bid.toDoubleOrNull()
                        if (taxaApi != null && taxaApi > 0) {

                            // Aplica a taxa (invertida ou não)
                            val taxaFinal = if (inverterTaxa) (1 / taxaApi) else taxaApi
                            val valorDestino = valorOrigem * taxaFinal

                            // Sucesso! Realiza a transação e atualiza o estado
                            realizarTransacao(moedaOrigem, valorOrigem, moedaDestino, valorDestino)
                            _estadoConversao.value = EstadoConversao.Sucesso(moedaDestino, valorDestino)

                        } else {
                            _estadoConversao.value = EstadoConversao.Erro(R.string.erro_api) // Erro na taxa
                        }
                    } else {
                        // A API não retornou o par de moedas esperado
                        _estadoConversao.value = EstadoConversao.Erro(R.string.erro_api_par_nao_encontrado)
                    }
                } else {
                    _estadoConversao.value = EstadoConversao.Erro(R.string.erro_api) // Erro de HTTP
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _estadoConversao.value = EstadoConversao.Erro(R.string.erro_api) // Erro de rede/exceção
            }
        }
    }

    /**
     * Reseta o estado da conversão para "Ocioso", para limpar a UI.
     */
    fun resetarEstado() {
        _estadoConversao.value = EstadoConversao.Ocioso
    }


    // --- Lógica de Saldo (Companion Object para Singleton) ---
    // Usamos um companion object para que os saldos sejam compartilhados (estáticos)
    // entre a MainActivity e a ConverterActivity.
    companion object {

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
                Moeda.BRL -> String.format(Locale.getDefault(), "R$ %.2f", valor)
                Moeda.USD -> String.format(Locale.getDefault(), "$ %.2f", valor)
                Moeda.BTC -> String.format(Locale.getDefault(), "BTC %.4f", valor)
            }
        }
    }
}