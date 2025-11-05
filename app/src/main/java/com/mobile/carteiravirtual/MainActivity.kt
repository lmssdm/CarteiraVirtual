package com.mobile.carteiravirtual

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mobile.carteiravirtual.WalletViewModel.Companion.formatarValor

class MainActivity : AppCompatActivity() {

    // Não precisamos mais do 'by viewModels()' aqui para os saldos,
    // pois vamos aceder aos dados estáticos (companion object) do WalletViewModel

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
        // Força a atualização dos valores toda vez que a tela principal
        // volta ao foco (ex: depois de fechar a ConverterActivity).
        // Isto é crucial porque os LiveData estão num companion object.
        atualizarValoresVisuais()
    }

    private fun setupObservers() {
        // Observa os saldos estáticos do WalletViewModel
        WalletViewModel.saldoBRL.observe(this) { saldo ->
            tvSaldoReal.text = formatarValor(Moeda.BRL, saldo)
        }

        WalletViewModel.saldoUSD.observe(this) { saldo ->
            tvSaldoDolar.text = formatarValor(Moeda.USD, saldo)
        }

        WalletViewModel.saldoBTC.observe(this) { saldo ->
            tvSaldoBitcoin.text = formatarValor(Moeda.BTC, saldo)
        }
    }

    private fun atualizarValoresVisuais() {
        // Pega os valores atuais do LiveData
        WalletViewModel.saldoBRL.value?.let {
            tvSaldoReal.text = formatarValor(Moeda.BRL, it)
        }
        WalletViewModel.saldoUSD.value?.let {
            tvSaldoDolar.text = formatarValor(Moeda.USD, it)
        }
        WalletViewModel.saldoBTC.value?.let {
            tvSaldoBitcoin.text = formatarValor(Moeda.BTC, it)
        }
    }
}