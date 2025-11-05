package com.mobile.carteiravirtual

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton // 1. IMPORTANTE: Importar o ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.OnBackPressedCallback // 2. IMPORTANTE: Importar o Callback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mobile.carteiravirtual.WalletViewModel.Companion.formatarValor

class ConverterActivity : AppCompatActivity() {

    private val walletViewModel: WalletViewModel by viewModels()

    private lateinit var spinnerOrigem: Spinner
    private lateinit var spinnerDestino: Spinner
    private lateinit var etValor: EditText
    private lateinit var btnConverter: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvResultado: TextView
    private lateinit var tvErro: TextView
    private lateinit var btnVoltar: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_converter)

        spinnerOrigem = findViewById(R.id.spinnerMoedaOrigem)
        spinnerDestino = findViewById(R.id.spinnerMoedaDestino)
        etValor = findViewById(R.id.etValorConverter)
        btnConverter = findViewById(R.id.btnConfirmarConversao)
        progressBar = findViewById(R.id.progressBar)
        tvResultado = findViewById(R.id.tvResultadoConversao)
        tvErro = findViewById(R.id.tvErro)
        btnVoltar = findViewById(R.id.btnVoltar) // 4. Inicialize o btnVoltar

        setupObserver()

        btnConverter.setOnClickListener {
            iniciarConversao()
        }

        btnVoltar.setOnClickListener {
            walletViewModel.resetarEstado()
            onBackPressedDispatcher.onBackPressed()
        }

        val callback = object : OnBackPressedCallback(true /* habilitado */) {
            override fun handleOnBackPressed() {
                walletViewModel.resetarEstado()
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun setupObserver() {
        walletViewModel.estadoConversao.observe(this) { estado ->
            when (estado) {
                is EstadoConversao.Carregando -> {
                    progressBar.visibility = View.VISIBLE
                    btnConverter.isEnabled = false
                    tvResultado.visibility = View.GONE
                    tvErro.visibility = View.GONE
                }
                is EstadoConversao.Sucesso -> {
                    progressBar.visibility = View.GONE
                    btnConverter.isEnabled = true
                    tvErro.visibility = View.GONE
                    val resultadoFormatado = formatarValor(estado.moedaDestino, estado.valorDestino)
                    tvResultado.text = "${getString(R.string.valor_convertido)} $resultadoFormatado"
                    tvResultado.visibility = View.VISIBLE
                }
                is EstadoConversao.Erro -> {
                    progressBar.visibility = View.GONE
                    btnConverter.isEnabled = true
                    tvResultado.visibility = View.GONE
                    tvErro.text = getString(estado.mensagem)
                    tvErro.visibility = View.VISIBLE
                }
                is EstadoConversao.Ocioso -> {
                    progressBar.visibility = View.GONE
                    btnConverter.isEnabled = true
                    tvResultado.visibility = View.GONE
                    tvErro.visibility = View.GONE
                }
            }
        }
    }

    private fun iniciarConversao() {
        val strOrigem = spinnerOrigem.selectedItem.toString()
        val strDestino = spinnerDestino.selectedItem.toString()

        // CORREÇÃO DO BUG DA VÍRGULA:
        // Substitui vírgula por ponto antes de validar
        val valorOrigemStr = etValor.text.toString().replace(',', '.')

        try {
            val moedaOrigem = Moeda.valueOf(strOrigem)
            val moedaDestino = Moeda.valueOf(strDestino)
            walletViewModel.iniciarConversao(moedaOrigem, moedaDestino, valorOrigemStr)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            tvErro.text = getString(R.string.erro_api_par_nao_encontrado)
            tvErro.visibility = View.VISIBLE
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        walletViewModel.resetarEstado()
        onBackPressedDispatcher.onBackPressed()
        return true
    }


}