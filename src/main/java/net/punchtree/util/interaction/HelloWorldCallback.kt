package net.punchtree.util.interaction

import org.bukkit.entity.Interaction
import org.bukkit.entity.Player

object HelloWorldCallback : InteractionCallback {

    // TODO Annotation processing to register callbacks

    override fun onRightClick(interaction: Interaction, player: Player): Boolean {
        player.sendMessage("Hello, world!")
        return true
    }

    override fun onLeftClick(interaction: Interaction, player: Player): Boolean {
        player.sendMessage("Goodbye, world!")
        return true
    }

}