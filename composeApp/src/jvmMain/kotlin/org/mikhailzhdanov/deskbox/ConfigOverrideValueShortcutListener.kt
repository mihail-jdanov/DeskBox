package org.mikhailzhdanov.deskbox

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener

class ConfigOverrideValueShortcutListener(
    val handler: () -> Unit
) : NativeKeyListener {

    override fun nativeKeyPressed(nativeEvent: NativeKeyEvent?) {
        nativeEvent?.let { e ->
            if (
                e.modifiers and NativeKeyEvent.CTRL_MASK != 0 &&
                e.modifiers and NativeKeyEvent.SHIFT_MASK != 0 &&
                e.modifiers and NativeKeyEvent.ALT_MASK != 0 &&
                e.keyCode == NativeKeyEvent.VC_O
            ) {
                handler()
            }
        }
    }

}