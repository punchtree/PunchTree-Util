package net.punchtree.util.tools.placement

import net.punchtree.util.color.PunchTreeColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent

const val REACH = 6.5
/** The max distance from the preview location that a display can be destroyed by left-clicking */
private const val MAX_DESTRUCTION_DISTANCE_FROM_PREVIEW_LOCATION = 0.5

private const val PLACEMENT_TOOL_PLACED_TAG = "placement-tool-placed"

private const val PLACEMENT_SOUND = "block.wood.place"

class PlacementToolPlayer(val player: Player) {

    private var selectedForDestructionModel: ItemDisplay? = null
    private var yaw = roundYawToNearestIncrementOfXDegrees(player.location.yaw, 45f)
    private var verticalOffsetPixels = 0
    private val previewDisplay: ItemDisplay =
        player.world.spawnEntity(snapToPixelGrid(player.location), EntityType.ITEM_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM) as ItemDisplay

    fun disable() {
        previewDisplay.remove()
        selectedForDestructionModel?.let {
            it.isGlowing = false
            PunchTreeColor.RED.glowingTeam.removeEntity(it)
        }
    }

    /** @param scrollAmount positive values are clockwise */
    fun rotate(scrollAmount: Int) {
        yaw += scrollAmount * 45
        yaw %= 360
        placePreviewAtRaycast()
    }

    fun placePreviewAtRaycast() {
        selectedForDestructionModel?.let {
            if (playerStoppedLookingAtSelectedForDestructionModel(it)) {
                it.isGlowing = false
                PunchTreeColor.RED.glowingTeam.removeEntity(it)
                selectedForDestructionModel = null
            }
        }
        previewDisplay.itemStack = player.inventory.itemInMainHand
        player.rayTraceBlocks(REACH)?.let {
            previewDisplay.teleport(snapToPixelGrid(it.hitPosition.toLocation(previewDisplay.world)).apply {
                yaw = this@PlacementToolPlayer.yaw
                y += 0.5 + verticalOffsetPixels / 16.0
                when(it.hitBlockFace) {
                    BlockFace.DOWN -> y -= 1.0
                    BlockFace.NORTH -> z -= 0.5
                    BlockFace.SOUTH -> z += 0.5
                    BlockFace.WEST -> x -= 0.5
                    BlockFace.EAST -> x += 0.5
                    else -> {}
            } })
        } ?: run {
            val reachMax = player.eyeLocation.add(player.location.direction.multiply(REACH))
            previewDisplay.teleport(snapToPixelGrid(reachMax).apply {
                yaw = this@PlacementToolPlayer.yaw
                y += 0.5 + verticalOffsetPixels / 16.0
            })
        }
    }

    fun place() {
        if (previewDisplay.itemStack?.type == Material.AIR) {
            return
        }
        player.world.spawnEntity(previewDisplay.location, EntityType.ITEM_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM) {
            val placedItem: ItemDisplay = it as ItemDisplay
            placedItem.itemStack = previewDisplay.itemStack
            placedItem.setRotation(yaw, 0f)
            placedItem.addScoreboardTag(PLACEMENT_TOOL_PLACED_TAG)
            player.playSound(player.location, PLACEMENT_SOUND, 1f, 1f)
        }
    }

    fun destroy() {
        val previewLocation = previewDisplay.location
        previewLocation.world.getNearbyEntities(
            previewLocation,
            MAX_DESTRUCTION_DISTANCE_FROM_PREVIEW_LOCATION,
            MAX_DESTRUCTION_DISTANCE_FROM_PREVIEW_LOCATION,
            MAX_DESTRUCTION_DISTANCE_FROM_PREVIEW_LOCATION
        ) {
            it is ItemDisplay && it.scoreboardTags.contains(PLACEMENT_TOOL_PLACED_TAG)
        }.minByOrNull {
            it.location.distanceSquared(previewLocation)
        }?.let {
            if (isSelectedForDestruction(it)) {
                it.remove()
            } else {
                setSelectedForDestructionModel(it)
            }
        }
    }

    private fun playerStoppedLookingAtSelectedForDestructionModel(it: ItemDisplay) =
        previewDisplay.location.distanceSquared(it.location) > MAX_DESTRUCTION_DISTANCE_FROM_PREVIEW_LOCATION * MAX_DESTRUCTION_DISTANCE_FROM_PREVIEW_LOCATION

    private fun isSelectedForDestruction(it: Entity): Boolean {
        return selectedForDestructionModel?.uniqueId == it.uniqueId
    }

    private fun setSelectedForDestructionModel(model: Entity) {
        selectedForDestructionModel = model as ItemDisplay
        model.isGlowing = true
        PunchTreeColor.RED.glowingTeam.addEntity(model)
    }

    fun adjustVerticalOffset(up: Boolean) {
        verticalOffsetPixels += if (up) 1 else -1
        placePreviewAtRaycast()
        player.sendActionBar("Vertical Offset: ${if (verticalOffsetPixels > 0 ) "+" else ""}${verticalOffsetPixels} px")
    }

    companion object {
        internal fun snapToPixelGrid(location: Location): Location {
            return Location(
                location.world,
                (location.x * 16).toInt() / 16.0,
                (location.y * 16).toInt() / 16.0,
                (location.z * 16).toInt() / 16.0,
                roundYawToNearestIncrementOfXDegrees(location.yaw, 45f),
                0f
            )
        }

        internal fun roundYawToNearestIncrementOfXDegrees(yaw: Float, degreeIncrement: Float): Float {
            var incrementIndex = (yaw / degreeIncrement).toInt()
            if (yaw < 0) incrementIndex -= 1
            val incrementBelow = incrementIndex * degreeIncrement
            val incrementAbove = incrementBelow + degreeIncrement
            return if (yaw - incrementBelow < incrementAbove - yaw) incrementBelow else incrementAbove
        }
    }
}