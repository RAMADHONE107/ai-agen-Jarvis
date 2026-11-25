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
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.security.MessageDigest

class MainActivity : Activity() {
    private val secretStore by lazy { SecretStore(this) }
    private val hermesClient by lazy { HermesClient() }
    private val bg = Color.rgb(10, 10, 12)
    private val panel = Color.rgb(25, 25, 29)
    private val panel2 = Color.rgb(38, 38, 44)
    private val orange = Color.rgb(255, 91, 20)
    private val muted = Color.rgb(157, 157, 166)
    private val white = Color.rgb(246, 246, 248)

    private lateinit var content: FrameLayout
    private lateinit var status: TextView
    private lateinit var chatHistory: LinearLayout
    private lateinit var chatScroll: ScrollView
    private lateinit var chatInput: EditText
    private lateinit var keyInput: EditText
    private lateinit var localInput: EditText
    private var selectedTab = "home"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        buildShell()
        showTab("home")
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) refreshStatus()
    }

    private fun buildShell() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(15), dp(20), dp(10))
        }
        val heading = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        heading.addView(text("J.A.R.V.I.S", 24f, white, true))
        heading.addView(text("MOBILE AI AGENT  •  v0.4.1", 10f, muted, true))
        header.addView(heading)
        header.addView(button("⚙", panel2, white, 44) { showTab("settings") }, LinearLayout.LayoutParams(dp(48), dp(42)))
        root.addView(header)

        status = text("HERMES • MEMERIKSA STATUS", 11f, muted, true).apply {
            setPadding(dp(20), dp(8), dp(20), dp(10))
        }
        root.addView(status)

        content = FrameLayout(this)
        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(6), dp(7), dp(6), dp(8))
            setBackgroundColor(Color.rgb(18, 18, 21))
        }
        listOf(Triple("home", "⌂", "Beranda"), Triple("chat", "◉", "Chat"), Triple("apps", "▦", "Aplikasi"), Triple("settings", "⚙", "Setelan")).forEach { item ->
            nav.addView(navItem(item.second, item.third) { showTab(item.first) },
                LinearLayout.LayoutParams(0, dp(58), 1f))
        }
        root.addView(nav)
        setContentView(root)
        refreshStatus()
    }

    private fun showTab(tab: String) {
        selectedTab = tab
        content.removeAllViews()
        val screen = when (tab) {
            "chat" -> chatScreen()
            "apps" -> appsScreen()
            "settings" -> settingsScreen()
            else -> homeScreen()
        }
        content.addView(screen, FrameLayout.LayoutParams(-1, -1))
    }

    private fun homeScreen(): View {
        val scroll = ScrollView(this)
        val body = column().apply { setPadding(dp(20), dp(16), dp(20), dp(24)) }
        body.addView(text("ASISTEN PRIBADI", 11f, muted, true))
        body.addView(text("Apa yang bisa saya bantu hari ini?", 27f, white, true).apply { setPadding(0, dp(8), 0, dp(8)) })
        body.addView(text("Perintah, ide, dan pekerjaan — satu tempat.", 13f, muted).apply { setPadding(0, 0, 0, dp(18)) })

        val hero = column().apply {
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = gradient()
        }
        hero.addView(text("✦  JARVIS AI", 11f, Color.WHITE, true))
        hero.addView(text("Bicara dengan JARVIS", 21f, Color.WHITE, true).apply { setPadding(0, dp(8), 0, dp(4)) })
        hero.addView(text("Tanyakan sesuatu atau minta bantuan menyelesaikan tugas.", 12f, Color.WHITE))
        hero.addView(button("MULAI CHAT  →", Color.WHITE, orange, 46) { showTab("chat") },
            LinearLayout.LayoutParams(-1, dp(46)).apply { topMargin = dp(15) })
        body.addView(hero)

        body.addView(text("Akses cepat", 18f, white, true).apply { setPadding(0, dp(24), 0, dp(10)) })
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(quickCard("▣", "WhatsApp", "Buka aplikasi") { handleCommand("buka WhatsApp") },
            LinearLayout.LayoutParams(0, dp(112), 1f).apply { marginEnd = dp(6) })
        row.addView(quickCard("◎", "Chrome", "Buka browser") { handleCommand("buka Chrome") },
            LinearLayout.LayoutParams(0, dp(112), 1f).apply { marginStart = dp(6) })
        body.addView(row)
        body.addView(button("LIHAT SEMUA APLIKASI  →", panel2, white, 46) { showTab("apps") },
            LinearLayout.LayoutParams(-1, dp(46)).apply { topMargin = dp(10) })
        body.addView(text("Aktivitas terbaru", 18f, white, true).apply { setPadding(0, dp(24), 0, dp(10)) })
        body.addView(text("JARVIS siap menerima instruksi. Buka tab Chat untuk bertanya kepada Hermes.", 13f, muted).apply {
            setPadding(dp(14), dp(14), dp(14), dp(14)); background = rounded(panel, 14)
        })
        scroll.addView(body)
        return scroll
    }

    private fun chatScreen(): View {
        val root = column()
        val title = column().apply { setPadding(dp(18), dp(12), dp(18), dp(8)) }
        title.addView(text("Chat dengan JARVIS", 21f, white, true))
        title.addView(text("Pesan dan jawaban akan tampil di ruang percakapan.", 12f, muted))
        root.addView(title)
        chatScroll = ScrollView(this).apply { isFillViewport = true; clipToPadding = false }
        chatHistory = column().apply { setPadding(dp(16), dp(8), dp(16), dp(14)) }
        chatHistory.addView(bubble("JARVIS", "Saya siap membantu. Kirim pertanyaan atau instruksi.", false))
        chatScroll.addView(chatHistory)
        root.addView(chatScroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val composer = column().apply {
            setPadding(dp(12), dp(10), dp(12), dp(12))
            background = rounded(panel, 16)
        }
        chatInput = EditText(this).apply {
            hint = "Tulis pesan untuk Hermes..."
            setHintTextColor(muted); setTextColor(white); textSize = 14f
            minLines = 1; maxLines = 4
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(panel2, 12)
        }
        composer.addView(chatInput, LinearLayout.LayoutParams(-1, -2))
        composer.addView(button("KIRIM KE HERMES  ↑", orange, Color.WHITE, 46) { sendChat() },
            LinearLayout.LayoutParams(-1, dp(46)).apply { topMargin = dp(8) })
        root.addView(composer, LinearLayout.LayoutParams(-1, -2))
        return root
    }

    private fun sendChat() {
        val message = chatInput.text.toString().trim()
        if (message.isBlank()) {
            addBubble("JARVIS", "Tulis pesan terlebih dahulu.", false)
            return
        }
        chatInput.setText("")
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(chatInput.windowToken, 0)
        addBubble("KAMU", message, true)
        val pending = bubble("HERMES", "Sedang memproses...", false)
        chatHistory.addView(pending)
        scrollChatDown()
        val key = secretStore.readApiKey()
        if (key.isNullOrBlank()) {
            pending.findViewWithTag<TextView>("bubble_body")?.text = "API key belum tersimpan. Buka tab Setelan untuk menghubungkan Hermes."
            return
        }
        Thread {
            try {
                val answer = hermesClient.sendMessage(key, message)
                runOnUiThread {
                    pending.findViewWithTag<TextView>("bubble_body")?.text = answer
                    scrollChatDown()
                }
            } catch (error: Exception) {
                runOnUiThread {
                    pending.findViewWithTag<TextView>("bubble_body")?.text =
                        "Gagal menghubungi Hermes: " + (error.message ?: "kesalahan tidak diketahui")
                    scrollChatDown()
                }
            }
        }.start()
    }

    private fun addBubble(who: String, message: String, user: Boolean) {
        chatHistory.addView(bubble(who, message, user))
        scrollChatDown()
    }

    private fun scrollChatDown() {
        chatScroll.post { chatScroll.fullScroll(View.FOCUS_DOWN) }
    }

    private fun bubble(who: String, message: String, user: Boolean): View {
        val card = column().apply {
            setPadding(dp(13), dp(10), dp(13), dp(11))
            background = rounded(if (user) Color.rgb(67, 35, 23) else panel, 14)
        }
        card.addView(text(who, 10f, if (user) orange else muted, true))
        card.addView(text(message, 14f, white).apply {
            tag = "bubble_body"
            setPadding(0, dp(5), 0, 0)
            setTextIsSelectable(true)
        })
        val wrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = if (user) Gravity.END else Gravity.START
        }
        wrap.addView(card, LinearLayout.LayoutParams(-1, -2).apply {
            leftMargin = if (user) dp(34) else 0
            rightMargin = if (user) 0 else dp(34)
            bottomMargin = dp(9)
        })
        return wrap
    }

    private fun appsScreen(): View {
        val scroll = ScrollView(this)
        val body = column().apply { setPadding(dp(20), dp(16), dp(20), dp(24)) }
        body.addView(text("Aplikasi & perintah", 22f, white, true))
        body.addView(text("Pilih aplikasi untuk membukanya, atau masukkan perintah lokal.", 12f, muted).apply { setPadding(0, dp(5), 0, dp(16)) })
        listOf(
            Triple("WhatsApp", "com.whatsapp", "▣"),
            Triple("Chrome", "com.android.chrome", "◎"),
            Triple("YouTube", "com.google.android.youtube", "▶"),
            Triple("Google Maps", "com.google.android.apps.maps", "⌖"),
            Triple("Gmail", "com.google.android.gm", "✉")
        ).forEach { app ->
            body.addView(button(app.third + "   BUKA " + app.first, panel, white, 50) { launchPackage(app.second, app.first) },
                LinearLayout.LayoutParams(-1, dp(50)).apply { bottomMargin = dp(9) })
        }
        body.addView(text("Perintah manual", 17f, white, true).apply { setPadding(0, dp(16), 0, dp(8)) })
        localInput = EditText(this).apply {
            hint = "Contoh: buka WhatsApp"
            setHintTextColor(muted); setTextColor(white); textSize = 14f
            setSingleLine(true); setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(panel2, 12)
        }
        body.addView(localInput, LinearLayout.LayoutParams(-1, dp(48)))
        body.addView(button("JALANKAN PERINTAH", orange, Color.WHITE, 46) {
            handleCommand(localInput.text.toString())
        }, LinearLayout.LayoutParams(-1, dp(46)).apply { topMargin = dp(9) })
        scroll.addView(body)
        return scroll
    }

    private fun settingsScreen(): View {
        val scroll = ScrollView(this)
        val body = column().apply { setPadding(dp(20), dp(16), dp(20), dp(24)) }
        body.addView(text("Pengaturan", 22f, white, true))
        body.addView(text("Koneksi Hermes dan izin perangkat.", 12f, muted).apply { setPadding(0, dp(5), 0, dp(16)) })
        body.addView(text("API KEY HERMES", 11f, muted, true))
        keyInput = EditText(this).apply {
            hint = "Tempel API key Hermes di sini"
            setHintTextColor(muted); setTextColor(white); textSize = 13f
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setSingleLine(true); setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(panel2, 12)
        }
        body.addView(keyInput, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(7) })
        body.addView(button("SIMPAN API KEY DENGAN AMAN", panel2, white, 46) {
            val key = keyInput.text.toString().trim()
            if (key.length < 16) showToast("API key belum valid.")
            else {
                secretStore.saveApiKey(key)
                keyInput.setText("")
                showToast("API key tersimpan aman. Fingerprint: " + fingerprint(key))
            }
        }, LinearLayout.LayoutParams(-1, dp(46)).apply { topMargin = dp(9) })
        body.addView(button("LIHAT FINGERPRINT API KEY", panel2, white, 46) {
            val key = secretStore.readApiKey()
            showToast(if (key.isNullOrBlank()) "API key belum tersimpan." else "Fingerprint: " + fingerprint(key))
        }, LinearLayout.LayoutParams(-1, dp(46)).apply { topMargin = dp(8) })
        body.addView(button("TES KONEKSI HERMES", orange, Color.WHITE, 46) {
            val key = secretStore.readApiKey()
            if (key.isNullOrBlank()) showToast("Simpan API key terlebih dahulu.")
            else {
                showToast("Menguji koneksi Hermes...")
                Thread {
                    try {
                        val result = hermesClient.testConnection(key)
                        runOnUiThread { showToast(result) }
                    } catch (e: Exception) {
                        runOnUiThread { showToast("Koneksi gagal: " + (e.message ?: "unknown")) }
                    }
                }.start()
            }
        }, LinearLayout.LayoutParams(-1, dp(46)).apply { topMargin = dp(8) })
        body.addView(text("AKSESIBILITAS", 11f, muted, true).apply { setPadding(0, dp(22), 0, dp(8)) })
        body.addView(button("BUKA PENGATURAN ACCESSIBILITY", panel2, white, 46) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }, LinearLayout.LayoutParams(-1, dp(46)))
        body.addView(text("Layanan saat ini hanya kerangka dasar. Kontrol layar belum diaktifkan.", 12f, muted).apply { setPadding(0, dp(9), 0, 0) })
        scroll.addView(body)
        return scroll
    }

    private fun handleCommand(raw: String) {
        when (val command = CommandParser.parse(raw)) {
            JarvisCommand.OPEN_ACCESSIBILITY_SETTINGS -> startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            JarvisCommand.UNKNOWN -> showToast("Perintah belum dikenali. Coba: buka Chrome, YouTube, WhatsApp, Maps, atau Gmail.")
            else -> command.packageName?.let { launchPackage(it, command.label) }
        }
    }

    private fun launchPackage(packageName: String, label: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent == null) showToast("$label tidak ditemukan atau belum terpasang.")
        else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            showToast("Membuka $label.")
        }
    }

    private fun quickCard(icon: String, title: String, subtitle: String, action: () -> Unit): View {
        val card = column().apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = rounded(panel, 15)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        card.addView(text(icon, 21f, orange, true))
        card.addView(text(title, 15f, white, true).apply { setPadding(0, dp(5), 0, dp(2)) })
        card.addView(text(subtitle, 11f, muted))
        return card
    }

    private fun navItem(icon: String, title: String, action: () -> Unit): View {
        val item = column().apply {
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        item.addView(text(icon, 20f, if (title.lowercase() == selectedTab) orange else white, true))
        item.addView(text(title, 10f, muted, true).apply { setPadding(0, dp(3), 0, 0) })
        return item
    }

    private fun column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    private fun text(value: String, size: Float, color: Int, bold: Boolean = false) = TextView(this).apply {
        text = value; textSize = size; setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }
    private fun button(value: String, color: Int, textColor: Int, height: Int, action: () -> Unit) = Button(this).apply {
        text = value; textSize = 11f; setTextColor(textColor); isAllCaps = false
        typeface = Typeface.DEFAULT_BOLD
        minHeight = dp(height); minimumHeight = dp(height)
        minWidth = 0; minimumWidth = 0
        stateListAnimator = null
        background = rounded(color, 12)
        setPadding(dp(10), 0, dp(10), 0)
        isClickable = true; isFocusable = true
        setOnClickListener { action() }
    }
    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }
    private fun gradient() = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(255_112_35, 205_55_8)).apply { cornerRadius = dp(19).toFloat() }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun fingerprint(key: String): String = MessageDigest.getInstance("SHA-256")
        .digest(key.toByteArray(Charsets.UTF_8)).take(6).joinToString("") { "%02x".format(it) }
    private fun showToast(message: String) = android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    private fun refreshStatus() {
        if (!::status.isInitialized) return
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
        val active = enabled.contains(packageName + "/" + JarvisAccessibilityService::class.java.name)
        status.text = if (active) "● ACCESSIBILITY AKTIF  •  HERMES SIAP" else "● ACCESSIBILITY BELUM AKTIF  •  HERMES LOKAL"
        status.setTextColor(if (active) orange else muted)
    }
}