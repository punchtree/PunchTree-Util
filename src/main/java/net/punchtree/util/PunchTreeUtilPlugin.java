package net.punchtree.util;

import net.punchtree.util.commands.CustomModelDataCommand;
import net.punchtree.util.playingcards.*;
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

		initializePlayingCards();
	}

	private void initializePlayingCards() {
		getCommand("playingcards").setExecutor(new PlayingCardCommands());
		Bukkit.getPluginManager().registerEvents(new CardToCardListener(), this);
		Bukkit.getPluginManager().registerEvents(new CardToGroundListener(), this);
		Bukkit.getPluginManager().registerEvents(new CardBreakListener(), this);
		Bukkit.getPluginManager().registerEvents(new CardInventoryListener(), this);
	}


}
