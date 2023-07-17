package net.punchtree.util.playingcards;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class PlayingCardCommands implements CommandExecutor, TabCompleter {

    private static final String GET_DECK_SUBCOMMAND = "get-deck";
    private static final String HELP_SUBCOMMAND = "help";
    private static final List<String> SUBCOMMANDS = List.of(GET_DECK_SUBCOMMAND, HELP_SUBCOMMAND);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if ( ! (sender instanceof Player player)) return false;
        if (args.length < 1) return false;

        switch (args[0]) {
            case GET_DECK_SUBCOMMAND -> player.getInventory().addItem(PlayingCard.getNewDeck());
            case HELP_SUBCOMMAND -> {
                Component header = Component.text("♥").color(NamedTextColor.DARK_RED)
                        .append(Component.text("♣").color(NamedTextColor.BLACK))
                        .append(Component.text(" Playing Card Controls ").color(NamedTextColor.DARK_AQUA))
                        .append(Component.text("♠").color(NamedTextColor.BLACK))
                        .append(Component.text("♦\n").color(NamedTextColor.DARK_RED));
                player.sendMessage(header.append(
                        Component.text(" Right click to draw 1 card\n").append(
                        Component.text(" Left click to place 1 card\n")).append(
                        Component.text(" Sneak for the same action with the whole stack\n")).append(
                        Component.text(" Offhand-swap to flip cards over\n")).append(
                        Component.text(" Double-click in inventory to collect all\n")).append(
                        Component.text(" Craft to shuffle\n")).append(
                        Component.text(" /playingcards for info and options"))
                        .color(NamedTextColor.AQUA)));
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length >= 2) return Collections.emptyList();
        return SUBCOMMANDS;
    }
}
