package com.mobile.carteiravirtual

import android.os.Bundle
import android.view.View
import android.widget.Toast
// import androidx.appcompat.app.AppCompatActivity // <- PODE REMOVER ESTE IMPORT
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

    private fun processarSucesso(moedaOrigem: Moeda, valorOrigem: Double, moedaDestino: Moeda, taxa: Double) {
        val valorDestino = valorOrigem * taxa

        // Atualiza o ViewModel (agora sem o companion object)
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