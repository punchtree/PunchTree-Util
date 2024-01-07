package net.punchtree.util.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private const val DEFAULT_MAX_DISTANCE = 3

object UtiliKillCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>?): Boolean {

        if (sender !is Player) return false
        if (args == null || args.isEmpty()) {
            return false
        }

        val tagToKill = args[0]
        var maxDistance = DEFAULT_MAX_DISTANCE
        if (args.size >= 2) {
            args[1].toIntOrNull()?.let {
                maxDistance = it
            }
        }
        var type: String? = null
        if (args.size >= 3) {
            type = args[2]
        }
        val typeSelector = if (type == null) "" else ",type=$type"
        sender.performCommand("minecraft:kill @e[tag=$tagToKill,limit=1,distance=..$maxDistance$typeSelector]")

        return true
    }


}