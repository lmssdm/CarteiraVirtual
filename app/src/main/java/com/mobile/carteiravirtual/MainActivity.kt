package com.mobile.carteiravirtual

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Instancia o ViewModel
    private val walletViewModel: WalletViewModel by viewModels()

    private lateinit var tvSaldoReal: TextView
    private lateinit var tvSaldoDolar: TextView
    private lateinit var tvSaldoBitcoin: TextView
    private lateinit var btnConverter: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa as Views
        tvSaldoReal = findViewById(R.id.tvSaldoReal)
        tvSaldoDolar = findViewById(R.id.tvSaldoDolar)
        tvSaldoBitcoin = findViewById(R.id.tvSaldoBitcoin)
        btnConverter = findViewById(R.id.btnConverter)

        // Configura os Observadores para atualizar a UI quando os saldos mudarem
        setupObservers()

        // Configura o clique do botão
        btnConverter.setOnClickListener {
            // Intenção de ir para a ConverterActivity
            val intent = Intent(this, ConverterActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        tvSaldoReal.text = walletViewModel.formatarValor(Moeda.BRL, walletViewModel.saldoBRL.value ?: 0.0)
        tvSaldoDolar.text = walletViewModel.formatarValor(Moeda.USD, walletViewModel.saldoUSD.value ?: 0.0)
        tvSaldoBitcoin.text = walletViewModel.formatarValor(Moeda.BTC, walletViewModel.saldoBTC.value ?: 0.0)
    }
    // --- FIM DA CORREÇÃO ---


    private fun setupObservers() {
        // O Observer garante atualizações *enquanto a tela estiver ativa*
        walletViewModel.saldoBRL.observe(this) { saldo ->
            tvSaldoReal.text = walletViewModel.formatarValor(Moeda.BRL, saldo)
        }

        walletViewModel.saldoUSD.observe(this) { saldo ->
            tvSaldoDolar.text = walletViewModel.formatarValor(Moeda.USD, saldo)
        }

        walletViewModel.saldoBTC.observe(this) { saldo ->
            tvSaldoBitcoin.text = walletViewModel.formatarValor(Moeda.BTC, saldo)
        }
    }
}