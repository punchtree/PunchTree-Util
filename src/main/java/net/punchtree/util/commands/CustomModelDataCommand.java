package net.punchtree.util.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CustomModelDataCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if ( ! (sender instanceof Player)) return true;
		Player player = (Player) sender;
		
		if (args.length < 2) {
			player.sendMessage("/cmd <item> <number>");
			return true;
		}
		
		Bukkit.getServer().dispatchCommand(player, String.format("minecraft:give %s %s{CustomModelData:%s}", player.getName(), args[0], args[1]));
		return true;
	}

}
