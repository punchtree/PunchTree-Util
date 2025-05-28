package net.punchtree.util.persistentmetadata

import net.md_5.bungee.api.ChatColor
import net.punchtree.util.PunchTreeUtilPlugin
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.metadata.FixedMetadataValue
import java.sql.*

/**
 * Persistent Block Metadata API
 * @author Cxom
 */
object PersistentMetadata {
    var conn: Connection? = null

    // TODO swap this out for a chunk based persistent data container solution. Start by extracting the API as an interface
    //  with a DB implementation and then we can implement a chunk solution conforming to the same interface

    // TODO overridden methods to ensure value is valid
    fun setMetadata(block: Block, key: String, value: Any): Boolean {
        checkNotNull(conn)
        val stmt: PreparedStatement
        try {
            stmt = conn!!.prepareStatement(
                "REPLACE INTO blocks (world, x, y, z, metadata_key, metadata_value) "
                        + "VALUES (?, ?, ?, ?, ?, ?);"
            )
            stmt.setString(1, block.world.name)
            stmt.setInt(2, block.x)
            stmt.setInt(3, block.y)
            stmt.setInt(4, block.z)
            stmt.setString(5, key)
            stmt.setObject(6, value)
            stmt.executeUpdate()
        } catch (e: SQLException) {
            Bukkit.broadcastMessage(ChatColor.RED.toString() + "Error storing metadata_key `" + key + "` and value `" + value.toString() + "` to database!")
            e.printStackTrace()
            return false
        }

        block.setMetadata(key, FixedMetadataValue(PunchTreeUtilPlugin.instance, value))
        return true
    }

    fun getAllMetadata(block: Block): Map<String, Any> {
        checkNotNull(conn)
        val metadataMap: MutableMap<String, Any> = HashMap()
        try {
            val st = conn!!.prepareStatement("SELECT * FROM blocks WHERE world = ? AND x = ? AND y = ? AND z = ?")
            st.setString(1, block.world.name)
            st.setInt(2, block.x)
            st.setInt(3, block.y)
            st.setInt(4, block.z)
            val rs = st.executeQuery()
            while (rs.next()) {
                metadataMap.put(rs.getString("metadata_key"), rs.getObject("metadata_value"))
            }
        } catch (e: SQLException) {
            Bukkit.broadcastMessage(
                ChatColor.RED.toString() + "Error retrieving metadata values for block `" + blockString(
                    block
                ) + "` from database!"
            )
            e.printStackTrace()
        }
        return metadataMap
    }

    fun getMetadataInRadius(center: Block, radius: Int): Map<Block, MutableMap<String, Any>> {
        val world = center.world
        val metadataMap: MutableMap<Block, MutableMap<String, Any>> = HashMap()
        try {
            val st =
                conn!!.prepareStatement("SELECT * FROM blocks WHERE world = ? AND x >= ? AND x <= ? AND y >= ? AND y <= ? AND z >= ? AND z <= ?")
            st.setString(1, center.world.name)
            st.setInt(2, center.x - radius)
            st.setInt(3, center.x + radius)
            st.setInt(4, center.y - radius)
            st.setInt(5, center.y + radius)
            st.setInt(6, center.z - radius)
            st.setInt(7, center.z + radius)
            val rs = st.executeQuery()
            while (rs.next()) {
                val block = Location(
                    world,
                    rs.getInt("x").toDouble(),
                    rs.getInt("y").toDouble(),
                    rs.getInt("z").toDouble()
                ).block
                var blockMap = metadataMap[block]
                if (blockMap == null) {
                    blockMap = HashMap()
                    metadataMap.put(block, blockMap)
                }
                blockMap.put(rs.getString("metadata_key"), rs.getObject("metadata_value"))
            }
        } catch (e: SQLException) {
            Bukkit.broadcastMessage(
                ChatColor.RED.toString() + "Error retrieving metadata values in radius of " + radius + " around block `" + blockString(
                    center
                ) + "` from database!"
            )
            e.printStackTrace()
        }
        return metadataMap
    }

