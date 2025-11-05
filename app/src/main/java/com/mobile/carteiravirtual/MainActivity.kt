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

        tvSaldoReal = findViewById(R.id.tvSaldoReal)
        tvSaldoDolar = findViewById(R.id.tvSaldoDolar)
        tvSaldoBitcoin = findViewById(R.id.tvSaldoBitcoin)
        btnConverter = findViewById(R.id.btnConverter)

        setupObservers()

        btnConverter.setOnClickListener {
            val intent = Intent(this, ConverterActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        atualizarValoresVisuais()
    }

    private fun setupObservers() {
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