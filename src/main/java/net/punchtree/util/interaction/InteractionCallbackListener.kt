package net.punchtree.util.interaction

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.EquipmentSlot

class InteractionCallbackListener : Listener {

    @EventHandler
    fun onEntityInteractEvent(event: PlayerInteractAtEntityEvent) {
        if (event.rightClicked.type != EntityType.INTERACTION) return
        if (event.hand != EquipmentSlot.HAND) return
        val interaction = event.rightClicked as Interaction
        interaction.scoreboardTags.forEach {
            if (callbackRightClick(it, interaction, event.player)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPrePlayerAttack(event: PrePlayerAttackEntityEvent) {
        if (event.attacked.type != EntityType.INTERACTION) return
        val interaction = event.attacked as Interaction
        interaction.scoreboardTags.forEach {
            if (callbackLeftClick(it, interaction, event.player)) {
                event.isCancelled = true
            }
        }
    }

    private fun callbackRightClick(tag: String, interaction: Interaction, player: Player): Boolean {
        return InteractionCallbackManager.getCallback(tag)?.onRightClick(interaction, player) ?: false
    }

    private fun callbackLeftClick(tag: String, interaction: Interaction, player: Player): Boolean {
        return InteractionCallbackManager.getCallback(tag)?.onLeftClick(interaction, player) ?: false
    }

}
