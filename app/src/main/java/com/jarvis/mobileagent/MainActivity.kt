package com.jarvis.mobileagent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.security.MessageDigest

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var output: TextView
    private lateinit var apiKeyInput: EditText
    private val secretStore by lazy { SecretStore(this) }
    private val hermesClient by lazy { HermesClient() }

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
            setPadding(28, 36, 28, 28)
            setBackgroundColor(0xFFF4F7FB.toInt())
        }
        root.addView(TextView(this).apply {
            text = "J.A.R.V.I.S"
            textSize = 30f
            setTextColor(0xFF14243A.toInt())
            gravity = Gravity.CENTER
        }, matchWrap())
        root.addView(TextView(this).apply {
            text = "Mobile AI Agent • Tahap 0.3 Hermes"
            textSize = 14f
            setTextColor(0xFF52657A.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 6, 0, 22)
        }, matchWrap())

        status = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFF24364B.toInt())
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0xFFE4ECF5.toInt())
        }
        root.addView(status, matchWrap())

        root.addView(sectionTitle("Koneksi Hermes"), matchWrap().apply { topMargin = 22 })
        apiKeyInput = EditText(this).apply {
            hint = "Tempel API key Hermes di sini"
            textSize = 14f
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setSingleLine(true)
            setText(secretStore.readApiKey().orEmpty())
            setPadding(14, 12, 14, 12)
        }
        root.addView(apiKeyInput, matchWrap())
        root.addView(Button(this).apply {
            text = "Simpan API key dengan aman"
            setOnClickListener {
                val key = apiKeyInput.text.toString().trim()
                if (key.length < 16) {
                    output.text = "API key belum valid. Periksa key di UserLAnd, jangan kirim key ke chat."
                } else {
                    secretStore.saveApiKey(key)
                    apiKeyInput.setText("")
                    output.text = "API key tersimpan terenkripsi. Fingerprint tersimpan: " + keyFingerprint(key)
                }
            }
        }, matchWrap())
        root.addView(Button(this).apply {
            text = "Lihat fingerprint API key tersimpan"
            setOnClickListener {
                val key = secretStore.readApiKey()
                output.text = if (key.isNullOrBlank()) "Belum ada API key tersimpan."
                else "Fingerprint API key tersimpan: " + keyFingerprint(key)
            }
        }, matchWrap())
        root.addView(Button(this).apply {
            text = "Tes koneksi Hermes"
            setOnClickListener { testHermesConnection() }
        }, matchWrap())

        root.addView(sectionTitle("Chat dengan JARVIS"), matchWrap().apply { topMargin = 22 })
        val prompt = EditText(this).apply {
            hint = "Tulis pesan untuk Hermes..."
            textSize = 16f
            minLines = 2
            maxLines = 4
            setPadding(14, 12, 14, 12)
        }
        root.addView(prompt, matchWrap())
        root.addView(Button(this).apply {
            text = "Kirim ke Hermes"
            setOnClickListener {
                val message = prompt.text.toString().trim()
                if (message.isBlank()) {
                    output.text = "Tulis pesan terlebih dahulu."
                } else {
                    prompt.setText("")
                    sendToHermes(message)
                }
            }
        }, matchWrap())

        root.addView(sectionTitle("Perintah perangkat"), matchWrap().apply { topMargin = 22 })
        val localInput = EditText(this).apply {
            hint = "Contoh: buka Chrome"
            textSize = 16f
            setSingleLine(true)
            setPadding(14, 12, 14, 12)
        }
        root.addView(localInput, matchWrap())
        root.addView(Button(this).apply {
            text = "Jalankan perintah lokal"
            setOnClickListener { handleCommand(localInput.text.toString()) }
        }, matchWrap())
        root.addView(Button(this).apply {
            text = "Pengaturan Accessibility"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }, matchWrap())

        output = TextView(this).apply {
            text = "Status: siap. Simpan API key Hermes untuk mulai chat."
            textSize = 14f
            setTextColor(0xFF24364B.toInt())
            setPadding(4, 20, 4, 0)
        }
        root.addView(output, matchWrap())
        root.addView(TextView(this).apply {
            text = "Koneksi memakai 127.0.0.1:8642. Fingerprint hanya 12 karakter SHA-256 dan tidak mengungkap API key. Pastikan Hermes API Server aktif di UserLAnd."
            textSize = 12f
            setTextColor(0xFF64748B.toInt())
            setPadding(4, 24, 4, 0)
        }, matchWrap())

        val scroll = ScrollView(this).apply { addView(root) }
        setContentView(scroll)
        refreshStatus()
    }

    private fun keyFingerprint(key: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(key.toByteArray(Charsets.UTF_8))
        return digest.take(6).joinToString("") { "%02x".format(it) }
    }

    private fun testHermesConnection() {
        val key = currentApiKey() ?: return
        output.text = "Menguji Hermes. Fingerprint key Android: " + keyFingerprint(key)
        Thread {
            try {
                val result = hermesClient.testConnection(key)
                runOnUiThread { output.text = result + "\nFingerprint key Android: " + keyFingerprint(key) }
            } catch (error: Exception) {
                runOnUiThread {
                    output.text = "Koneksi gagal: " + (error.message ?: "periksa Hermes di UserLAnd") +
                        "\nFingerprint key Android: " + keyFingerprint(key)
                }
            }
        }.start()
    }

    private fun sendToHermes(message: String) {
        val key = currentApiKey() ?: return
        output.text = "JARVIS sedang menghubungi Hermes..."
        Thread {
            try {
                val result = hermesClient.sendMessage(key, message)
                runOnUiThread { output.text = "HERMES:\n\n" + result }
            } catch (error: Exception) {
                runOnUiThread {
                    output.text = "Pesan gagal dikirim: " + (error.message ?: "periksa koneksi Hermes")
                }
            }
        }.start()
    }

    private fun currentApiKey(): String? {
        val key = secretStore.readApiKey()
        if (key.isNullOrBlank()) {
            output.text = "Simpan API key Hermes terlebih dahulu."
            return null
        }
        return key
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
            "Accessibility: AKTIF (service fondasi) • Hermes: konfigurasi lokal"
        } else {
            "Accessibility: BELUM AKTIF • Hermes: konfigurasi lokal"
        }
    }

    private fun sectionTitle(value: String) = TextView(this).apply {
        text = value
        textSize = 18f
        setTextColor(0xFF14243A.toInt())
        setPadding(0, 4, 0, 8)
    }

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
