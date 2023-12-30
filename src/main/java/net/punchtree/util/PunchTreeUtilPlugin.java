package net.punchtree.util;

import net.punchtree.util.commands.CustomModelDataCommand;
import net.punchtree.util.playingcards.*;
import net.punchtree.util.playingcards.pokerchips.PokerChipsListener;
import net.punchtree.util.sounds.soundtest.SoundMenu;
import net.punchtree.util.sounds.soundtest.SoundTestCommand;
import net.punchtree.util.tools.placement.MovePacketListener;
import net.punchtree.util.tools.placement.PlacementTool;
import net.punchtree.util.tools.placement.PlacementToolCommand;
import net.punchtree.util.tools.placement.PlacementToolListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.punchtree.util.color.ColoredScoreboardTeams;
import net.punchtree.util.debugvar.DebugVarCommand;

public class PunchTreeUtilPlugin extends JavaPlugin {

	public static Plugin getInstance() {
		return getPlugin(PunchTreeUtilPlugin.class);
	}

	@Override
	public void onEnable() {
		ColoredScoreboardTeams.initializeTeams();
		
		DebugVarCommand debugVarCommand = new DebugVarCommand();
		getCommand("debugvar").setExecutor(debugVarCommand);
		getCommand("debugvar").setTabCompleter(debugVarCommand);
		getCommand("cmd").setExecutor(new CustomModelDataCommand());
		getCommand("soundtest").setExecutor(new SoundTestCommand());


		getCommand("placementtool").setExecutor(PlacementToolCommand.INSTANCE);
		Bukkit.getPluginManager().registerEvents(new PlacementToolListener(), this);
		MovePacketListener.INSTANCE.enable();

		Bukkit.getPluginManager().registerEvents(new SoundMenu(), this);

		initializePlayingCards();

		initializePokerChips();
	}

	private void initializePokerChips() {
		Bukkit.getPluginManager().registerEvents(new PokerChipsListener(), this);
	}

	private void initializePlayingCards() {
		getCommand("playingcards").setExecutor(new PlayingCardCommands());
		Bukkit.getPluginManager().registerEvents(new CardToCardListener(), this);
		Bukkit.getPluginManager().registerEvents(new CardToGroundListener(), this);
		Bukkit.getPluginManager().registerEvents(new CardBreakListener(), this);
		Bukkit.getPluginManager().registerEvents(new CardInventoryListener(), this);
	}

	@Override
	public void onDisable() {
		PlacementTool.INSTANCE.onDisable();
		MovePacketListener.INSTANCE.disable();
	}

}
