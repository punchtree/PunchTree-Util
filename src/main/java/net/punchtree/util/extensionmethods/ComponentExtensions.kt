package net.punchtree.util.extensionmethods

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

fun Component.textContent(): String {
    return if (this is TextComponent) {
        content()
    } else ""
}