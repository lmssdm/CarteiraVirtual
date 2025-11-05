package com.mobile.carteiravirtual

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mobile.carteiravirtual.api.RetrofitClient
import com.mobile.carteiravirtual.databinding.FragmentConverterBinding
import kotlinx.coroutines.launch
import java.lang.Exception

class ConverterFragment : Fragment(R.layout.fragment_converter) {

    // Pega o MESMO ViewModel compartilhado
    private val walletViewModel: WalletViewModel by activityViewModels()
    private val apiService = RetrofitClient.instance

    // Configura o ViewBinding
    private var _binding: FragmentConverterBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentConverterBinding.bind(view)

        binding.btnVoltar.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnConfirmarConversao.setOnClickListener {
            iniciarConversao()
        }
    }

    // --- FUNÇÃO FALTANTE ---
    private fun iniciarConversao() {
        binding.tvErro.visibility = View.GONE
        binding.tvResultadoConversao.visibility = View.GONE

        val strOrigem = binding.spinnerMoedaOrigem.selectedItem.toString()
        val strDestino = binding.spinnerMoedaDestino.selectedItem.toString()
        val valorOrigemStr = binding.etValorConverter.text.toString()

        val valorLimpo = valorOrigemStr.replace(',', '.')

        if (valorLimpo.isBlank() || valorLimpo.toDoubleOrNull() ?: 0.0 <= 0.0) {
            mostrarErro(getString(R.string.erro_valor_invalido))
            return
        }

        if (strOrigem == strDestino) {
            mostrarErro(getString(R.string.erro_mesma_moeda))
            return
        }

        val valorOrigem = valorLimpo.toDouble() // Usa a string limpa
        val moedaOrigem = Moeda.valueOf(strOrigem)
        val moedaDestino = Moeda.valueOf(strDestino)

        if (!walletViewModel.temSaldoSuficiente(moedaOrigem, valorOrigem)) {
            mostrarErro(getString(R.string.erro_saldo_insuficiente))
            return
        }

        buscarCotacao(moedaOrigem, moedaDestino, valorOrigem)
    }

    private fun buscarCotacao(moedaOrigem: Moeda, moedaDestino: Moeda, valorOrigem: Double) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnConfirmarConversao.isEnabled = false

        // Lógica do Bitcoin (igual à sua)
        var parMoedaApi = "${moedaOrigem}-${moedaDestino}"
        var chaveRespostaApi = "${moedaOrigem}${moedaDestino}"
        var inverterTaxa = false

        if (moedaDestino == Moeda.BTC) {
            parMoedaApi = "${moedaDestino}-${moedaOrigem}"
            chaveRespostaApi = "${moedaDestino}${moedaOrigem}"
            inverterTaxa = true
        } else if (moedaOrigem == Moeda.BTC && moedaDestino != Moeda.BTC) {
            parMoedaApi = "${moedaOrigem}-${moedaDestino}"
            chaveRespostaApi = "${moedaOrigem}${moedaDestino}"
            inverterTaxa = false
        }

        lifecycleScope.launch {
            try {
                val response = apiService.getCotacao(parMoedaApi)
                if (response.isSuccessful && response.body() != null) {
                    val cotacaoItem = response.body()!![chaveRespostaApi]
                    if (cotacaoItem != null) {
                        val taxaOriginal = cotacaoItem.bid.toDoubleOrNull()
                        if (taxaOriginal != null && taxaOriginal > 0) {
                            val taxa = if (inverterTaxa) (1 / taxaOriginal) else taxaOriginal
                            processarSucesso(moedaOrigem, valorOrigem, moedaDestino, taxa)
                        } else {
                            mostrarErro(getString(R.string.erro_api) + " (Taxa inválida)")
                        }
                    } else {
                        mostrarErro(getString(R.string.erro_api) + " (Par ${parMoedaApi} não encontrado)")
                    }
                } else {
                    mostrarErro(getString(R.string.erro_api) + " (Resposta: ${response.code()})")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mostrarErro(getString(R.string.erro_api))
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnConfirmarConversao.isEnabled = true
            }
        }
    }

    private fun processarSucesso(moedaOrigem: Moeda, valorOrigem: Double, moedaDestino: Moeda, taxa: Double) {
        val valorDestino = valorOrigem * taxa

        // Atualiza o ViewModel
        walletViewModel.realizarTransacao(moedaOrigem, valorOrigem, moedaDestino, valorDestino)

        val resultadoFormatado = walletViewModel.formatarValor(moedaDestino, valorDestino)
        binding.tvResultadoConversao.text = "${getString(R.string.valor_convertido)} $resultadoFormatado"
        binding.tvResultadoConversao.visibility = View.VISIBLE

        Toast.makeText(requireContext(), getString(R.string.conversao_sucesso), Toast.LENGTH_LONG).show()

        findNavController().popBackStack()
    }

    private fun mostrarErro(mensagem: String) {
        binding.tvErro.text = mensagem
        binding.tvErro.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}