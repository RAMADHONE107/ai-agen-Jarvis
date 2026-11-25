package com.jarvis.mobileagent

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Stage 0 service registration only.
 * Screen inspection, clicks, gestures and text entry are deliberately disabled
 * until explicit user-approved controls and verification are implemented.
 */
class JarvisAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Intentionally no screen content collection in this milestone.
    }

    override fun onInterrupt() {
        // Nothing to interrupt yet.
    }
}
