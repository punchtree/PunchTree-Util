package net.punchtree.util.tools.interactiontag

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player

val INTERACTION_TAG_TOOL_PREFIX = MiniMessage.miniMessage().deserialize("<red>[</red>ITT<red>]</red> ")

object InteractionTagTool {

    enum class Mode {
        INSPECT,
        ADD,
        /** removes a single specified tag */
        REMOVE,
        CLEAR
    }

    private val playersUsingInteractionTagTool = mutableMapOf<Player, InteractionTagToolPlayer>()

    internal fun onDisable() {
        playersUsingInteractionTagTool.values.forEach { it.disable() }
        playersUsingInteractionTagTool.clear()
    }

    fun toggleFor(sender: Player) {
        if (playersUsingInteractionTagTool.containsKey(sender)) {
            disableFor(sender)
        } else {
            enableFor(sender)
        }
    }

    fun enableFor(player: Player) {
        playersUsingInteractionTagTool[player] = InteractionTagToolPlayer(player)
    }

    fun disableFor(player: Player) {
        playersUsingInteractionTagTool.remove(player)?.disable()
    }

    fun onRightClickInteraction(player: Player, interaction: Interaction): Boolean {
        playersUsingInteractionTagTool[player]?.let {
            it.performInspect(interaction)
            return true
        }
        return false
    }

    fun onLeftClickInteraction(player: Player, interaction: Interaction): Boolean {
        playersUsingInteractionTagTool[player]?.let {
            it.onPerformAction(interaction)
            return true
        }
        return false
    }

    fun switchToInspect(sender: Player) {
        playersUsingInteractionTagTool[sender]?.let {
            it.switchToInspect()
        }
    }

    fun switchToAddTags(player: Player, toSet: Set<String>) {
        playersUsingInteractionTagTool[player]?.let {
            it.switchToAddTags(toSet)
        }
    }

    fun switchToRemoveTags(sender: Player, toSet: Set<String>) {
        playersUsingInteractionTagTool[sender]?.let {
            it.switchToRemoveTags(toSet)
        }
    }

    fun switchToClearTags(sender: Player) {
        playersUsingInteractionTagTool[sender]?.let {
            it.switchToClearTags()
        }
    }

}