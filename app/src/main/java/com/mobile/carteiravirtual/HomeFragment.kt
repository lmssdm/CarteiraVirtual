package com.mobile.carteiravirtual

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mobile.carteiravirtual.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    // Pega o ViewModel compartilhado da Activity
    private val walletViewModel: WalletViewModel by activityViewModels()

    // Configura o ViewBinding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // Configura os Observadores
        setupObservers()

        // Configura o clique do botão para navegar
        binding.btnConverter.setOnClickListener {
            // Usa o NavController para ir para a tela de conversão
            findNavController().navigate(R.id.action_homeFragment_to_converterFragment)
        }
    }

    private fun setupObservers() {
        // Usa viewLifecycleOwner para os observadores do Fragment
        walletViewModel.saldoBRL.observe(viewLifecycleOwner) { saldo ->
            binding.tvSaldoReal.text = walletViewModel.formatarValor(Moeda.BRL, saldo)
        }

        walletViewModel.saldoUSD.observe(viewLifecycleOwner) { saldo ->
            binding.tvSaldoDolar.text = walletViewModel.formatarValor(Moeda.USD, saldo)
        }

        walletViewModel.saldoBTC.observe(viewLifecycleOwner) { saldo ->
            binding.tvSaldoBitcoin.text = walletViewModel.formatarValor(Moeda.BTC, saldo)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}