package net.punchtree.util.tools.interactionplacement

import net.kyori.adventure.text.Component
import net.minecraft.util.Mth.clamp
import net.punchtree.util.color.PunchTreeColor
import net.punchtree.util.tools.placement.PlacementToolPlayer.Companion.snapToPixelGrid
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Vector3f
import kotlin.math.abs
import kotlin.math.max

const val DEFAULT_REACH = 2.5
const val MIN_REACH = 0.5
const val MAX_REACH = 7.5

private const val PIXELS_TO_BLOCKS = 0.0625f

private const val INTERACTION_TOOL_PLACED_TAG = "interaction-tool-placed"

class InteractionPlacementToolPlayer(val player: Player) {

    private var horizontalScalePixels = 16
    private val horizontalScale get() = horizontalScalePixels * PIXELS_TO_BLOCKS
    private var verticalScalePixels = 16
    private val verticalScale get() = verticalScalePixels * PIXELS_TO_BLOCKS
    private var tagsToAdd = mutableSetOf<String>()
    private var distance = DEFAULT_REACH
    private val previewDisplay: ItemDisplay =
        player.world.spawnEntity(snapToPixelGrid(player.location), EntityType.ITEM_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM) {
            it as ItemDisplay
            it.setItemStack(ItemStack(Material.RED_STAINED_GLASS, 1))
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
//            player.inventory.setItem(EquipmentSlot.HAND, ItemStack(Material.RED_STAINED_GLASS, 1))
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
                if (verticalScalePixels % 2 != 0) {
                    y -= 0.03125
                }
                if (horizontalScalePixels % 2 != 0) {
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
                y -= 0.5 * verticalScalePixels * PIXELS_TO_BLOCKS
                if (verticalScalePixels % 2 != 0) {
                    y -= 0.03125
                }
                if (horizontalScalePixels % 2 != 0) {
                    x -= 0.03125
                    z -= 0.03125
                }
                yaw = 0f
                pitch = 0f
            })
            previewInteraction.interactionWidth = horizontalScale
            previewInteraction.interactionHeight = verticalScale
        }

        player.sendActionBar(Component.text("width ${horizontalScalePixels}px | height ${verticalScalePixels}px | tags to add: [${tagsToAdd.joinToString(",")}]"))
    }

    fun place() {
        player.sendMessage("Placed interaction with width ")
        player.world.spawnEntity(previewInteraction.location, EntityType.INTERACTION, CreatureSpawnEvent.SpawnReason.CUSTOM) {
            it as Interaction
            it.interactionWidth = horizontalScale
            it.interactionHeight = verticalScale
            it.scoreboardTags.add(INTERACTION_TOOL_PLACED_TAG)
            tagsToAdd.forEach { tag -> it.scoreboardTags.add(tag) }
        }
    }

    fun adjustHeight(scrollAmount: Int) {
        verticalScalePixels = max(1, verticalScalePixels + scrollAmount)
        placePreviewAtRaycast()
    }

    fun adjustDistance(scrollAmount: Int) {
        distance = clamp(distance + scrollAmount * PIXELS_TO_BLOCKS, MIN_REACH, MAX_REACH)
        placePreviewAtRaycast()
    }

    fun decreaseHorizontalSize() {
        horizontalScalePixels = max(1, horizontalScalePixels - 1)
        placePreviewAtRaycast()
    }

    fun increaseHorizontalSize() {
        horizontalScalePixels += 1
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
                it.remove()
                PunchTreeColor.RED.glowingTeam.removeEntity(selectedForDestructionPreview!!)
                selectedForDestructionPreview!!.remove()
                selectedForDestructionPreview = null
                selectedForDestructionInteraction = null
            } else {
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
            it.setItemStack(ItemStack(Material.RED_STAINED_GLASS, 1))
            it.transformation = Transformation(
                previewDisplay.transformation.translation,
                previewDisplay.transformation.leftRotation,
                Vector3f(interaction.interactionWidth, interaction.interactionHeight, interaction.interactionWidth),
                previewDisplay.transformation.rightRotation
            )
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

    fun addTagToAdd(tag: String) {
        tagsToAdd.add(tag)
    }

    fun removeTagToAdd(tag: String) {
        tagsToAdd.remove(tag)
    }

    fun clearTagsToAdd() {
        tagsToAdd.clear()
    }

}