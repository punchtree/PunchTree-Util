package net.punchtree.util.visualization

import net.punchtree.util.PunchTreeUtilPlugin
import net.punchtree.util.tools.placement.PlacementToolPlayer
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.*

class VisualizationTesting {
    companion object {
        fun doLine(player: Player) {
            val forward = player.eyeLocation.direction.normalize()
            val right = Vector(0, 1, 0).crossProduct(forward).normalize() // modifies lhs
            val up = right.getCrossProduct(forward).normalize() // "get" method doesn't modify lhs
            val point1 = player.eyeLocation.clone()
                .add(forward.clone().multiply(4))
                .add(right.clone().multiply(-4))
                .add(up.clone().multiply(-1))

            val point2 = player.eyeLocation.clone()
                .add(forward.clone().multiply(6))
                .add(right.clone().multiply(4))
                .add(up.clone().multiply(1))

            Visualization.drawLine(player, point1, point2)
        }

        fun doTriangle(player: Player) {
            val forward = player.eyeLocation.direction.normalize()
            val right = Vector(0, 1, 0).crossProduct(forward).normalize() // modifies lhs
            val up = right.getCrossProduct(forward).normalize() // "get" method doesn't modify lhs
            val point1 = player.eyeLocation.clone()
                .add(forward.clone().multiply(4))
                .add(right.clone().multiply(-2))
                .add(up.clone().multiply(-1))

            val point2 = player.eyeLocation.clone()
                .add(forward.clone().multiply(4))
                .add(right.clone().multiply(2))
                .add(up.clone().multiply(1))

            val point3 = player.eyeLocation.clone()
                .add(forward.clone().multiply(4))
                .add(right.clone().multiply(-2))
                .add(up.clone().multiply(1))

            Visualization.drawTriangle(player, point1, point2, point3)
        }

        private val controlPoints: Queue<Location> = LinkedList()

        fun doTrianglePath(player: Player) {
            controlPoints.add(player.location.clone())
            if (controlPoints.size < 3) {
                player.sendMessage("Added control point, need ${4 - controlPoints.size} more")
                return
            } else if (controlPoints.size > 9) {
                controlPoints.remove()
            }


            // Every set of 4 control points will be a segment
            for (i in 3 .. controlPoints.size) {
                Visualization.drawTriangle(player,
                    controlPoints.elementAt(i - 3),
                    controlPoints.elementAt(i - 2),
                    controlPoints.elementAt(i - 1))
            }

            // runnable for path drawing

//        PathDrawer.drawPath(path, 0.1)

        }

        fun modelTest(player: Player, model: ItemStack) {
            val display = player.world.spawnEntity(
                PlacementToolPlayer.snapToPixelGrid(player.location).also {
                    it.yaw = 0f
                    it.pitch = 0f
                },
                EntityType.ITEM_DISPLAY,
                CreatureSpawnEvent.SpawnReason.CUSTOM
            ) {
                it as ItemDisplay
                it.setItemStack(model)
                it.scoreboardTags.add("loqinttemp")
            } as ItemDisplay

            object : BukkitRunnable() {
                override fun run() {
                    display.remove()
                }
            }.runTaskLater(PunchTreeUtilPlugin.instance, 60)
        }
    }

}
