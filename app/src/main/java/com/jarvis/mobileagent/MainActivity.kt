package com.jarvis.mobileagent

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
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
    private lateinit var promptInput: EditText
    private lateinit var settingsPanel: LinearLayout
    private lateinit var localInput: EditText
    private val secretStore by lazy { SecretStore(this) }
    private val hermesClient by lazy { HermesClient() }

    private val bg = Color.rgb(10, 10, 12)
    private val panel = Color.rgb(25, 25, 29)
    private val panel2 = Color.rgb(34, 34, 39)
    private val orange = Color.rgb(255, 91, 20)
    private val muted = Color.rgb(157, 157, 166)
    private val white = Color.rgb(246, 246, 248)

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
            setBackgroundColor(bg)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(14))
        }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        brand.addView(label("J.A.R.V.I.S", 25f, white, true))
        brand.addView(label("MOBILE AI AGENT  •  v0.4", 10f, muted, true).apply { setPadding(1, dp(3), 0, 0) })
        val gear = actionButton("⚙", panel2, white) { toggleSettings() }
        header.addView(brand)
        header.addView(gear, LinearLayout.LayoutParams(dp(46), dp(42)))
        root.addView(header)

        status = label("HERMES • MEMERIKSA STATUS", 11f, muted, true).apply {
            setPadding(dp(22), dp(8), dp(22), dp(10))
        }
        root.addView(status)

        val scroll = ScrollView(this).apply { isFillViewport = false }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(10), dp(20), dp(22))
        }

        body.addView(label("Asisten pribadi kamu", 13f, muted))
        body.addView(label("Apa yang bisa saya\nbantu hari ini?", 29f, white, true).apply {
            setPadding(0, dp(8), 0, dp(8))
        })
        body.addView(label("Perintah, ide, dan pekerjaan — satu tempat.", 13f, muted).apply {
            setPadding(0, 0, 0, dp(18))
        })

        val chips = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        listOf("Semua", "Chat AI", "Aplikasi", "Perangkat").forEachIndexed { index, value ->
            val chip = label(value, 11f, if (index == 0) bg else white, true).apply {
                gravity = Gravity.CENTER
                setPadding(dp(13), dp(8), dp(13), dp(8))
                background = rounded(if (index == 0) orange else panel2, 30)
            }
            val lp = LinearLayout.LayoutParams(-2, -2)
            lp.marginEnd = dp(7)
            chips.addView(chip, lp)
        }
        body.addView(chips)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(17), dp(18), dp(18))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.rgb(255, 112, 35), Color.rgb(205, 55, 8))).apply { cornerRadius = dp(20).toFloat() }
        }
        hero.addView(label("✦  JARVIS AI", 11f, Color.WHITE, true))
        hero.addView(label("Bicara dengan JARVIS", 21f, Color.WHITE, true).apply { setPadding(0, dp(9), 0, dp(3)) })
        hero.addView(label("Tanyakan sesuatu atau minta bantuan menyelesaikan tugas.", 12f, Color.WHITE))
        val startChat = actionButton("MULAI PERCAKAPAN  →", Color.WHITE, orange) {
            promptInput.requestFocus()
            promptInput.post { (getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager).showSoftInput(promptInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT) }
        }
        hero.addView(startChat, LinearLayout.LayoutParams(-1, dp(46)).apply { topMargin = dp(16) })
        body.addView(hero, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(20) })

        body.addView(section("Akses cepat").apply { setPadding(0, dp(24), 0, dp(10)) })
        val quick = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        quick.addView(quickCard("▣", "WhatsApp", "Buka aplikasi") { handleCommand("buka WhatsApp") }, LinearLayout.LayoutParams(0, dp(112), 1f).apply { marginEnd = dp(7) })
        quick.addView(quickCard("◉", "Chrome", "Buka browser") { handleCommand("buka Chrome") }, LinearLayout.LayoutParams(0, dp(112), 1f).apply { marginStart = dp(7) })
        body.addView(quick)

        body.addView(section("Percakapan").apply { setPadding(0, dp(24), 0, dp(10)) })
        val chatCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = rounded(panel, 18)
        }
        chatCard.addView(label("JARVIS siap menerima instruksi.", 13f, muted).apply { setPadding(0, 0, 0, dp(10)) })
        promptInput = EditText(this).apply {
            hint = "Tulis pesan untuk Hermes..."
            setHintTextColor(muted)
            setTextColor(white)
            textSize = 14f
            minLines = 2
            maxLines = 4
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(panel2, 12)
        }
        chatCard.addView(promptInput, LinearLayout.LayoutParams(-1, -2))
        chatCard.addView(actionButton("KIRIM KE JARVIS  ↑", orange, Color.WHITE) {
            val message = promptInput.text.toString().trim()
            if (message.isBlank()) output.text = "Tulis pesan terlebih dahulu."
            else { promptInput.setText(""); sendToHermes(message) }
        }, LinearLayout.LayoutParams(-1, dp(46)).apply { topMargin = dp(10) })
        body.addView(chatCard)

        body.addView(section("Hasil dan aktivitas").apply { setPadding(0, dp(22), 0, dp(9)) })
        output = label("JARVIS: Siap. Hubungkan Hermes untuk memulai.", 13f, white).apply {
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = rounded(panel, 16)
        }
        body.addView(output, LinearLayout.LayoutParams(-1, -2))

        body.addView(section("Perintah perangkat").apply { setPadding(0, dp(22), 0, dp(9)) })
        val localCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(14)); background = rounded(panel, 16) }
        localInput = EditText(this).apply {
            hint = "Contoh: buka WhatsApp"
            setHintTextColor(muted)
            setTextColor(white)
            textSize = 14f
            setSingleLine(true)
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = rounded(panel2, 10)
        }
        localCard.addView(localInput, LinearLayout.LayoutParams(-1, dp(48)))
        localCard.addView(actionButton("JALANKAN PERINTAH", panel2, white) { handleCommand(localInput.text.toString()) },
            LinearLayout.LayoutParams(-1, dp(44)).apply { topMargin = dp(9) })
        localCard.addView(actionButton("PENGATURAN ACCESSIBILITY", panel2, white) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }, LinearLayout.LayoutParams(-1, dp(44)).apply { topMargin = dp(8) })
        body.addView(localCard)

        settingsPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = rounded(panel, 16)
        }
        settingsPanel.addView(label("Koneksi Hermes", 17f, white, true))
        apiKeyInput = EditText(this).apply {
            hint = "Tempel API key Hermes"
            setHintTextColor(muted)
            setTextColor(white)
            textSize = 13f
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setSingleLine(true)
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = rounded(panel2, 10)
        }
        settingsPanel.addView(apiKeyInput, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(10) })
        settingsPanel.addView(actionButton("SIMPAN API KEY DENGAN AMAN", panel2, white) {
            val key = apiKeyInput.text.toString().trim()
            if (key.length < 16) output.text = "API key belum valid. Jangan kirim key ke chat."
            else {
                secretStore.saveApiKey(key)
                apiKeyInput.setText("")
                output.text = "API key tersimpan terenkripsi. Fingerprint: " + keyFingerprint(key)
            }
        }, LinearLayout.LayoutParams(-1, dp(44)).apply { topMargin = dp(8) })
        settingsPanel.addView(actionButton("LIHAT FINGERPRINT API KEY", panel2, white) {
            val key = secretStore.readApiKey()
            output.text = if (key.isNullOrBlank()) "Belum ada API key tersimpan." else "Fingerprint: " + keyFingerprint(key)
        }, LinearLayout.LayoutParams(-1, dp(44)).apply { topMargin = dp(8) })
        settingsPanel.addView(actionButton("TES KONEKSI HERMES", orange, Color.WHITE) { testHermesConnection() },
            LinearLayout.LayoutParams(-1, dp(44)).apply { topMargin = dp(8) })
        body.addView(settingsPanel, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(16) })

        body.addView(label("JARVIS • AI AGENT ANDROID", 10f, muted, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, dp(8))
        })

        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        refreshStatus()
    }

    private fun toggleSettings() {
        settingsPanel.visibility = if (settingsPanel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        if (settingsPanel.visibility == View.VISIBLE) settingsPanel.post { settingsPanel.requestFocus() }
    }

    private fun quickCard(icon: String, title: String, subtitle: String, action: () -> Unit): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = rounded(panel, 16)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        card.addView(label(icon, 22f, orange, true))
        card.addView(label(title, 15f, white, true).apply { setPadding(0, dp(7), 0, dp(3)) })
        card.addView(label(subtitle, 11f, muted))
        return card
    }

    private fun section(value: String) = label(value, 17f, white, true)

    private fun label(value: String, size: Float, color: Int, bold: Boolean = false) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    private fun actionButton(textValue: String, color: Int, textColor: Int, action: () -> Unit) = Button(this).apply {
        text = textValue
        textSize = 11f
        setTextColor(textColor)
        isAllCaps = false
        typeface = Typeface.DEFAULT_BOLD
        background = rounded(color, 12)
        setPadding(dp(10), 0, dp(10), 0)
        setOnClickListener { action() }
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun keyFingerprint(key: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(key.toByteArray(Charsets.UTF_8))
        return digest.take(6).joinToString("") { "%02x".format(it) }
    }

    private fun testHermesConnection() {
        val key = currentApiKey() ?: return
        output.text = "Menghubungi Hermes..."
        Thread {
            try {
                val result = hermesClient.testConnection(key)
                runOnUiThread { output.text = result + "\nFingerprint: " + keyFingerprint(key) }
            } catch (error: Exception) {
                runOnUiThread { output.text = "Koneksi gagal: " + (error.message ?: "periksa Hermes") }
            }
        }.start()
    }

    private fun sendToHermes(message: String) {
        val key = currentApiKey() ?: return
        output.text = "JARVIS sedang berpikir..."
        Thread {
            try {
                val result = hermesClient.sendMessage(key, message)
                runOnUiThread { output.text = "HERMES:\n\n" + result }
            } catch (error: Exception) {
                runOnUiThread { output.text = "Pesan gagal dikirim: " + (error.message ?: "periksa koneksi Hermes") }
            }
        }.start()
    }

    private fun currentApiKey(): String? {
        val key = secretStore.readApiKey()
        if (key.isNullOrBlank()) {
            output.text = "Buka pengaturan ⚙ dan simpan API key Hermes terlebih dahulu."
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
            JarvisCommand.UNKNOWN -> output.text = "Perintah belum dikenali. Coba: buka Chrome, YouTube, WhatsApp, Maps, atau Gmail."
            else -> {
                val packageName = command.packageName ?: return
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent == null) output.text = command.label + " tidak ditemukan atau belum terpasang."
                else {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(launchIntent)
                    output.text = "Perintah diterima: membuka " + command.label + "."
                }
            }
        }
    }

    private fun refreshStatus() {
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
        val active = enabled.contains(packageName + "/" + JarvisAccessibilityService::class.java.name)
        status.text = if (active) "● ACCESSIBILITY AKTIF  •  HERMES SIAP DIKONFIGURASI" else "● ACCESSIBILITY BELUM AKTIF  •  HERMES LOKAL"
        status.setTextColor(if (active) orange else muted)
    }
}