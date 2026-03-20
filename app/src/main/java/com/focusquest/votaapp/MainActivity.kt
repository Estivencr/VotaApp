package com.focusquest.votaapp

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // ─  Definir las variables de las vistas ──────────────────────────────────────────────
    private lateinit var panelConfiguracion: LinearLayout
    private lateinit var panelVotacion: LinearLayout
    private lateinit var panelResultados: LinearLayout

    private lateinit var etNumeroElectores: EditText
    private lateinit var btnIniciarEleccion: Button

    private lateinit var tvTurnoElector: TextView
    private lateinit var etEdadElector: EditText
    private lateinit var tvMensajeInhabilitado: TextView
    private lateinit var panelCandidatos: LinearLayout
    private lateinit var rgCandidatos: RadioGroup
    private lateinit var btnValidarEdad: Button
    private lateinit var btnRegistrarVoto: Button

    private lateinit var tvResultados: TextView
    private lateinit var tvGanador: TextView
    private lateinit var btnNuevaEleccion: Button

    // ──---- Contadores: Estado de la elección ────────────────────────────────
    private var totalElectores = 0
    private var electorActual = 0
    private val votos = intArrayOf(0, 0, 0)   // votos[0]=Cand1, votos[1]=Cand2, votos[2]=Cand3
    private var electoresHabilitados = 0
    private var electoresInhabilitados = 0

    // Nombres de los candidatos
    private val candidatos = arrayOf("Estiven Cano", "Angi Ruiz", "Carlos López")

    // ── Ciclo de vida ────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inicializarVistas()
        configurarListeners()
    }

    // ── Inicialización absolutamente tood lo que se mostrara ───────────────────────────────────────
    private fun inicializarVistas() {
        panelConfiguracion    = findViewById(R.id.panelConfiguracion)
        panelVotacion         = findViewById(R.id.panelVotacion)
        panelResultados       = findViewById(R.id.panelResultados)

        etNumeroElectores     = findViewById(R.id.etNumeroElectores)
        btnIniciarEleccion    = findViewById(R.id.btnIniciarEleccion)

        tvTurnoElector        = findViewById(R.id.tvTurnoElector)
        etEdadElector         = findViewById(R.id.etEdadElector)
        tvMensajeInhabilitado = findViewById(R.id.tvMensajeInhabilitado)
        panelCandidatos       = findViewById(R.id.panelCandidatos)
        rgCandidatos          = findViewById(R.id.rgCandidatos)
        btnValidarEdad        = findViewById(R.id.btnValidarEdad)
        btnRegistrarVoto      = findViewById(R.id.btnRegistrarVoto)

        tvResultados          = findViewById(R.id.tvResultados)
        tvGanador             = findViewById(R.id.tvGanador)
        btnNuevaEleccion      = findViewById(R.id.btnNuevaEleccion)
    }

    private fun configurarListeners() {
        btnIniciarEleccion.setOnClickListener  { iniciarEleccion() }
        btnValidarEdad.setOnClickListener      { validarEdadElector() }
        btnRegistrarVoto.setOnClickListener    { registrarVoto() }
        btnNuevaEleccion.setOnClickListener    { reiniciarApp() }
    }

    // ── Lógica: iniciar elección ─────────────────────────────
    private fun iniciarEleccion() {
        val texto = etNumeroElectores.text.toString().trim()

        if (texto.isEmpty()) {
            mostrarToast("Por favor ingrese el número de electores")
            return
        }

        totalElectores = texto.toInt()

        if (totalElectores <= 0) {
            mostrarToast("El número de electores debe ser mayor a cero")
            return
        }

        // Resetear contadores
        votos[0] = 0; votos[1] = 0; votos[2] = 0
        electorActual = 1
        electoresHabilitados = 0
        electoresInhabilitados = 0

        // Transición de panel
        panelConfiguracion.visibility = View.GONE
        panelVotacion.visibility = View.VISIBLE
        cerrarTeclado()
        prepararTurnoElector()
    }

    // ── Lógica: preparar turno del elector actual ────────────
    private fun prepararTurnoElector() {
        tvTurnoElector.text = "Elector $electorActual de $totalElectores"
        etEdadElector.text.clear()
        etEdadElector.isEnabled = true
        tvMensajeInhabilitado.visibility = View.GONE
        panelCandidatos.visibility = View.GONE
        rgCandidatos.clearCheck()
        btnValidarEdad.visibility = View.VISIBLE
        btnRegistrarVoto.visibility = View.GONE
    }

    // ── Lógica: validar edad del elector ─────────────────────
    private fun validarEdadElector() {
        val textoEdad = etEdadElector.text.toString().trim()

        if (textoEdad.isEmpty()) {
            mostrarToast("Ingrese la edad del elector")
            return
        }

        val edad = textoEdad.toInt()

        if (edad <= 0 || edad > 120) {
            mostrarToast("Ingrese una edad válida (1–120 años)")
            return
        }

        etEdadElector.isEnabled = false
        cerrarTeclado()

        if (edad >= 18) {
            // Elector habilitado → mostrar candidatos
            electoresHabilitados++
            tvMensajeInhabilitado.visibility = View.GONE
            panelCandidatos.visibility = View.VISIBLE
            btnValidarEdad.visibility = View.GONE
            btnRegistrarVoto.visibility = View.VISIBLE
        } else {
            // Elector menor de edad → no puede votar
            electoresInhabilitados++
            tvMensajeInhabilitado.visibility = View.VISIBLE
            panelCandidatos.visibility = View.GONE
            btnValidarEdad.visibility = View.GONE

            // Avanzar automáticamente al siguiente elector
            btnRegistrarVoto.visibility = View.VISIBLE
            btnRegistrarVoto.text = "Continuar"
        }
    }

    // ── Lógica: registrar voto o continuar ───────────────────
    private fun registrarVoto() {
        val esMenorDeEdad = tvMensajeInhabilitado.visibility == View.VISIBLE

        if (!esMenorDeEdad) {
            // Validar que haya seleccionado un candidato
            val seleccion = rgCandidatos.checkedRadioButtonId
            if (seleccion == -1) {
                mostrarToast("Seleccione un candidato para votar")
                return
            }

            // Contabilizar el voto
            when (seleccion) {
                R.id.rbCandidato1 -> votos[0]++
                R.id.rbCandidato2 -> votos[1]++
                R.id.rbCandidato3 -> votos[2]++
            }
        }

        // Restaurar texto del botón por si quedó como "Continuar"
        btnRegistrarVoto.text = "Registrar voto"

        // ¿Hay más electores?
        if (electorActual < totalElectores) {
            electorActual++
            prepararTurnoElector()
        } else {
            mostrarResultados()
        }
    }

    // ── Lógica: calcular y mostrar resultados ────────────────
    private fun mostrarResultados() {
        panelVotacion.visibility = View.GONE
        panelResultados.visibility = View.VISIBLE

        val totalVotos = votos.sum()

        // Construir resumen de votación
        val resumen = StringBuilder()
        resumen.append("Total de electores registrados: $totalElectores\n")
        resumen.append("Electores habilitados (≥18): $electoresHabilitados\n")
        resumen.append("Electores inhabilitados (<18): $electoresInhabilitados\n")
        resumen.append("Total de votos emitidos: $totalVotos\n\n")

        for (i in candidatos.indices) {
            val porcentaje = if (totalVotos > 0) (votos[i] * 100.0 / totalVotos) else 0.0
            resumen.append("${candidatos[i]}: ${votos[i]} voto(s) (%.1f%%)\n".format(porcentaje))
        }

        tvResultados.text = resumen.toString()

        // Determinar ganador
        val mensajeGanador = determinarGanador(totalVotos)
        tvGanador.text = mensajeGanador
    }

    private fun determinarGanador(totalVotos: Int): String {
        if (totalVotos == 0) {
            return "⚠️ No se emitió ningún voto válido"
        }

        val maxVotos = votos.max()
        val ganadores = candidatos.filterIndexed { i, _ -> votos[i] == maxVotos }

        return if (ganadores.size == 1) {
            "🏆 Candidato ganador:\n${ganadores[0]}\ncon $maxVotos voto(s)"
        } else {
            "🤝 Empate entre:\n${ganadores.joinToString(" y ")}\ncon $maxVotos voto(s) cada uno"
        }
    }

    // ── Lógica: reiniciar la app ─────────────────────────────
    private fun reiniciarApp() {
        etNumeroElectores.text.clear()
        panelResultados.visibility = View.GONE
        panelVotacion.visibility = View.GONE
        panelConfiguracion.visibility = View.VISIBLE
    }

    // ── Utilidades ───────────────────────────────────────────
    private fun mostrarToast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun cerrarTeclado() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }
}