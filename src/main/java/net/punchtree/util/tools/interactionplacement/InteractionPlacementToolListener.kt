package net.punchtree.util.tools.interactionplacement

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import net.punchtree.util.tools.placement.PlacementTool
import net.punchtree.util.tools.placement.PlacementToolListener.Companion.calculateScroll
import org.bukkit.entity.EntityType
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot

/*
 *  Right click/Left click - Place/Destroy
 *  Expand hor      - F
 *  Contract hor    - Shift-F
 *  Expand height   - Shift Scroll Up
 *  Contract height - Shift Scroll Down
 *  Make farther    - Scroll Up
 *  Make closer     - Scroll Down
 */

class InteractionPlacementToolListener : Listener {

    @EventHandler
    fun onChangeHotbarSlot(event: PlayerItemHeldEvent) {
        if (event.previousSlot == event.newSlot) return
        if (event.player.isSneaking) {
            InteractionPlacementTool.onAdjustDistance(event.player, calculateScroll(event.previousSlot, event.newSlot))
        } else {
            InteractionPlacementTool.onAdjustHeight(event.player, calculateScroll(event.previousSlot, event.newSlot))
        }
        event.isCancelled = true
    }

    @EventHandler
    fun onSwapHand(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        if (player.isSneaking) {
            InteractionPlacementTool.decreaseHorizontalSize(player)
        } else {
            InteractionPlacementTool.increaseHorizontalSize(player)
        }
        event.isCancelled = true
    }

    // with interactions, this is unnecessary, as you will always be left-clicking or right-clicking the preview interaction entity
    // (unless you starting moving the mouse really fast and click outside of the preview itneraction entity)
    // If the preview interaction is ever removed in the implementation, this will start doing the work
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.action == Action.LEFT_CLICK_AIR || event.action == Action.LEFT_CLICK_BLOCK) {
            if (InteractionPlacementTool.onLeftClick(event.player)) {
                event.isCancelled = true
                event.setUseItemInHand(Event.Result.DENY)
            }
        } else if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            if (InteractionPlacementTool.onRightClick(event.player)) {
                event.isCancelled = true
                event.setUseItemInHand(Event.Result.DENY)
            }
        }
    }

    // this and the next listener handle left and right click detection on the preview interaction entity
    @EventHandler
    fun onPrePlayerAttack(event: PrePlayerAttackEntityEvent) {
        if (event.attacked.type != EntityType.INTERACTION) return
        if (InteractionPlacementTool.onLeftClick(event.player)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onEntityInteractEvent(event: PlayerInteractAtEntityEvent) {
        if (event.rightClicked.type != EntityType.INTERACTION) return
        if (event.hand != EquipmentSlot.HAND) return
        if (InteractionPlacementTool.onRightClick(event.player)) {
            event.isCancelled = true
        }
    }

}