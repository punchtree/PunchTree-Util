package net.punchtree.util.tools.interactionplacement

import net.punchtree.util.tools.placement.PlacementToolPlayer
import org.bukkit.entity.Player


object InteractionPlacementTool {

    private val playersUsingInteractionPlacementTool = mutableMapOf<Player, InteractionPlacementToolPlayer>()

    internal fun toggleFor(player: Player): Boolean {
        if (playersUsingInteractionPlacementTool.containsKey(player)) {
            disableFor(player)
            return false
        } else {
            enableFor(player)
            return true
        }
    }

    internal fun enableFor(player: Player) {
        playersUsingInteractionPlacementTool[player] = InteractionPlacementToolPlayer(player)
    }

    internal fun disableFor(player: Player) {
        playersUsingInteractionPlacementTool.remove(player)?.disable()
    }

    internal fun onMove(player: Player) {
        playersUsingInteractionPlacementTool[player]?.placePreviewAtRaycast()
    }

    fun onDisable() {
        playersUsingInteractionPlacementTool.values.forEach { it.disable() }
    }

    fun onLeftClick(player: Player): Boolean {
        playersUsingInteractionPlacementTool[player]?.let {
            it.destroy()
            return true
        }
        return false
    }

    fun onRightClick(player: Player): Boolean {
        playersUsingInteractionPlacementTool[player]?.let {
            it.place()
            return true
        }
        return false
    }

    fun onAdjustHeight(player: Player, scrollAmount: Int): Boolean {
        playersUsingInteractionPlacementTool[player]?.let {
            it.adjustHeight(scrollAmount)
            return true
        }
        return false
    }

    fun onAdjustDistance(player: Player, scrollAmount: Int): Boolean {
        playersUsingInteractionPlacementTool[player]?.let {
            it.adjustDistance(scrollAmount)
            return true
        }
        return false
    }

    fun decreaseHorizontalSize(player: Player): Boolean {
        playersUsingInteractionPlacementTool[player]?.let {
            it.decreaseHorizontalSize()
            return true
        }
        return false
    }

    fun increaseHorizontalSize(player: Player): Boolean {
        playersUsingInteractionPlacementTool[player]?.let {
            it.increaseHorizontalSize()
            return true
        }
        return false
    }



}