package net.punchtree.util.playingcards;

import net.punchtree.util.debugvar.DebugVars;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.checkerframework.checker.nullness.qual.NonNull;

import static net.punchtree.util.playingcards.PlayingCardUtils.*;

@SuppressWarnings("UnstableApiUsage")
public class CardBreakListener implements Listener {

    @EventHandler
    public void beforeHangBreak(EntityDamageByEntityEvent event) {
        if ( !(event.getEntity() instanceof ItemFrame itemFrame)) return;
        if ( !(event.getDamager() instanceof Player player)) return;
        if ( !isCardlike(itemFrame.getItem())) return;

        event.setCancelled(true);

        onLeftClickCardlike(itemFrame, player);
    }

    @EventHandler
    public void onHangBreak(HangingBreakEvent event) {
        if (!(event.getEntity() instanceof ItemFrame itemFrame)) return;
        if (!(isCardlike(itemFrame.getItem()))) return;

        event.setCancelled(true);

        if (event instanceof HangingBreakByEntityEvent entityEvent && entityEvent.getRemover() instanceof Player player) {
            onLeftClickCardlike(itemFrame, player);
        } else {
            onNonplayerBreakCardlike(itemFrame);
        }
    }

    private void onLeftClickCardlike(ItemFrame itemFrame, @NonNull Player player) {
        if (player.isSneaking()) {
            onShiftLeftClickCardlike(itemFrame, player);
            return;
        }

        // TODO VERIFY left click should always place only one card, even from a cardstack onto an existing cardlike
        if (DebugVars.getBoolean(PUNCHTREE_CARDS_LEFT_CLICK_PLACE_FLAG, true)) {
            placeOneCardFromHandOntoCardlike(itemFrame, player);
        } else {
            CardToCardListener.attemptToDrawCardFromCardlike(EquipmentSlot.HAND, itemFrame, player, player.getInventory().getItemInMainHand());
        }
    }

    private static void onShiftLeftClickCardlike(ItemFrame itemFrame, Player player) {
        if (DebugVars.getBoolean(PUNCHTREE_CARDS_LEFT_CLICK_PLACE_FLAG, true)) {
            attemptToPlaceCardlikeOnCardlike(itemFrame, player);
        } else {
            CardToCardListener.onPlayerPickupCardlike(itemFrame, player);
        }
    }

    static void attemptToPlaceCardlikeOnCardlike(ItemFrame itemFrame, Player player) {
        // TODO The method name implies already knowing the first condition to be true - this should be renamed or refactored
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (isCardlike(itemInHand)) {
            addCardlikeToFrame(itemFrame, itemInHand);
            player.getInventory().setItemInMainHand(null);
        } else {
            showCardCount(itemFrame);
        }
    }

    private static void addCardlikeToFrame(ItemFrame itemFrame, ItemStack cardlikeToAdd) {
        ItemStack combinedStack = combineCardStacks(cardlikeToAdd, itemFrame.getItem());
        itemFrame.setItem(combinedStack);
        showCardCount(itemFrame);
    }

    static void placeOneCardFromHandOntoCardlike(ItemFrame itemFrame, Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (isCardlike(itemInHand)) {
            if (isSingleCard(itemInHand)) {
                itemFrame.setItem(combineCardStacks(itemInHand, itemFrame.getItem()));
                player.getInventory().setItemInMainHand(null);
            } else {
                // This code kind of duplicates dropping cards in the inventory - refactoring opportunity?
                BundleMeta handMeta = (BundleMeta) itemInHand.getItemMeta();
                ItemStack topCard = handMeta.getItems().get(0);
                itemFrame.setItem(combineCardStacks(topCard, itemFrame.getItem()));
                if (handMeta.getItems().size() == 2) {
                    player.getInventory().setItemInMainHand(handMeta.getItems().get(1));
                } else {
                    handMeta.setItems(handMeta.getItems().subList(1, handMeta.getItems().size()));
                    updateTopCardOfStack(handMeta);
                    itemInHand.setItemMeta(handMeta);
                }
            }
        }
        // show the card count both if the player placed a card and if they didn't
        showCardCount(itemFrame);
    }

    static void onNonplayerBreakCardlike(ItemFrame itemFrame) {
        Bukkit.broadcastMessage("Cards broken by something other than a player!");
        ItemStack cardlikeToDrop = CardToCardListener.getBrokenCardItem(itemFrame.getItem());
        itemFrame.getWorld().dropItem(itemFrame.getLocation(), cardlikeToDrop);
        itemFrame.getWorld().playSound(itemFrame.getLocation(), Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1, 1);
        itemFrame.remove();
    }

    static void flipCardOnGround(ItemFrame itemFrame) {
        ItemStack flippedCard = flipCardlike(itemFrame.getItem());
        flippedCard.editMeta(meta -> meta.displayName(null));
        itemFrame.setItem(flippedCard);
    }

}
