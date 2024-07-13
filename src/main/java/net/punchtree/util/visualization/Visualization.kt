package net.punchtree.util.visualization

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.punchtree.util.PunchTreeUtilPlugin
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.acos

class Visualization {
    companion object {
        val LINE_MODEL = ItemStack(Material.LEATHER_CHESTPLATE, 1).also {
            it.editMeta {itemMeta ->
                itemMeta.setCustomModelData(1001)
                itemMeta.displayName(Component.text("Line"))
            }
        }

        fun drawLine(audience: Audience, point1: Location, point2: Location) {
            val transformationMatrix = getTransformationMatrix(point1, point2)

            val display = point1.world.spawnEntity(
                point1.clone().also {
                    it.yaw = 0f
                    it.pitch = 0f
                },
                EntityType.ITEM_DISPLAY,
                CreatureSpawnEvent.SpawnReason.CUSTOM
            ) {
                it as ItemDisplay
                it.itemStack = LINE_MODEL
                it.transformation = transformationMatrix
                it.scoreboardTags.add("loqinttemp")
            } as ItemDisplay

            object : BukkitRunnable() {
                override fun run() {
                    display.remove()
                }
            }.runTaskLater(PunchTreeUtilPlugin.instance, 60)
        }

        private fun getTransformationMatrix(point1: Location, point2: Location): Transformation {
            val distance = point1.distance(point2).toFloat()

            // distance is the scaling factor since our model is unit length
            val modelVector = Vector3f(0f, 0f, -1f)
            val targetVector = Vector3f(
                (point2.x - point1.x).toFloat(),
                (point2.y - point1.y).toFloat(),
                (point2.z - point1.z).toFloat()
            )

            val rotationAxis = Vector3f()
            modelVector.cross(targetVector, rotationAxis)

            val rotationAngle = acos(modelVector.dot(targetVector) / distance)
            val rotation = Quaternionf().rotationAxis(rotationAngle, rotationAxis)

            // use rotation as left rotation because we want to rotate FIRST, then scale, to avoid shearing the model
            // (but both work for the line because we have one point at the origin)
            val bukkitTransformation = Transformation(Vector3f(), rotation, Vector3f(1f, 1f, distance), Quaternionf())
            return bukkitTransformation
        }
    }
}
