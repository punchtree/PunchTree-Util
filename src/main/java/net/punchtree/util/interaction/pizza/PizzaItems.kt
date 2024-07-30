package net.punchtree.util.interaction.pizza

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

data object PizzaItems {

    internal val PIZZA_PADDLE_ITEM = ItemStack(Material.NAUTILUS_SHELL).apply {
        itemMeta = itemMeta.apply {
            displayName(Component.text("Pizza Paddle"))
            setCustomModelData(15002)
        }
    }

    internal val PIZZA_DOUGH_ITEM = ItemStack(Material.NAUTILUS_SHELL).apply {
        itemMeta = itemMeta.apply {
            displayName(Component.text("Pizza Dough"))
            setCustomModelData(15004)
        }
    }

}