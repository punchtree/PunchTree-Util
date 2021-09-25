package net.punchtree.util;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.punchtree.util.color.ColoredScoreboardTeams;
import net.punchtree.util.debugvar.DebugVarCommand;

public class PunchTreeUtilPlugin extends JavaPlugin {

	public static Plugin getPlugin() {
		return getPlugin(PunchTreeUtilPlugin.class);
	}
	
	@Override
	public void onEnable() {
		ColoredScoreboardTeams.initializeTeams();
		
		DebugVarCommand debugVarCommand = new DebugVarCommand();
		getCommand("debugvar").setExecutor(debugVarCommand);
		getCommand("debugvar").setTabCompleter(debugVarCommand);
	}

	
	
}
