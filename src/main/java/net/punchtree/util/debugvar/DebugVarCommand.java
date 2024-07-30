package net.punchtree.util.debugvar;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DebugVarCommand implements CommandExecutor, TabCompleter {

	// Could refactor all the type abbreviation aliases into a helper
	// Could rename existingVar in the get subcommand
	// Could make list work without a type specified
	// Could make get work without a type specified
	// Could make set work without a type specified
	// Could make longtypes be printed in different colors
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	
		if (args.length == 0) {
			showUsage(sender);
			return true;
		}
		
		String subcommand = args[0].toLowerCase();

		// TODO we can genericize this by creating some sort of implementable interface

		switch (subcommand) {
		case "set":
            if (args.length < 3 || (!"loc".equalsIgnoreCase(args[1]) && !"location".equalsIgnoreCase(args[1]) && args.length < 4)) {
				showUsage(sender);
				return true;
			}
			String type = args[1].toLowerCase();
			String longType = null;
			String varKey = args[2].toLowerCase();
			String stringValue = args[3];
			Object existingVar = null;
			
			switch ( type ) {
			case "str":
			case "string":
				
				longType = "string";
				existingVar = DebugVars.getString(varKey, null);
				DebugVars.setString(varKey, stringValue, true);
				
				break;
			case "int":
			case "integer":
				
				longType = "integer";
				existingVar = DebugVars.getInteger(varKey, null);
				try {
					int integerValue = Integer.parseInt(stringValue);
					DebugVars.setInteger(varKey, integerValue, true);
				} catch (NumberFormatException ex) {
					sender.sendMessage(ChatColor.RED + stringValue + " could not be understood as an integer.");
					return true;
				}
				
				break;
			case "dec":
			case "decimal":
				
				longType = "decimal";
				existingVar = DebugVars.getDecimal(varKey, null);
				try {
					double decimalValue = Double.parseDouble(stringValue);
					DebugVars.setDecimal(varKey, decimalValue, true);
				} catch (NumberFormatException ex) {
					sender.sendMessage(ChatColor.RED + stringValue + " could not be understood as a decimal number.");
					return true;
				}
				
				break;
			case "bool":
			case "boolean":
				
				longType = "boolean";
				existingVar = DebugVars.getBoolean(varKey, null);
				if (stringValue.equalsIgnoreCase("true") || stringValue.equalsIgnoreCase("t")) {
					DebugVars.setBoolean(varKey, true, true);
				} else if (stringValue.equalsIgnoreCase("false") || stringValue.equalsIgnoreCase("f")) {
					DebugVars.setBoolean(varKey, false, true);
				} else {
					sender.sendMessage(ChatColor.RED + stringValue + " could not be understood as a boolean.");
					return true;
				}
				
				break;
			case "loc":
			case "location":

				if ( ! (sender instanceof Player player)) {
					sender.sendMessage(ChatColor.RED + "Cannot set a location unless you're in-game!");
					return true;
				}

				longType = "location";
				existingVar = DebugVars.getLocation(varKey, null);
				DebugVars.setLocation(varKey, player.getLocation(), true);

				break;
			default: 
				showUsage(sender);
				return true;
			}			
			
			if (existingVar != null) {
				sender.sendMessage(ChatColor.YELLOW + "(overwrote previous value of " + existingVar + ")");
			}
			
			printDebugVariable(sender, longType, varKey, stringValue);
			
			break;
		case "get":
			if (args.length < 3) {
				showUsage(sender);
				return true;
			}
			
			type = args[1].toLowerCase();
			longType = null;
			varKey = args[2].toLowerCase();
			existingVar = null;
			
			switch ( type ) {
			case "str":
			case "string":
				longType = "string";
				existingVar = DebugVars.getString(varKey, null);
				break;
			case "int":
			case "integer":		
				longType = "integer";
				existingVar = DebugVars.getInteger(varKey, null);
				break;
			case "dec":
			case "decimal":
				longType = "decimal";
				existingVar = DebugVars.getDecimal(varKey, null);
				break;
			case "bool":
			case "boolean":
				longType = "boolean";
				existingVar = DebugVars.getBoolean(varKey, null);
				break;
			case "loc":
			case "location":
				longType = "location";
				existingVar = DebugVars.getLocation(varKey, null);
				break;
			default: 
				showUsage(sender);
				return true;
			}
			
			if (existingVar == null) {
				sender.sendMessage("No debug variable found for key '" + varKey + "'");
			} else {
				printDebugVariable(sender, longType, varKey, existingVar);
			}
			
			break;
		case "list":
			if (args.length < 2) {
				showUsage(sender);
				return true;
			}
			
			type = args[1].toLowerCase();
			longType = null;
			
			switch ( type ) {
			case "str":
			case "string":
				if (DebugVars.debugStrings.isEmpty()) {
					sender.sendMessage(ChatColor.AQUA + "No debug strings defined to display");
					return true;
				}
				DebugVars.debugStrings.forEach((key, value) -> printDebugVariable(sender, "string", key, value));
				break;
			case "int":
			case "integer":	
				if (DebugVars.debugIntegers.isEmpty()) {
					sender.sendMessage(ChatColor.AQUA + "No debug integers defined to display");
					return true;
				}
				DebugVars.debugIntegers.forEach((key, value) -> printDebugVariable(sender, "integer", key, value));
				break;
			case "dec":
			case "decimal":
				if (DebugVars.debugDecimals.isEmpty()) {
					sender.sendMessage(ChatColor.AQUA + "No debug decimals defined to display");
					return true;
				}
				DebugVars.debugDecimals.forEach((key, value) -> printDebugVariable(sender, "decimal", key, value));
				break;
			case "bool":
			case "boolean":
				if (DebugVars.debugBooleans.isEmpty()) {
					sender.sendMessage(ChatColor.AQUA + "No debug booleans defined to display");
					return true;
				}
				DebugVars.debugBooleans.forEach((key, value) -> printDebugVariable(sender, "boolean", key, value));
				break;
			case "loc":
			case "location":
				if (DebugVars.debugLocations.isEmpty()) {
					sender.sendMessage(ChatColor.AQUA + "No debug locations defined to display");
					return true;
				}
				DebugVars.debugLocations.forEach((key, value) -> printDebugVariable(sender, "location", key, value));
				break;
			default: 
				showUsage(sender);
				return true;
			}
			
			break;
		case "help":
		default:
			showUsage(sender);
			return true;
		}
	
		return true;
	}
	
	private void printDebugVariable(CommandSender sender, String longType, String key, Object value) {
		sender.sendMessage(ChatColor.GREEN + "debug " + longType.toUpperCase() + " " + ChatColor.AQUA + key + ChatColor.GREEN + " - " + value);
	}

	private void showUsage(CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "<type> one of: str[ing] int[eger] dec[imal] bool[ean] loc[ation]");
		sender.sendMessage(ChatColor.GREEN + "/debugvar set <type> <var> <value>");
		sender.sendMessage(ChatColor.GREEN + "/debugvar get <type> <var>");
		sender.sendMessage(ChatColor.GREEN + "/debugvar list <type>");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> completions = new ArrayList<>();
		switch(args.length) {
		case 0:
			return Arrays.asList("get", "set", "list", "help");
		case 1:
			StringUtil.copyPartialMatches(args[0], Arrays.asList("get", "set", "list", "help"), completions);
			break;
		case 2:
			if ( ! (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("list"))) {				
				break;
			}
			StringUtil.copyPartialMatches(args[1], Arrays.asList("string", "integer", "decimal", "boolean", "location"), completions);
			break;
		case 3:
			if ( ! (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("set"))) {
				break;
			}
			String type = args[1].toLowerCase();
			switch (type) {
			case "str":
			case "string":
				StringUtil.copyPartialMatches(args[2], DebugVars.debugStrings.keySet(), completions);
				break;
			case "int":
			case "integer":
				StringUtil.copyPartialMatches(args[2], DebugVars.debugIntegers.keySet(), completions);
				break;
			case "dec":
			case "decimal":
				StringUtil.copyPartialMatches(args[2], DebugVars.debugDecimals.keySet(), completions);
				break;
			case "bool":
			case "boolean":
				StringUtil.copyPartialMatches(args[2], DebugVars.debugBooleans.keySet(), completions);
				break;
			case "loc":
			case "location":
				StringUtil.copyPartialMatches(args[2], DebugVars.debugLocations.keySet(), completions);
				break;
			}
			break;
		}
		
		return completions;
	}

}
