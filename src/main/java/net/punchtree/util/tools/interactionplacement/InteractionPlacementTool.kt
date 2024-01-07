package net.punchtree.util.tools.interactionplacement

import net.punchtree.util.tools.placement.PlacementToolPlayer
import org.bukkit.entity.Player

object InteractionPlacementTool {

    private val playersUsingInteractionPlacementTool = mutableMapOf<Player, PlacementToolPlayer>()

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
        playersUsingInteractionPlacementTool[player] = PlacementToolPlayer(player)
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

    fun onScroll(player: Player, scrollAmount: Int): Boolean {
        playersUsingInteractionPlacementTool[player]?.let {
            it.rotate(scrollAmount)
            return true
        }
        return false
    }

    fun updatePreview(player: Player) {
        playersUsingInteractionPlacementTool[player]?.placePreviewAtRaycast()
    }

    fun adjustVerticalOffSet(player: Player): Boolean {
        if (player.isSneaking) {
            playersUsingInteractionPlacementTool[player]?.let {
                it.adjustVerticalOffset(false)
                return true
            }
        } else {
            playersUsingInteractionPlacementTool[player]?.let {
                it.adjustVerticalOffset(true)
                return true
            }
        }
        return false
    }

}