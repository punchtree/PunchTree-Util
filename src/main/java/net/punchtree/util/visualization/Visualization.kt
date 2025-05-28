package net.punchtree.util.visualization

import com.destroystokyo.paper.ParticleBuilder
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.punchtree.util.PunchTreeUtilPlugin
import net.punchtree.util.color.PunchTreeColor
import net.punchtree.util.particle.ParticleShapes
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.abs
import kotlin.math.acos

class Visualization {
    companion object {
        val LINE_MODEL = ItemStack(Material.LEATHER_CHESTPLATE, 1).also {
            it.editMeta {itemMeta ->
                itemMeta.setCustomModelData(1001)
                itemMeta.displayName(Component.text("Line"))
            }
        }

        val TRIANGLE_MODEL = ItemStack(Material.LEATHER_CHESTPLATE, 1).also {
            it.editMeta {itemMeta ->
                itemMeta.setCustomModelData(1002)
                itemMeta.displayName(Component.text("Triangle"))
            }
        }

        val CUBOID_MODEL = ItemStack(Material.LEATHER_CHESTPLATE).also {
            it.editMeta { itemMeta ->
                itemMeta.itemModel = NamespacedKey("punchtree", "recolorable")
                itemMeta.setCustomModelData(300)
                itemMeta.displayName(Component.text("Cuboid"))
            }
        }

        // TODO FIXME this fails when axis-aligned on the Z axis - it's degenerate or something
        //  I think this bug probably affects cuboid too
        fun drawLine(audience: Audience, point1: Location, point2: Location) {
            val transformationMatrix = getLineTransformationMatrix(point1, point2)

            val display = point1.world.spawnEntity(
                point1.clone().also {
                    it.yaw = 0f
                    it.pitch = 0f
                },
                EntityType.ITEM_DISPLAY,
                CreatureSpawnEvent.SpawnReason.CUSTOM
            ) {
                it as ItemDisplay
                it.setItemStack(LINE_MODEL)
                it.transformation = transformationMatrix
                it.scoreboardTags.add("loqinttemp")
                it.isGlowing = true
                PunchTreeColor.RED.glowingTeam.addEntity(it)
            } as ItemDisplay

            object : BukkitRunnable() {
                override fun run() {
                    display.remove()
                }
            }.runTaskLater(PunchTreeUtilPlugin.instance, 60)
        }

        private fun getLineTransformationMatrix(point1: Location, point2: Location): Transformation {
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

        fun drawTriangle(player: Player, point1: Location, point2: Location, point3: Location) {
            val transformationMatrix = getTriangleTransformation(point1, point2, point3)

            val display = point1.world.spawnEntity(
                point1.clone().also {
                    it.yaw = 0f
                    it.pitch = 0f
                },
                EntityType.ITEM_DISPLAY,
                CreatureSpawnEvent.SpawnReason.CUSTOM
            ) {
                it as ItemDisplay
                it.setItemStack(TRIANGLE_MODEL)
                it.setTransformationMatrix(transformationMatrix)
                it.glowColorOverride = PunchTreeColor(255, 200, 200).bukkitColor
                it.scoreboardTags.add("loqinttemp")
                it.isGlowing = true
            } as ItemDisplay

            object : BukkitRunnable() {
                override fun run() {
                    display.remove()
                }
            }.runTaskLater(PunchTreeUtilPlugin.instance, 60)

            // Backup particle test
            object : BukkitRunnable() {
                var counter = 0
                override fun run() {
                    ParticleShapes.setParticleBuilder(ParticleBuilder(Particle.WAX_OFF))
                    ParticleShapes.spawnParticleLine(point1, point2, (point1.distance(point2) * 6).toInt())
                    ParticleShapes.spawnParticleLine(point2, point3, (point2.distance(point3) * 6).toInt())
                    ParticleShapes.spawnParticleLine(point3, point1, (point3.distance(point1) * 6).toInt())
                    if (++counter > 60) {
                        cancel()
                    }
                }
            }.runTaskTimer(PunchTreeUtilPlugin.instance, 0, 1)
        }

        private fun getTriangleTransformation(point1: Location, point2: Location, point3: Location): Matrix4f {
            val targetTriangle = listOf(
                Vector3f(point1.x.toFloat(), point1.y.toFloat(), point1.z.toFloat()),
                Vector3f(point2.x.toFloat(), point2.y.toFloat(), point2.z.toFloat()),
                Vector3f(point3.x.toFloat(), point3.y.toFloat(), point3.z.toFloat())
            )

            val unitRightTriangle = listOf(
                Vector3f(0f, 0f, 0f), // Necessary to prevent the determinant from being zero, make the matrix invertible
                Vector3f(0f, 0f, -1f),
                Vector3f(0f, 1f, 0f)
            )

            val translatedTargetTriangle = targetTriangle.map { it.sub(targetTriangle[0], Vector3f()) }

            // Triangles are 2D! Inorder to prevent degeneration in the generated transformation in 3D,
            // use the normals of the triangles as the first vector in the transformation matrix calculation
            // this way we're not modifying the normal axis of the triangles, just rotating it

            val unitNormal = unitRightTriangle[1].cross(unitRightTriangle[2], Vector3f()).normalize()
            val targetNormal = translatedTargetTriangle[1].cross(translatedTargetTriangle[2], Vector3f()).normalize()

            val mUnitTri = Matrix3f(unitNormal, unitRightTriangle[1], unitRightTriangle[2])
            val mTargetTri = Matrix3f(targetNormal, translatedTargetTriangle[1], translatedTargetTriangle[2])

            val inverse = Matrix3f(mUnitTri).invert()
            val transformationMatrix = Matrix4f().set(Matrix3f(mTargetTri).mul(inverse))


            return transformationMatrix
        }

        fun drawCuboid(player: Player, a: Location, b: Location) {
            require(a.world == b.world) { "The locations of the cuboid corners have to be in the same world!" }
            // TODO take any corners and calculate min and max ourselves
            val centerLocation = Location(a.world, (a.x + b.x) / 2.0, (a.y + b.y) / 2.0, (a.z + b.z) / 2.0)
            val halfExtents = Vector(
                abs(b.x - a.x) / 2.0,
                abs(b.y - a.y) / 2.0,
                abs(b.z - a.z) / 2.0
            )
            drawCuboid(player, centerLocation, halfExtents)
        }

        fun drawCuboid(player: Player, center: Location, halfExtents: Vector) {
            val scale = Vector3f(
                (halfExtents.x * 2).toFloat(),
                (halfExtents.y * 2).toFloat(),
                (halfExtents.z * 2).toFloat()
            )
            val transformationMatrix = Transformation(Vector3f(), Quaternionf(), scale, Quaternionf())

            val display = center.world.spawnEntity(
                center.clone().also {
                    it.yaw = 0f
                    it.pitch = 0f
                },
                EntityType.ITEM_DISPLAY,
                CreatureSpawnEvent.SpawnReason.CUSTOM
            ) {
                it as ItemDisplay
                it.setItemStack(CUBOID_MODEL)
                it.transformation = transformationMatrix
                it.glowColorOverride = PunchTreeColor(255, 200, 200).bukkitColor
                it.scoreboardTags.add("loqinttemp")
                it.isGlowing = true
            } as ItemDisplay

            object : BukkitRunnable() {
                override fun run() {
                    display.remove()
                }
            }.runTaskLater(PunchTreeUtilPlugin.instance, 60)
            // TODO track temporary display entities and delete on server shutdown as a productionizing step
            // TODO this API really needs a way to customize display time
        }
    }
}
