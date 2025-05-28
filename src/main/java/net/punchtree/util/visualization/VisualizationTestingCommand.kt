package net.punchtree.util.visualization

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.util.Vector

object VisualizationTestingCommand : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if ( sender !is Player) return false
        if (args.isEmpty()) return false

        when (val subcommand = args[0].lowercase()) {
            "modeltest" -> doModelTest(sender, args)
            "line" -> VisualizationTesting.doLine(sender)
            "triangle", "tri" -> VisualizationTesting.doTriangle(sender)
            "triangle-path" -> VisualizationTesting.doTrianglePath(sender)
            "cuboid" -> VisualizationTesting.doCuboid(sender, Vector(5, 5, 5))
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
            "triangle", "tri" -> VisualizationTesting.modelTest(sender, Visualization.TRIANGLE_MODEL)
            "cuboid" -> VisualizationTesting.modelTest(sender, Visualization.CUBOID_MODEL)

            else -> sender.sendMessage("Unknown model: $model")
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String>? {
        return when {
            args.size <= 1 -> {
                mutableListOf("modeltest", "line", "triangle", "triangle-path", "cuboid")
            }
            args.size == 2 && args[0].lowercase()  == "modeltest" -> {
                mutableListOf("line", "triangle")
            }
            else -> {
                mutableListOf()
            }
        }
    }
}