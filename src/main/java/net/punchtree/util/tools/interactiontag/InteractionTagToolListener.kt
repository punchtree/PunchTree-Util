package net.punchtree.util.tools.interactiontag

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.EquipmentSlot

class InteractionTagToolListener : Listener {

    @EventHandler
    fun onEntityInteractEvent(event: PlayerInteractAtEntityEvent) {
        if (event.rightClicked.type != EntityType.INTERACTION) return
        if (event.hand != EquipmentSlot.HAND) return
        if (InteractionTagTool.onRightClickInteraction(event.player, event.rightClicked as Interaction)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPrePlayerAttack(event: PrePlayerAttackEntityEvent) {
        if (event.attacked.type != EntityType.INTERACTION) return
        if (InteractionTagTool.onLeftClickInteraction(event.player, event.attacked as Interaction)) {
            event.isCancelled = true
        }
    }
}