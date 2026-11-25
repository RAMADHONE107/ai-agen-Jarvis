package com.jarvis.mobileagent

import org.junit.Assert.assertEquals
import org.junit.Test

class CommandParserTest {
    @Test fun parsesChrome() {
        assertEquals(JarvisCommand.OPEN_CHROME, CommandParser.parse("buka Chrome"))
    }
    @Test fun parsesWhatsAppAlias() {
        assertEquals(JarvisCommand.OPEN_WHATSAPP, CommandParser.parse("jalankan WA"))
    }
    @Test fun parsesAccessibilitySettings() {
        assertEquals(JarvisCommand.OPEN_ACCESSIBILITY_SETTINGS, CommandParser.parse("buka pengaturan aksesibilitas"))
    }
    @Test fun rejectsUnknownApp() {
        assertEquals(JarvisCommand.UNKNOWN, CommandParser.parse("hapus semua data"))
    }
}