    fun getMetadata(block: Block, key: String): Any? {
        checkNotNull(conn)
        try {
            val st =
                conn!!.prepareStatement("SELECT * FROM blocks WHERE world = ? AND x = ? AND y = ? AND z = ? AND metadata_key = ?")
            st.setString(1, block.world.name)
            st.setInt(2, block.x)
            st.setInt(3, block.y)
            st.setInt(4, block.z)
            st.setString(5, key)
            val rs = st.executeQuery()
            if (!rs.next()) {
                return null
            }
            return rs.getObject("metadata_value")
        } catch (e: SQLException) {
            Bukkit.broadcastMessage(ChatColor.RED.toString() + "Error retrieving metadata value for metadata_key `" + key + "` from database!")
            e.printStackTrace()
            return null
        }
    }

    fun removeMetadata(block: Block, key: String): Boolean {
        checkNotNull(conn)
        val stmt: PreparedStatement
        try {
            stmt = conn!!.prepareStatement(
                "DELETE FROM blocks WHERE world = ? AND x = ? AND y = ? AND z = ? AND metadata_key = ?"
            )
            stmt.setString(1, block.world.name)
            stmt.setInt(2, block.x)
            stmt.setInt(3, block.y)
            stmt.setInt(4, block.z)
            stmt.setString(5, key)
            stmt.executeUpdate()
        } catch (e: SQLException) {
            Bukkit.broadcastMessage(ChatColor.RED.toString() + "Error deleting metadata_key `" + key + "` from database!")
            e.printStackTrace()
            return false
        }

        block.removeMetadata(key, PunchTreeUtilPlugin.instance)
        return true
    }

    private fun blockString(block: Block): String {
        return String.format("%s:%d,%d,%d", block.world.name, block.x, block.y, block.z)
    }


    private fun onEnable() {
        establishConnectionAndRestoreMetadata()
    }
    
    private fun establishConnectionAndRestoreMetadata() {
        PunchTreeUtilPlugin.instance.dataFolder.mkdir()

        try {
            conn = DriverManager.getConnection("jdbc:sqlite:plugins/PersistentMetadata/persistentmetadata.db")
            conn?.createStatement()?.executeUpdate(
                "CREATE TABLE IF NOT EXISTS blocks ("
                        + "rowid INTEGER PRIMARY KEY,"
                        + "world TEXT,"
                        + "x INTEGER,"
                        + "y INTEGER,"
                        + "z INTEGER,"
                        + "metadata_key TEXT,"
                        + "metadata_value)" /* VARIANT */
            )
            restoreMetadata()
        } catch (e: SQLException) {
            println("Could not initialize database! " + e.message)
            e.printStackTrace()
        }
    }

    // TODO thread this for scaling
    private fun restoreMetadata() {
        try {
            // We're going through the whole table
            // USE A CURSOR!
            val st: Statement = checkNotNull(conn).createStatement(
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY
            )
            st.fetchSize = 100
            val rs = st.executeQuery("SELECT * FROM blocks")
            while (rs.next()) {
                val world = Bukkit.getWorld(rs.getString("world"))
                val block = Location(
                    world,
                    rs.getInt("x").toDouble(),
                    rs.getInt("y").toDouble(),
                    rs.getInt("z").toDouble()
                ).block
                block.setMetadata(rs.getString("metadata_key"), FixedMetadataValue(PunchTreeUtilPlugin.instance, rs.getObject("metadata_value")))
            }
        } catch (e: SQLException) {
            Bukkit.broadcastMessage(ChatColor.DARK_RED.toString() + "ERROR: " + ChatColor.RED + "Error while restoring block metadata - aborting. A stacktrace has been printed to console.")
            e.printStackTrace()
        }
    }
}
