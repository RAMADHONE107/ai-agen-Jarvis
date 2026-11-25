package com.jarvis.mobileagent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var output: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) refreshStatus()
    }

    private fun render() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP
            setPadding(28, 36, 28, 24)
            setBackgroundColor(0xFFF4F7FB.toInt())
        }
        root.addView(TextView(this).apply {
            text = "J.A.R.V.I.S"
            textSize = 30f
            setTextColor(0xFF14243A.toInt())
            gravity = Gravity.CENTER
        }, matchWrap())
        root.addView(TextView(this).apply {
            text = "Mobile AI Agent • Tahap 0.2"
            textSize = 14f
            setTextColor(0xFF52657A.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 6, 0, 26)
        }, matchWrap())

        status = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFF24364B.toInt())
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0xFFE4ECF5.toInt())
        }
        root.addView(status, matchWrap())

        val input = EditText(this).apply {
            hint = "Contoh: buka Chrome"
            textSize = 16f
            setSingleLine(true)
            setPadding(14, 12, 14, 12)
        }
        root.addView(input, matchWrap().apply { topMargin = 22 })

        root.addView(Button(this).apply {
            text = "Jalankan perintah"
            setOnClickListener { handleCommand(input.text.toString()) }
        }, matchWrap())

        root.addView(Button(this).apply {
            text = "Pengaturan Accessibility"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }, matchWrap())

        output = TextView(this).apply {
            text = "Status: siap menerima perintah lokal."
            textSize = 14f
            setTextColor(0xFF24364B.toInt())
            setPadding(4, 20, 4, 0)
        }
        root.addView(output, matchWrap())

        root.addView(TextView(this).apply {
            text = "Mode aman: versi ini hanya membuka aplikasi yang ada dalam daftar izin. JARVIS belum membaca layar, mengetuk, mengetik, mengirim pesan, atau melakukan transaksi."
            textSize = 12f
            setTextColor(0xFF64748B.toInt())
            setPadding(4, 26, 4, 0)
        }, matchWrap())

        setContentView(root)
        refreshStatus()
    }

    private fun handleCommand(raw: String) {
        when (val command = CommandParser.parse(raw)) {
            JarvisCommand.OPEN_ACCESSIBILITY_SETTINGS -> {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                output.text = "Membuka Pengaturan Accessibility Android."
            }
            JarvisCommand.UNKNOWN -> {
                output.text = "Perintah belum dikenali. Coba: buka Chrome, YouTube, WhatsApp, Maps, atau Gmail."
            }
            else -> {
                val packageName = command.packageName ?: return
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent == null) {
                    output.text = command.label + " tidak ditemukan atau belum terpasang."
                } else {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(launchIntent)
                    output.text = "Perintah diterima: membuka " + command.label + "."
                }
            }
        }
    }

    private fun refreshStatus() {
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        val active = enabled.contains(packageName + "/" + JarvisAccessibilityService::class.java.name)
        status.text = if (active) {
            "Accessibility: AKTIF (service fondasi)"
        } else {
            "Accessibility: BELUM AKTIF"
        }
    }

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
