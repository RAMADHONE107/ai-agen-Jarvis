package com.jarvis.mobileagent

enum class JarvisCommand(val packageName: String?, val label: String) {
    OPEN_CHROME("com.android.chrome", "Chrome"),
    OPEN_YOUTUBE("com.google.android.youtube", "YouTube"),
    OPEN_WHATSAPP("com.whatsapp", "WhatsApp"),
    OPEN_MAPS("com.google.android.apps.maps", "Google Maps"),
    OPEN_GMAIL("com.google.android.gm", "Gmail"),
    OPEN_ACCESSIBILITY_SETTINGS(null, "Pengaturan Aksesibilitas"),
    UNKNOWN(null, "Perintah belum dikenali")
}

object CommandParser {
    fun parse(raw: String): JarvisCommand {
        val command = raw.trim().lowercase()
            .replace("buka aplikasi ", "buka ")
            .replace("jalankan ", "buka ")
        if (command.contains("aksesibilitas") || command.contains("accessibility")) {
            return JarvisCommand.OPEN_ACCESSIBILITY_SETTINGS
        }
        if (!command.startsWith("buka ")) return JarvisCommand.UNKNOWN
        val target = command.removePrefix("buka ").trim()
        return when {
            target == "chrome" || target.contains("google chrome") -> JarvisCommand.OPEN_CHROME
            target == "youtube" || target.contains("you tube") -> JarvisCommand.OPEN_YOUTUBE
            target == "whatsapp" || target == "wa" -> JarvisCommand.OPEN_WHATSAPP
            target == "maps" || target.contains("google maps") || target == "peta" -> JarvisCommand.OPEN_MAPS
            target == "gmail" || target.contains("google mail") -> JarvisCommand.OPEN_GMAIL
            else -> JarvisCommand.UNKNOWN
        }
    }
}
