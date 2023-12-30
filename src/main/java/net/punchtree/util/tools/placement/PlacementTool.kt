package net.punchtree.util.tools.placement

import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PlacementTool {

    private val playersUsingPlacementTool = mutableMapOf<Player, PlacementToolPlayer>()

    internal fun toggleFor(player: Player): Boolean {
        if (playersUsingPlacementTool.containsKey(player)) {
            disableFor(player)
            return false
        } else {
            enableFor(player)
            return true
        }
    }

    internal fun enableFor(player: Player) {
        playersUsingPlacementTool[player] = PlacementToolPlayer(player)
    }

    internal fun disableFor(player: Player) {
        playersUsingPlacementTool.remove(player)?.disable()
    }

    internal fun onMove(player: Player) {
        val t = playersUsingPlacementTool[player]
        if (t != null) {
            t.placePreviewAtRaycast()
        }
    }

    fun onDisable() {
        playersUsingPlacementTool.values.forEach { it.disable() }
    }

    fun onLeftClick(player: Player): Boolean {
        playersUsingPlacementTool[player]?.let {
            it.destroy()
            return true
        }
        return false
    }

    fun onRightClick(player: Player): Boolean {
        playersUsingPlacementTool[player]?.let {
            it.place()
            return true
        }
        return false
    }

    fun onScroll(player: Player, scrollAmount: Int): Boolean {
        playersUsingPlacementTool[player]?.let {
            it.rotate(scrollAmount)
            return true
        }
        return false
    }

    fun updatePreview(player: Player) {
        playersUsingPlacementTool[player]?.placePreviewAtRaycast()
    }

    fun adjustVerticalOffSet(player: Player): Boolean {
        if (player.isSneaking) {
            playersUsingPlacementTool[player]?.let {
                it.adjustVerticalOffset(false)
                return true
            }
        } else {
            playersUsingPlacementTool[player]?.let {
                it.adjustVerticalOffset(true)
                return true
            }
        }
        return false
    }

}