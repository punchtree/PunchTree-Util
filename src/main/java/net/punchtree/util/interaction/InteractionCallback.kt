package net.punchtree.util.interaction

import org.bukkit.entity.Interaction
import org.bukkit.entity.Player

interface InteractionCallback {

    fun onRightClick(interaction: Interaction, player: Player): Boolean
    fun onLeftClick(interaction: Interaction, player: Player): Boolean

}
