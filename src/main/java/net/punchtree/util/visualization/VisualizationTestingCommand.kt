package net.punchtree.util.visualization

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object VisualizationTestingCommand : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {

        if ( sender !is Player) return false
        if (args == null || args.isEmpty()) return false

        when (val subcommand = args[0].lowercase()) {
            "modeltest" -> doModelTest(sender, args)
            "line" -> VisualizationTesting.doLine(sender)
//            "circle" -> VisualizationTesting.doCircle(sender)
//            "cleanup" -> VisualizationTesting.onDisable()
            else -> sender.sendMessage("Unknown subcommand: $subcommand")
        }
        return true
    }

    private fun doModelTest(sender: Player, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage("Usage: /visualization modeltest <model>")
            return
        }

        when (val model = args[1]) {
            "line" -> VisualizationTesting.modelTest(sender, Visualization.LINE_MODEL)

            else -> sender.sendMessage("Unknown model: $model")
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): MutableList<String>? {
        return when {
            args == null || args.size <= 1 -> {
                mutableListOf("modeltest", "line")
            }
            args.size == 2 && args[0].lowercase()  == "modeltest" -> {
                mutableListOf("line")
            }
            else -> {
                mutableListOf()
            }
        }
    }
}