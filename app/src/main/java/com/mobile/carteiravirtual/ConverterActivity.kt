package com.mobile.carteiravirtual

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mobile.carteiravirtual.api.RetrofitClient
import kotlinx.coroutines.launch
import java.lang.Exception

class ConverterActivity : AppCompatActivity() {

    // Compartilha o mesmo ViewModel da MainActivity
    private val walletViewModel: WalletViewModel by viewModels()
    private val apiService = RetrofitClient.instance

    private lateinit var spinnerOrigem: Spinner
    private lateinit var spinnerDestino: Spinner
    private lateinit var etValor: EditText
    private lateinit var btnConverter: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvResultado: TextView
    private lateinit var tvErro: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_converter)

        // Configura a ActionBar (opcional, para ter o botão "voltar")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.converter_recursos)

        spinnerOrigem = findViewById(R.id.spinnerMoedaOrigem)
        spinnerDestino = findViewById(R.id.spinnerMoedaDestino)
        etValor = findViewById(R.id.etValorConverter)
        btnConverter = findViewById(R.id.btnConfirmarConversao)
        progressBar = findViewById(R.id.progressBar)
        tvResultado = findViewById(R.id.tvResultadoConversao)
        tvErro = findViewById(R.id.tvErro)

        btnConverter.setOnClickListener {
            iniciarConversao()
        }
    }

    private fun iniciarConversao() {
        // 1. Limpar UI de feedback
        tvErro.visibility = View.GONE
        tvResultado.visibility = View.GONE

        // 2. Obter valores
        val strOrigem = spinnerOrigem.selectedItem.toString()
        val strDestino = spinnerDestino.selectedItem.toString()
        val valorOrigemStr = etValor.text.toString()

        // 3. Validar entradas
        if (valorOrigemStr.isBlank() || valorOrigemStr.toDoubleOrNull() ?: 0.0 <= 0.0) {
            mostrarErro(getString(R.string.erro_valor_invalido))
            return
        }

        if (strOrigem == strDestino) {
            mostrarErro(getString(R.string.erro_mesma_moeda))
            return
        }

        val valorOrigem = valorOrigemStr.toDouble()
        val moedaOrigem = Moeda.valueOf(strOrigem)
        val moedaDestino = Moeda.valueOf(strDestino)

        // 4. Verificar saldo
        if (!walletViewModel.temSaldoSuficiente(moedaOrigem, valorOrigem)) {
            mostrarErro(getString(R.string.erro_saldo_insuficiente))
            return
        }

        // 5. Chamar API (dentro de uma Coroutine)
        buscarCotacao(moedaOrigem, moedaDestino, valorOrigem)
    }

    private fun buscarCotacao(moedaOrigem: Moeda, moedaDestino: Moeda, valorOrigem: Double) {
        // Mostrar ProgressBar
        progressBar.visibility = View.VISIBLE
        btnConverter.isEnabled = false

        // Formato da API: "USD-BRL"
        val parMoedaApi = "${moedaOrigem}-${moedaDestino}"
        // Chave da resposta da API: "USDBRL"
        val chaveRespostaApi = "${moedaOrigem}${moedaDestino}"

        lifecycleScope.launch {
            try {
                val response = apiService.getCotacao(parMoedaApi)

                if (response.isSuccessful && response.body() != null) {
                    val cotacaoItem = response.body()!![chaveRespostaApi]

                    if (cotacaoItem != null) {
                        val taxa = cotacaoItem.bid.toDoubleOrNull()
                        if (taxa != null) {
                            processarSucesso(moedaOrigem, valorOrigem, moedaDestino, taxa)
                        } else {
                            mostrarErro(getString(R.string.erro_api))
                        }
                    } else {
                        mostrarErro(getString(R.string.erro_api) + " (Par ${parMoedaApi} não encontrado)")
                    }

                } else {
                    mostrarErro(getString(R.string.erro_api) + " (Resposta: ${response.code()})")
                }

            } catch (e: Exception) {
                // Trata erros de rede ou de parsing
                e.printStackTrace()
                mostrarErro(getString(R.string.erro_api))
            } finally {
                // Esconder ProgressBar
                progressBar.visibility = View.GONE
                btnConverter.isEnabled = true
            }
        }
    }

    private fun processarSucesso(moedaOrigem: Moeda, valorOrigem: Double, moedaDestino: Moeda, taxa: Double) {
        val valorDestino = valorOrigem * taxa

        // Atualiza o ViewModel
        walletViewModel.realizarTransacao(moedaOrigem, valorOrigem, moedaDestino, valorDestino)

        // Mostra o resultado na tela
        val resultadoFormatado = walletViewModel.formatarValor(moedaDestino, valorDestino)
        tvResultado.text = "${getString(R.string.valor_convertido)} $resultadoFormatado"
        tvResultado.visibility = View.VISIBLE

        // Mostra um Toast e fecha a activity
        Toast.makeText(this, getString(R.string.conversao_sucesso), Toast.LENGTH_LONG).show()

        // Fecha a ConverterActivity e volta para a MainActivity
        finish()
    }

    private fun mostrarErro(mensagem: String) {
        tvErro.text = mensagem
        tvErro.visibility = View.VISIBLE
    }

    // Para o botão "voltar" na ActionBar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
