package com.mobile.carteiravirtual

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mobile.carteiravirtual.WalletViewModel.Companion.formatarValor

class ConverterActivity : AppCompatActivity() {

    // ViewModel refatorado
    private val walletViewModel: WalletViewModel by viewModels()

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

        // Configura o observador para o estado da conversão
        setupObserver()

        btnConverter.setOnClickListener {
            iniciarConversao()
        }
    }

    private fun setupObserver() {
        walletViewModel.estadoConversao.observe(this) { estado ->
            // O 'when' deve ser exaustivo
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

                    // Atualiza o texto de resultado
                    val resultadoFormatado = formatarValor(estado.moedaDestino, estado.valorDestino)
                    tvResultado.text = "${getString(R.string.valor_convertido)} $resultadoFormatado"
                    tvResultado.visibility = View.VISIBLE
                }
                is EstadoConversao.Erro -> {
                    progressBar.visibility = View.GONE
                    btnConverter.isEnabled = true
                    tvResultado.visibility = View.GONE

                    // Mostra a mensagem de erro vinda do R.string
                    tvErro.text = getString(estado.mensagem)
                    tvErro.visibility = View.VISIBLE
                }
                is EstadoConversao.Ocioso -> {
                    // Estado inicial ou após resetar
                    progressBar.visibility = View.GONE
                    btnConverter.isEnabled = true
                    tvResultado.visibility = View.GONE
                    tvErro.visibility = View.GONE
                }
            }
        }
    }

    private fun iniciarConversao() {
        // 1. Obter valores da UI
        val strOrigem = spinnerOrigem.selectedItem.toString()
        val strDestino = spinnerDestino.selectedItem.toString()
        val valorOrigemStr = etValor.text.toString()

        // 2. Tentar converter para Enum
        // Usamos try/catch caso o valor do spinner não seja um Enum válido
        try {
            val moedaOrigem = Moeda.valueOf(strOrigem)
            val moedaDestino = Moeda.valueOf(strDestino)

            // 3. Chamar o ViewModel
            walletViewModel.iniciarConversao(moedaOrigem, moedaDestino, valorOrigemStr)

        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            // Tratar erro se os nomes dos spinners não baterem com o Enum Moeda
            tvErro.text = getString(R.string.erro_api_par_nao_encontrado)
            tvErro.visibility = View.VISIBLE
        }
    }

    // Para o botão "voltar" na ActionBar
    override fun onSupportNavigateUp(): Boolean {
        // Reseta o estado no ViewModel antes de voltar
        walletViewModel.resetarEstado()
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onBackPressed() {
        // Garante que o estado seja resetado
        walletViewModel.resetarEstado()
        super.onBackPressed()
    }
}