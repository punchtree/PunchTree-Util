package net.punchtree.util;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.punchtree.util.color.ColoredScoreboardTeams;

public class PunchTreeUtilPlugin extends JavaPlugin {

	public static Plugin getPlugin() {
		return getPlugin(PunchTreeUtilPlugin.class);
	}
	
	@Override
	public void onEnable() {
		ColoredScoreboardTeams.initializeTeams();
	}

	
	
}
