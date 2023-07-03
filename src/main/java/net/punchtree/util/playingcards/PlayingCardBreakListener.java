package net.punchtree.util.playingcards;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

import static net.punchtree.util.playingcards.PlayingCardUtils.*;

@SuppressWarnings("UnstableApiUsage")
public class PlayingCardBreakListener implements Listener {

    @EventHandler
    public void onHangBreak(HangingBreakEvent event) {
        if (!(event.getEntity() instanceof ItemFrame itemFrame)) return;
        ItemStack item = itemFrame.getItem();
        if (!(isCardOrCardStack(item))) return;

        event.setCancelled(true);

        if (event instanceof HangingBreakByEntityEvent entityEvent
                && entityEvent.getRemover() instanceof Player player
                && player.isSneaking()) {
            itemFrame.setItem(flipCardOrCardStack(item));
            return;
        }

        ItemStack cardToDrop = item;
        if (item.getItemMeta() instanceof BundleMeta bundleMeta) {
            if (isFaceDownCardStack(item)) {
                // make this a face down card
                if (isLastCardInStack(bundleMeta)) {
                    cardToDrop = flipCardOrCardStack(bundleMeta.getItems().get(0));
                } else {
                    // face down card stack > 1
                    bundleMeta.displayName(PlayingCard.FACE_DOWN_CARD_PILE_NAME);
                }
            } else {
                if (isLastCardInStack(bundleMeta)) {
                    cardToDrop = bundleMeta.getItems().get(0);
                } else {
                    // face up card stack > 1
                    bundleMeta.displayName(PlayingCard.fromItem(bundleMeta.getItems().get(0)).getName());
                }
            }
            item.setItemMeta(bundleMeta);
        } else {
            // Card is a single card, not a stack - shouldn't happen
            item.editMeta(meta -> meta.displayName(PlayingCard.fromItem(item).getName()));
        }

        if (event instanceof HangingBreakByEntityEvent entityEvent
                && entityEvent.getRemover() instanceof Player player) {
            addItemToInventoryOrDrop(player, itemFrame, cardToDrop);
        } else {
            itemFrame.getWorld().dropItem(itemFrame.getLocation(), cardToDrop);
        }
        itemFrame.getWorld().playSound(itemFrame.getLocation(), Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1, 1);
        itemFrame.remove();
    }

    private void addItemToInventoryOrDrop(Player player, ItemFrame itemFrame, ItemStack cardToDrop) {
        HashMap<Integer, ItemStack> overflowItems = player.getInventory().addItem(cardToDrop);
        if (!overflowItems.isEmpty()) {
            itemFrame.getWorld().dropItem(itemFrame.getLocation(), cardToDrop);
        }
    }

}
