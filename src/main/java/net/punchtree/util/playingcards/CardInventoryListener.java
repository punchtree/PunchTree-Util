package net.punchtree.util.playingcards;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import static net.punchtree.util.playingcards.PlayingCardUtils.*;

public class CardInventoryListener implements Listener {

    @EventHandler
    public void onFlipCard(PlayerSwapHandItemsEvent event) {
        ItemStack mainHandItem = event.getMainHandItem();
        if (isCardOrCardStack(mainHandItem)) {
            event.setCancelled(true);
            ItemStack flippedCard = flipCardOrCardStack(mainHandItem);
            event.getPlayer().getInventory().setItemInOffHand(flippedCard);
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_TOAST_IN, 1, 1);
        }
        ItemStack offHandItem = event.getOffHandItem();
        if (isCardOrCardStack(offHandItem)) {
            event.setCancelled(true);
            ItemStack flippedCard = flipCardOrCardStack(offHandItem);
            event.getPlayer().getInventory().setItemInMainHand(flippedCard);
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_TOAST_IN, 1, 1);
        }
    }

    @EventHandler
    public void onDragCardStack(InventoryClickEvent event) {
        if (!isCardOrCardStack(event.getCursor())) return;
        Bukkit.broadcastMessage(event.getClick().name());
        Bukkit.broadcastMessage(event.getAction().name());
        Bukkit.broadcastMessage("Cursor is: " + (isCardStack(event.getCursor()) ? "Card Stack" : "Card"));
        Bukkit.broadcastMessage("Current is: " + (isCardStack(event.getCurrentItem()) ? "Card Stack" : isCardOrCardStack(event.getCurrentItem()) ? "Card" : "Not a card"));
    }


}
