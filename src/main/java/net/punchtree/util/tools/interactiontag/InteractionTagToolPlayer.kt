package net.punchtree.util.tools.interactiontag

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.punchtree.util.color.PunchTreeColor
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player

class InteractionTagToolPlayer(private val player: Player) {

    // TODO raycast in inspect mode

    private var mode = InteractionTagTool.Mode.INSPECT
    private var tags = mutableSetOf<String>()

    init {
        player.sendMessage(INTERACTION_TAG_TOOL_PREFIX.append(Component.text("Interaction Tag Tool enabled (inspect mode)")))
    }

    internal fun onPerformAction(interaction: Interaction) {
        when (mode) {
            InteractionTagTool.Mode.INSPECT -> {
                performInspect(interaction)
            }
            InteractionTagTool.Mode.ADD -> {
               performAddTags(interaction)
            }
            InteractionTagTool.Mode.REMOVE -> {
                onRemoveTags(interaction)
            }
            InteractionTagTool.Mode.CLEAR -> {
                onClearTags(interaction)
            }
        }
    }

    internal fun performInspect(interaction: Interaction) {
        player.sendMessage(INTERACTION_TAG_TOOL_PREFIX.append(Component.text("Inspect Tags: ${interaction.scoreboardTags.joinToString(", ")}")))
        player.sendActionBar(Component.text(when (mode) {
            InteractionTagTool.Mode.INSPECT -> "Inspecting"
            InteractionTagTool.Mode.ADD -> "Adding: ${tags.joinToString(", ")}"
            InteractionTagTool.Mode.REMOVE -> "Removing: ${tags.joinToString(", ")}"
            InteractionTagTool.Mode.CLEAR -> "Clearing all tags"
        }).color(NamedTextColor.GRAY))
    }

    private fun performAddTags(interaction: Interaction) {
        val existingTags = interaction.scoreboardTags
        val newTags = tags - existingTags
        val intersectingExistingTags = tags.intersect(existingTags)
        val nonintersectingExistingTags = existingTags - intersectingExistingTags
        val reportMessage = INTERACTION_TAG_TOOL_PREFIX.append(Component.text("Add Tags: ")).append(
            Component.join(
                JoinConfiguration.commas(true),
                nonintersectingExistingTags.map { Component.text(it).color(NamedTextColor.GRAY) })
                //.color(NamedTextColor.GRAY)
        ).append(
            Component.space()
        ).append(
            Component.join(
                JoinConfiguration.commas(true),
                intersectingExistingTags.map { Component.text(it).color(PunchTreeColor(153, 187, 153)) })
                //.color(NamedTextColor.GRAY)
        ).append(
            Component.space()
        ).append(
            Component.join(
                JoinConfiguration.commas(true),
                newTags.map { Component.text(it).color(NamedTextColor.GREEN) })
                //.color(NamedTextColor.GRAY)
        )
        tags.forEach(interaction::addScoreboardTag)
        player.sendMessage(reportMessage)
    }

    private fun onRemoveTags(interaction: Interaction) {
        // TODO remove messaging
        val existingTags = interaction.scoreboardTags
        val remainingTags = existingTags - tags
        val removedTags = existingTags - remainingTags

        val reportMessage = INTERACTION_TAG_TOOL_PREFIX.append(Component.text("Remove Tags: ")).append(
            Component.join(
                JoinConfiguration.commas(true),
                remainingTags.map { Component.text(it).color(NamedTextColor.GRAY) })
        ).append(
            Component.space()
        ).append(
            Component.join(
                JoinConfiguration.commas(true),
                removedTags.map { Component.text(it).color(NamedTextColor.RED) })
        )

        tags.forEach(interaction::removeScoreboardTag)
        player.sendMessage(reportMessage)
    }

    private fun onClearTags(interaction: Interaction) {
        val tagsOnInteraction = interaction.scoreboardTags.toSet()
        val reportMessage = INTERACTION_TAG_TOOL_PREFIX.append(Component.text("Cleared Tags: ")).append(
            Component.join(
                JoinConfiguration.commas(true),
                tagsOnInteraction.map { Component.text(it).color(NamedTextColor.RED) })
        )
        tagsOnInteraction.forEach(interaction::removeScoreboardTag)
        player.sendMessage(reportMessage)
    }

    fun disable() {
        mode = InteractionTagTool.Mode.INSPECT
        tags.clear()
        player.sendMessage(INTERACTION_TAG_TOOL_PREFIX.append(Component.text("Interaction Tag Tool disabled")))
    }

    fun switchToInspect() {
        mode = InteractionTagTool.Mode.INSPECT
        this.tags.clear()
        player.sendMessage(INTERACTION_TAG_TOOL_PREFIX.append(Component.text("Switched to inspect mode")))
    }

    fun switchToAddTags(tags: Set<String>) {
        mode = InteractionTagTool.Mode.ADD
        this.tags = tags.toMutableSet()
        player.sendMessage(INTERACTION_TAG_TOOL_PREFIX.append(Component.text("Switched to add tags mode")))
    }

    fun switchToRemoveTags(tags: Set<String>) {
        mode = InteractionTagTool.Mode.REMOVE
        this.tags = tags.toMutableSet()
        player.sendMessage(INTERACTION_TAG_TOOL_PREFIX.append(Component.text("Switched to remove tags mode")))
    }

    fun switchToClearTags() {
        mode = InteractionTagTool.Mode.CLEAR
        this.tags.clear()
        player.sendMessage(INTERACTION_TAG_TOOL_PREFIX.append(Component.text("Switched to clear tags mode")))
    }

}
