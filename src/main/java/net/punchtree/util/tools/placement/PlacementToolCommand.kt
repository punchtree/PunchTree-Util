package net.punchtree.util.tools.placement

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object PlacementToolCommand : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        if (args.isEmpty()) {
            val nowEnabled = PlacementTool.toggleFor(sender)
            sender.sendMessage("Placement tool ${if (nowEnabled) "enabled" else "disabled"}")
            return true
        }

        when(args[0]) {
            "on" -> {
                PlacementTool.enableFor(sender)
                sender.sendMessage("Placement tool enabled")
            }
            "off" -> {
                PlacementTool.disableFor(sender)
                sender.sendMessage("Placement tool disabled")
            }
            else -> return false
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
        if (args.isEmpty() || args.size <= 1) return mutableListOf("on", "off")
        return mutableListOf()
    }

}