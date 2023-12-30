package net.punchtree.util.tools.placement

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import net.punchtree.util.PunchTreeUtilPlugin
import org.bukkit.scheduler.BukkitRunnable

object MovePacketListener {

    private val inputPacketAdapter = InputPacketAdapter()

    fun enable() {
        ProtocolLibrary.getProtocolManager().addPacketListener(inputPacketAdapter)
    }

    fun disable() {
        ProtocolLibrary.getProtocolManager().removePacketListener(inputPacketAdapter)
    }

    class InputPacketAdapter : PacketAdapter(PunchTreeUtilPlugin.getInstance(), PacketType.Play.Client.POSITION, PacketType.Play.Client.POSITION_LOOK, PacketType.Play.Client.LOOK) {
        override fun onPacketReceiving(event: PacketEvent) {
            object : BukkitRunnable() {
                override fun run() {
                    PlacementTool.onMove(event.player)
                }
            }.runTask(PunchTreeUtilPlugin.getInstance())
        }
    }
}