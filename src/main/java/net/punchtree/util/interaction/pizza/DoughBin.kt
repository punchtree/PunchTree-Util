package net.punchtree.util.interaction.pizza

import net.punchtree.util.interaction.InteractionCallback
import net.punchtree.util.interaction.pizza.PizzaItems.PIZZA_DOUGH_ITEM
import org.bukkit.Material
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player

object DoughBin : InteractionCallback {

    override fun onRightClick(interaction: Interaction, player: Player): Boolean {
        if (player.inventory.itemInMainHand.type == Material.AIR) {
            player.inventory.setItemInMainHand(PIZZA_DOUGH_ITEM)
            return true
        }
        return false
    }

}