package net.punchtree.util

import net.punchtree.util.color.ColoredScoreboardTeams
import net.punchtree.util.commands.CustomModelDataCommand
import net.punchtree.util.debugvar.DebugVarCommand
import net.punchtree.util.playingcards.*
import net.punchtree.util.playingcards.pokerchips.PokerChipsListener
import net.punchtree.util.sounds.soundtest.SoundMenu
import net.punchtree.util.sounds.soundtest.SoundTestCommand
import net.punchtree.util.tools.placement.MovePacketListener.disable
import net.punchtree.util.tools.placement.MovePacketListener.enable
import net.punchtree.util.tools.placement.PlacementTool
import net.punchtree.util.tools.placement.PlacementToolCommand
import net.punchtree.util.tools.placement.PlacementToolListener
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class PunchTreeUtilPlugin : JavaPlugin() {
    override fun onEnable() {
        ColoredScoreboardTeams.initializeTeams()
        val debugVarCommand = DebugVarCommand()
        getCommand("debugvar")!!.setExecutor(debugVarCommand)
        getCommand("debugvar")!!.tabCompleter = debugVarCommand
        getCommand("cmd")!!.setExecutor(CustomModelDataCommand())
        getCommand("soundtest")!!.setExecutor(SoundTestCommand())
        getCommand("placementtool")!!.setExecutor(PlacementToolCommand)
        Bukkit.getPluginManager().registerEvents(PlacementToolListener(), this)

        enable()
        Bukkit.getPluginManager().registerEvents(SoundMenu(), this)
        initializePlayingCards()
        initializePokerChips()
    }

    private fun initializePokerChips() {
        Bukkit.getPluginManager().registerEvents(PokerChipsListener(), this)
    }

    private fun initializePlayingCards() {
        getCommand("playingcards")!!.setExecutor(PlayingCardCommands())
        Bukkit.getPluginManager().registerEvents(CardToCardListener(), this)
        Bukkit.getPluginManager().registerEvents(CardToGroundListener(), this)
        Bukkit.getPluginManager().registerEvents(CardBreakListener(), this)
        Bukkit.getPluginManager().registerEvents(CardInventoryListener(), this)
    }

    override fun onDisable() {
        PlacementTool.onDisable()
        disable()
    }

    companion object {
        @JvmStatic
		val instance: Plugin
            get() = getPlugin(PunchTreeUtilPlugin::class.java)
    }
}
