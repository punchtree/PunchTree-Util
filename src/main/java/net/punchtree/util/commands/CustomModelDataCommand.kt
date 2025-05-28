package net.punchtree.util.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class CustomModelDataCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) return true
        val player = sender

        if (args.size < 2) {
            player.sendMessage("/cmd <item> <number>")
            return true
        }

        val material = Material.matchMaterial(args[0])
        if (material == null) {
            player.sendMessage(Component.text("There is no material with that name!").color(NamedTextColor.RED))
            return true
        } else if (!material.isItem) {
            player.sendMessage(Component.text("That material cannot be an item!").color(NamedTextColor.RED))
            return true
        }

        val customModelDataNumber =
        try {
            args[1].toInt()
        } catch (nfe: NumberFormatException) {
            player.sendMessage(Component.text("'" + args[1] + "' is not a number!").color(NamedTextColor.RED))
            return true
        }

        val itemStack = ItemStack.of(material)
        itemStack.editMeta { im: ItemMeta -> im.setCustomModelData(customModelDataNumber) }
        player.inventory.addItem(itemStack)

        return true
    }
}
