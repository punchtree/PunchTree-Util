package net.punchtree.util.tools.interactionplacement

import net.minecraft.util.Mth.clamp
import net.punchtree.util.color.PunchTreeColor
import net.punchtree.util.tools.placement.PlacementToolPlayer.Companion.snapToPixelGrid
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Vector3f
import kotlin.math.abs
import kotlin.math.max

const val DEFAULT_REACH = 2.5
const val MIN_REACH = 0.5
const val MAX_REACH = 7.5

private const val ONE_PIXEL = 0.0625f

private const val INTERACTION_TOOL_PLACED_TAG = "interaction-tool-placed"

class InteractionPlacementToolPlayer(val player: Player) {

    private var distance = DEFAULT_REACH
    private var horizontalScale = 1f
    private var verticalScale = 1f
    private val previewDisplay: ItemDisplay =
        player.world.spawnEntity(snapToPixelGrid(player.location), EntityType.ITEM_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM) {
            it as ItemDisplay
            it.itemStack = ItemStack(Material.RED_STAINED_GLASS, 1)
        } as ItemDisplay
    private val previewInteraction: Interaction =
        player.world.spawnEntity(snapToPixelGrid(player.location), EntityType.INTERACTION, CreatureSpawnEvent.SpawnReason.CUSTOM) {
            it as Interaction
            it.interactionWidth = 1f
            it.interactionHeight = 1f
        } as Interaction
    private var selectedForDestructionInteraction: Interaction? = null
    private var selectedForDestructionPreview: ItemDisplay? = null

    init {
        if (player.inventory.itemInMainHand.type == Material.AIR) {
            player.inventory.setItem(EquipmentSlot.HAND, ItemStack(Material.RED_STAINED_GLASS, 1))
        }
    }

    fun disable() {
        previewDisplay.remove()
        previewInteraction.remove()
        selectedForDestructionPreview?.let {
            it.isGlowing = false
            PunchTreeColor.RED.glowingTeam.removeEntity(it)
        }
    }

    fun placePreviewAtRaycast() {
        selectedForDestructionInteraction?.let {
            if (playerStoppedLookingAtSelectedForDestructionInteraction(it)) {
                PunchTreeColor.RED.glowingTeam.removeEntity(selectedForDestructionPreview!!)
                selectedForDestructionPreview!!.remove()
                selectedForDestructionPreview = null
                selectedForDestructionInteraction = null
            }
        }

        player.eyeLocation.add(player.location.direction.multiply(distance)).let {
            previewDisplay.teleport(snapToPixelGrid(it).apply {
                if ((verticalScale / 0.0625) % 2 != 0.0) {
                    y -= 0.03125
                }
                if ((horizontalScale / 0.0625) % 2 != 0.0) {
                    x -= 0.03125
                    z -= 0.03125
                }
                yaw = 0f
                pitch = 0f
            })

            previewDisplay.transformation = Transformation(
                previewDisplay.transformation.translation,
                previewDisplay.transformation.leftRotation,
                Vector3f(horizontalScale, verticalScale, horizontalScale),
                previewDisplay.transformation.rightRotation
            )

            previewInteraction.teleport(snapToPixelGrid(it).apply {
                y -= 0.5 * verticalScale
                if ((verticalScale / 0.0625) % 2 != 0.0) {
                    y -= 0.03125
                }
                if ((horizontalScale / 0.0625) % 2 != 0.0) {
                    x -= 0.03125
                    z -= 0.03125
                }
                yaw = 0f
                pitch = 0f
            })
            previewInteraction.interactionWidth = horizontalScale
            previewInteraction.interactionHeight = verticalScale
        }
    }

    fun place() {
        player.sendMessage("Placed interaction with width $horizontalScale and height $verticalScale")
        player.world.spawnEntity(previewInteraction.location, EntityType.INTERACTION, CreatureSpawnEvent.SpawnReason.CUSTOM) {
            it as Interaction
            it.interactionWidth = horizontalScale
            it.interactionHeight = verticalScale
            it.scoreboardTags.add(INTERACTION_TOOL_PLACED_TAG)
        }
    }

    fun adjustHeight(scrollAmount: Int) {
        verticalScale = max(ONE_PIXEL, verticalScale + scrollAmount * ONE_PIXEL)
        placePreviewAtRaycast()
    }

    fun adjustDistance(scrollAmount: Int) {
        distance = clamp(distance + scrollAmount * ONE_PIXEL, MIN_REACH, MAX_REACH)
        placePreviewAtRaycast()
    }

    fun decreaseHorizontalSize() {
        horizontalScale = max(ONE_PIXEL, horizontalScale - ONE_PIXEL)
        placePreviewAtRaycast()
    }

    fun increaseHorizontalSize() {
        horizontalScale += ONE_PIXEL
        placePreviewAtRaycast()
    }

    fun destroy() {
        val previewLocation = previewDisplay.location
        previewLocation.world.getNearbyEntities(
            previewLocation,
            horizontalScale / 2.0,
            verticalScale / 2.0,
            horizontalScale / 2.0
        ) {
            it is Interaction && it.scoreboardTags.contains(INTERACTION_TOOL_PLACED_TAG)
        }.minByOrNull {
            it.location.distanceSquared(previewLocation)
        }?.let {
            // TODO this will not destroy the selected for destruction model EVEN IF IT IS STILL IN RANGE if another interaction
            //  is closer, which may be inconvenient behavior
            it as Interaction
            if (isSelectedForDestruction(it)) {
                player.sendActionBar("Deleting an interaction...")
                it.remove()
                PunchTreeColor.RED.glowingTeam.removeEntity(selectedForDestructionPreview!!)
                selectedForDestructionPreview!!.remove()
                selectedForDestructionPreview = null
                selectedForDestructionInteraction = null
            } else {
                player.sendActionBar("Marking an interaction for deletion...")
                setSelectedForDestructionInteraction(it)
            }
        }
    }

    private fun isSelectedForDestruction(interaction: Interaction): Boolean {
        return selectedForDestructionInteraction?.uniqueId == interaction.uniqueId
    }

    private fun setSelectedForDestructionInteraction(interaction: Interaction) {
        // TODO is removing from the red team before deleting necessary?
        selectedForDestructionPreview?.let {
            PunchTreeColor.RED.glowingTeam.removeEntity(it)
            it.remove()
        }
        selectedForDestructionPreview = player.world.spawnEntity(interaction.location.add(0.0, interaction.interactionHeight / 2.0, 0.0), EntityType.ITEM_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM) {
            it as ItemDisplay
            it.itemStack = ItemStack(Material.RED_STAINED_GLASS, 1)
            it.transformation.scale.x = interaction.interactionWidth
            it.transformation.scale.y = interaction.interactionHeight
            it.transformation.scale.z = interaction.interactionWidth
            it.isGlowing = true
            PunchTreeColor.RED.glowingTeam.addEntity(it)
        } as ItemDisplay
        selectedForDestructionInteraction = interaction
    }

    private fun playerStoppedLookingAtSelectedForDestructionInteraction(selectedForDestructionInteraction: Interaction): Boolean {
        return abs(selectedForDestructionInteraction.location.x - previewDisplay.location.x) > horizontalScale / 2.0 ||
                abs(selectedForDestructionInteraction.location.y - previewDisplay.location.y) > verticalScale / 2.0 ||
                abs(selectedForDestructionInteraction.location.z - previewDisplay.location.z) > horizontalScale / 2.0
    }

}