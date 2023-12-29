package net.punchtree.util.sounds.soundtest;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SoundTestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ( ! ( sender instanceof Player player )) {
            sender.sendMessage(ChatColor.RED + "Only a player can run this command");
            return true;
        }

        int page = 1;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException nfe) {
                return false;
            }
        }

        SoundMenu.openMenuFor(player, page);

        return true;
    }
}
