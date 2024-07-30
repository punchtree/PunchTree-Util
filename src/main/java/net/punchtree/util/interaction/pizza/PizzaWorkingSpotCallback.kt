package net.punchtree.util.interaction.pizza

import net.punchtree.util.PunchTreeUtilPlugin
import net.punchtree.util.interaction.InteractionCallback
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

object PizzaWorkingSpotCallback : InteractionCallback {

    override fun onRightClick(interaction: Interaction, player: Player): Boolean {
        val itemInHand = player.inventory.itemInMainHand
        if (isPaddle(itemInHand)) {
            if (canPlacePaddle(interaction)) {
                placePaddle(interaction, player)
            } else {
                doFailureAlreadyInUse(player)
            }
            return true
        }

        if (isDough(itemInHand)) {
            if (canPlaceDough(interaction)) {
                placeDough(interaction, player)
            } else {
                doFailureAlreadyInUse(player)
            }
            return true
        }

        return false
    }

    private fun isPaddle(itemInHand: ItemStack): Boolean {
        return itemInHand.isSimilar(PizzaItems.PIZZA_PADDLE_ITEM)
    }

    private fun canPlacePaddle(interaction: Interaction): Boolean {
        return !isOccupied(interaction)
    }

    private fun placePaddle(interaction: Interaction, player: Player) {
        interaction.persistentDataContainer.set(ITEM_KEY, PizzaWorkingSpotStateDataType, PizzaWorkingSpotState(
            paddle = true,
            dough = false,
            sauce = false,
            cheese = false,
            toppings = emptyList()
        ))
        player.inventory.setItemInMainHand(ItemStack(Material.AIR))
        // TODO spawn paddle entity, store UUID in PizzaWorkingSpotState
    }

    private fun isDough(itemInHand: ItemStack): Boolean {
        return itemInHand.isSimilar(PizzaItems.PIZZA_DOUGH_ITEM)
    }

    private fun canPlaceDough(interaction: Interaction): Boolean {
        return getPizzaWorkingSpotState(interaction)?.let {
            return it.paddle && !it.dough
        } ?: false
    }

    private fun placeDough(interaction: Interaction, player: Player) {
        interaction.persistentDataContainer.set(ITEM_KEY, PizzaWorkingSpotStateDataType, PizzaWorkingSpotState(
            paddle = true,
            dough = true,
            sauce = false,
            cheese = false,
            toppings = emptyList()
        ))
        player.inventory.setItemInMainHand(ItemStack(Material.AIR))
        // We take for granted that this is a pizza working spot interaction
        val paddleDisplay = interaction.world.spawnEntity(interaction.location, EntityType.ITEM_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM) {
            it as ItemDisplay
            it.itemStack = PizzaItems.PIZZA_PADDLE_ITEM
        } as ItemDisplay

    }

    private fun isOccupied(interaction: Interaction): Boolean {
        return getPizzaWorkingSpotState(interaction)?.isEmpty()?.not() ?: false
    }

    private fun getPizzaWorkingSpotState(interaction: Interaction) =
        interaction.persistentDataContainer.get(ITEM_KEY, PizzaWorkingSpotStateDataType)

    private fun doFailureAlreadyInUse(player: Player) {
        player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
    }

    private val ITEM_KEY = NamespacedKey(PunchTreeUtilPlugin.NAMESPACE, "item")

    data class PizzaWorkingSpotState(
        val paddle: Boolean,
        val dough: Boolean,
        val sauce: Boolean,
        val cheese: Boolean,
        val toppings: List<String>
    ) {
        fun isEmpty(): Boolean {
            return !paddle && !dough && !sauce && !cheese && toppings.isEmpty()
        }
    }

    object PizzaWorkingSpotStateDataType : PersistentDataType<String, PizzaWorkingSpotState> {
        override fun getPrimitiveType(): Class<String> {
            return String::class.java
        }

        override fun getComplexType(): Class<PizzaWorkingSpotState> {
            return PizzaWorkingSpotState::class.java
        }

        override fun toPrimitive(complex: PizzaWorkingSpotState, context: PersistentDataAdapterContext): String {
            val value = "${complex.paddle},${complex.dough},${complex.sauce},${complex.cheese},${complex.toppings.joinToString(",")}"
            return value
        }

        override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): PizzaWorkingSpotState {
            val split = primitive.split(",")
            return PizzaWorkingSpotState(split[0].toBoolean(), split[1].toBoolean(), split[2].toBoolean(), split[3].toBoolean(), split.subList(4, split.size))
        }
    }

}