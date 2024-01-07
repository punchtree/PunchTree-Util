package net.punchtree.util.tools.interactionplacement

import net.punchtree.util.tools.placement.PlacementTool
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.ItemStack

object InteractionPlacementToolCommand : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) return false
        if (args == null || args.isEmpty()) {
            val nowEnabled = PlacementTool.toggleFor(sender)
            sender.sendMessage("Placement tool ${if (nowEnabled) "enabled" else "disabled"}")
            return true
        }

        when(args[0]) {
            "test" -> {
                sender.sendMessage("Interaction Test")
                val testInteraction = sender.world.spawnEntity(sender.location, EntityType.INTERACTION, CreatureSpawnEvent.SpawnReason.CUSTOM) {
                    it.isGlowing = true
                    it.addScoreboardTag("test-interaction")
                    it.addScoreboardTag("loqinttemp")
                } as Interaction
                val testDisplay = sender.world.spawnEntity(sender.location.apply {
                     y += 0.5
                     yaw = 0f
                     pitch = 0f
                }, EntityType.ITEM_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM) {
                    (it as ItemDisplay).itemStack = ItemStack(Material.WHITE_CONCRETE, 1)
                    it.isGlowing = true
                    it.addScoreboardTag("test-display")
                    it.addScoreboardTag("loqinttemp")
                } as ItemDisplay
            }
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

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>?): MutableList<String>? {
        if (args == null || args.isEmpty() || args.size <= 1) return mutableListOf("on", "off")
        return mutableListOf()
    }

}