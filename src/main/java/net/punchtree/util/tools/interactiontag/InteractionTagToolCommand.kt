package net.punchtree.util.tools.interactiontag

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object InteractionTagToolCommand : CommandExecutor, TabCompleter {

    // TODO we need to prevent people from using multiple tools at once. Some sort of property set on the player that
    //  gets inspected before enabling any tool
    // TODO we should extract an interface where we're triplicating, and potentially we could use that abstraction to
    //  help with the problem of enforcing only one tool at a time (some sort of manager that tracks a link between
    //  one player to one tool in abstract)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) return false
        if (args == null || args.isEmpty()) {
            InteractionTagTool.toggleFor(sender)
            return true
        }

        // TODO messages indicating that you switched modes
        when(args[0]) {
            "on" -> {
                InteractionTagTool.enableFor(sender)
            }
            "off" -> {
                InteractionTagTool.disableFor(sender)
            }
            "inspect" -> {
                InteractionTagTool.switchToInspect(sender)
            }
            "addtags" -> {
                if (args.size < 2) {
                    sender.sendMessage("Usage: /itt addtags <tag1> [tag2] ...")
                    return true
                }
                // we need to send the rest of the args as a set of strings
                InteractionTagTool.switchToAddTags(sender, args.slice(1 until args.size).toSet())
            }
            "removetags" -> {
                if (args.size < 2) {
                    sender.sendMessage("Usage: /itt removetags <tag1> [tag2] ...")
                    return true
                }
                // we need to send the rest of the args as a set of strings
                InteractionTagTool.switchToRemoveTags(sender, args.slice(1 until args.size).toSet())
            }
            "cleartags" -> {
                InteractionTagTool.switchToClearTags(sender)
            }
            else -> return false
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>?): MutableList<String>? {
        if (args == null || args.isEmpty() || args.size <= 1) return mutableListOf("on", "off", "inspect", "addtags", "removetags", "cleartags")
        return mutableListOf()
    }

}