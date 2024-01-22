package net.punchtree.util.tools.interactionplacement

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.regex.Pattern

object InteractionPlacementToolCommand : CommandExecutor, TabCompleter {

    private val validTagPattern = Pattern.compile("^[a-z0-9-:]+$")

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) return false
        if (args == null || args.isEmpty()) {
            val nowEnabled = InteractionPlacementTool.toggleFor(sender)
            sender.sendMessage("Interaction placement tool ${if (nowEnabled) "enabled" else "disabled"}")
            return true
        }

        when(args[0]) {
//            "test" -> {
//                sender.sendMessage("Interaction Test")
//                val testInteraction = sender.world.spawnEntity(sender.location, EntityType.INTERACTION, CreatureSpawnEvent.SpawnReason.CUSTOM) {
//                    it.isGlowing = true
//                    it.addScoreboardTag("test-interaction")
//                    it.addScoreboardTag("loqinttemp")
//                } as Interaction
//                val testDisplay = sender.world.spawnEntity(sender.location.apply {
//                     y += 0.5
//                     yaw = 0f
//                     pitch = 0f
//                }, EntityType.ITEM_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM) {
//                    (it as ItemDisplay).itemStack = ItemStack(Material.WHITE_CONCRETE, 1)
//                    it.isGlowing = true
//                    it.addScoreboardTag("test-display")
//                    it.addScoreboardTag("loqinttemp")
//                } as ItemDisplay
//            }
            "on" -> {
                InteractionPlacementTool.enableFor(sender)
                sender.sendMessage("Interaction placement tool enabled")
            }
            "off" -> {
                InteractionPlacementTool.disableFor(sender)
                sender.sendMessage("Interaction placement tool disabled")
            }
            "addtag" -> {
                if (args.size < 2) return false
                if (!validTagPattern.matcher(args[1]).matches()) {
                    sender.sendMessage("Invalid tag: ${args[1]}. Valid characters are a-z, 0-9, -, and :")
                    return true
                }
                InteractionPlacementTool.addTagToAdd(sender, args[1])
            }
            "removetag" -> {
                if (args.size < 2) return false
                if (!validTagPattern.matcher(args[1]).matches()) {
                    sender.sendMessage("Invalid tag: ${args[1]}. Valid characters are a-z, 0-9, -, and :")
                    return true
                }
                InteractionPlacementTool.removeTagToAdd(sender, args[1])
            }
            "cleartags" -> {
                InteractionPlacementTool.clearTagsToAdd(sender)
            }
            "help" -> {
                sender.sendMessage("Interaction placement tool help:")
                sender.sendMessage("/ipt - toggle the interaction placement tool on and off")
                sender.sendMessage("/ipt on - enable the interaction placement tool")
                sender.sendMessage("/ipt off - disable the interaction placement tool")
                sender.sendMessage("/ipt addtag <tag> - add a tag to be added to placed interactions")
                sender.sendMessage("/ipt removetag <tag> - remove a tag from being added to placed interactions")
                sender.sendMessage("/ipt cleartags - clear all tags to be added to placed interactions")
            }
            else -> return false
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>?): MutableList<String>? {
        if (args == null || args.isEmpty() || args.size <= 1) return mutableListOf("on", "off")
        return mutableListOf()
    }

}