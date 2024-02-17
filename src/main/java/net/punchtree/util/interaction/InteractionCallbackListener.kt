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

private const val CALLBACK_PREFIX = "punchtree:callback:"

class InteractionCallbackListener : Listener {

    // TODO extract separate callback management

    @EventHandler
    fun onEntityInteractEvent(event: PlayerInteractAtEntityEvent) {
        if (event.rightClicked.type != EntityType.INTERACTION) return
        if (event.hand != EquipmentSlot.HAND) return
        val interaction = event.rightClicked as Interaction
        interaction.scoreboardTags.forEach {
            if (isCallbackTag(it) && callbackRightClick(it, interaction, event.player)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPrePlayerAttack(event: PrePlayerAttackEntityEvent) {
        if (event.attacked.type != EntityType.INTERACTION) return
        val interaction = event.attacked as Interaction
        interaction.scoreboardTags.forEach {
            if (isCallbackTag(it) && callbackLeftClick(it, interaction, event.player)) {
                event.isCancelled = true
            }
        }
    }

    private fun isCallbackTag(tag: String?): Boolean {
        return tag?.startsWith(CALLBACK_PREFIX) ?: false
    }

    private fun callbackRightClick(tag: String, interaction: Interaction, player: Player): Boolean {
        return callbackMap[tag.substring(CALLBACK_PREFIX.length)]?.onRightClick(interaction, player) ?: false
    }

    private fun callbackLeftClick(tag: String, interaction: Interaction, player: Player): Boolean {
        return callbackMap[tag.substring(CALLBACK_PREFIX.length)]?.onLeftClick(interaction, player) ?: false
    }

    companion object {
        private val callbackMap = mutableMapOf<String, InteractionCallback>()
        fun registerCallback(tag: String, callback: InteractionCallback) {
            Bukkit.getLogger().fine("registering callback for tag $tag")
            callbackMap[tag] = callback
        }
    }
}
