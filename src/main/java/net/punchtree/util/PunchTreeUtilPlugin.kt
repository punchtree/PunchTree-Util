package net.punchtree.util

import net.punchtree.util.color.ColoredScoreboardTeams
import net.punchtree.util.commands.CustomModelDataCommand
import net.punchtree.util.commands.UtiliKillCommand
import net.punchtree.util.debugvar.DebugVarCommand
import net.punchtree.util.interaction.HelloWorldCallback
import net.punchtree.util.interaction.InteractionCallbackListener
import net.punchtree.util.interaction.InteractionCallbackManager
import net.punchtree.util.interaction.pizza.DoughBin
import net.punchtree.util.playingcards.*
import net.punchtree.util.playingcards.pokerchips.PokerChipsListener
import net.punchtree.util.sounds.soundtest.SoundMenu
import net.punchtree.util.sounds.soundtest.SoundTestCommand
import net.punchtree.util.tools.MovePacketListener
import net.punchtree.util.tools.interactionplacement.InteractionPlacementTool
import net.punchtree.util.tools.interactionplacement.InteractionPlacementToolCommand
import net.punchtree.util.tools.interactionplacement.InteractionPlacementToolListener
import net.punchtree.util.tools.interactiontag.InteractionTagTool
import net.punchtree.util.tools.interactiontag.InteractionTagToolCommand
import net.punchtree.util.tools.interactiontag.InteractionTagToolListener
import net.punchtree.util.tools.placement.PlacementTool
import net.punchtree.util.tools.placement.PlacementToolCommand
import net.punchtree.util.tools.placement.PlacementToolListener
import net.punchtree.util.visualization.VisualizationTestingCommand
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
		getCommand("interactionplacementtool")!!.setExecutor(InteractionPlacementToolCommand)
        getCommand("interactiontagtool")!!.setExecutor(InteractionTagToolCommand)
        getCommand("utilikill")!!.setExecutor(UtiliKillCommand)
        getCommand("visualize")!!.setExecutor(VisualizationTestingCommand)

        Bukkit.getPluginManager().registerEvents(PlacementToolListener(), this)
        Bukkit.getPluginManager().registerEvents(InteractionPlacementToolListener(), this)
        Bukkit.getPluginManager().registerEvents(InteractionTagToolListener(), this)
        Bukkit.getPluginManager().registerEvents(InteractionCallbackListener(), this)

        InteractionCallbackManager.registerCallback("hello_world", HelloWorldCallback)

        MovePacketListener.enable()
        Bukkit.getPluginManager().registerEvents(SoundMenu(), this)
        initializePlayingCards()
        initializePokerChips()

        initializePizzaTest()
    }

    private fun initializePizzaTest() {
        InteractionCallbackManager.registerCallback("pizza_dough_bin", DoughBin)
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
        InteractionPlacementTool.onDisable()
        InteractionTagTool.onDisable()
        MovePacketListener.disable()
    }

    companion object {
        @JvmStatic
		val instance: Plugin
            get() = getPlugin(PunchTreeUtilPlugin::class.java)
        const val NAMESPACE = "punchtree"
    }
}
