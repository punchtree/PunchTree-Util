package net.punchtree.util.tools.placement

import net.punchtree.util.playingcards.CardInventoryListener
import net.punchtree.util.playingcards.PlayingCardUtils
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot

class PlacementToolListener : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.action == Action.LEFT_CLICK_AIR || event.action == Action.LEFT_CLICK_BLOCK) {
            if (PlacementTool.onLeftClick(event.player)) {
                event.isCancelled = true
                event.setUseItemInHand(Event.Result.DENY)
            }
        } else if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            if (PlacementTool.onRightClick(event.player)) {
                event.isCancelled = true
                event.setUseItemInHand(Event.Result.DENY)
            }
        }
    }

    @EventHandler
    fun onChangeHotbarSlot(event: PlayerItemHeldEvent) {
        if (event.previousSlot == event.newSlot) return
        if (!event.player.isSneaking) {
            PlacementTool.updatePreview(event.player)
            return
        }
        if (PlacementTool.onScroll(event.player, calculateScroll(event.previousSlot, event.newSlot))) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onSwapHand(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        if (PlacementTool.adjustVerticalOffSet(player)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        PlacementTool.disableFor(player)
    }

    companion object {
        internal fun calculateScroll(previousSlot: Int, newSlot: Int) : Int {
            val leftDistance = (previousSlot - newSlot + 9) % 9
            val rightDistance = (newSlot - previousSlot + 9) % 9

            return if (leftDistance < rightDistance) leftDistance else -rightDistance
        }
    }

}
