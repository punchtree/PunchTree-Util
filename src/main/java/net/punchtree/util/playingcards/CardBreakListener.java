package net.punchtree.util.playingcards;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;

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

        // TODO left click should always place only one card, even from a cardstack onto an existing cardlike
        placeOneCardFromHandOntoCardlike(itemFrame, player);
    }

    private void placeOneCardFromHandOntoCardlike(ItemFrame itemFrame, Player player) {
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
        CardToCardListener.showCardCount(itemFrame);
    }

    private static void onShiftLeftClickCardlike(ItemFrame itemFrame, Player player) {
        CardToCardListener.attemptToPlaceCardlikeOnCardlike(itemFrame, player);
    }

    static void onNonplayerBreakCardlike(ItemFrame itemFrame) {
        Bukkit.broadcastMessage("Cards broken by something other than a player!");
        ItemStack cardlikeToDrop = getBrokenCardItem(itemFrame.getItem());
        itemFrame.getWorld().dropItem(itemFrame.getLocation(), cardlikeToDrop);
        itemFrame.getWorld().playSound(itemFrame.getLocation(), Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1, 1);
        itemFrame.remove();
    }

    static void onPlayerPickupCardlike(ItemFrame itemFrame, Player player) {
        ItemStack cardlikeToDrop = getBrokenCardItem(itemFrame.getItem());
        addItemToInventoryOrDrop(player, itemFrame, cardlikeToDrop);
        itemFrame.getWorld().playSound(itemFrame.getLocation(), Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1, 1);
        itemFrame.remove();
    }

    private static ItemStack getBrokenCardItem(ItemStack cardlike) {
        ItemStack cardToDrop = cardlike;
        if (isFaceDownCardStack(cardlike)) {
            BundleMeta bundleMeta = (BundleMeta) cardlike.getItemMeta();
            // make this a face down card
            if (isLastCardInStack(bundleMeta)) {
                // this is legacy behavior
                Bukkit.broadcastMessage("Breaking a single face down card THAT'S A BUNDLE");
                cardToDrop = flipCardlike(bundleMeta.getItems().get(0));
            } else {
                // face down card stack > 1
                bundleMeta.displayName(PlayingCard.FACE_DOWN_CARD_PILE_NAME);
            }
            cardlike.setItemMeta(bundleMeta);
        } else if (isFaceUpCardStack(cardlike)) {
            BundleMeta bundleMeta = (BundleMeta) cardlike.getItemMeta();
            if (isLastCardInStack(bundleMeta)) {
                // this is legacy behavior
                Bukkit.broadcastMessage("Breaking a single face up card THAT'S A BUNDLE");
                cardToDrop = bundleMeta.getItems().get(0);
            } else {
                // face up card stack > 1
                bundleMeta.displayName(PlayingCard.fromItem(bundleMeta.getItems().get(0)).getName());
            }
            cardlike.setItemMeta(bundleMeta);
        } else if (isFaceUpCard(cardlike)){
            // need to reset the title because it may be modified by the card count indicator
            cardlike.editMeta(meta -> meta.displayName(PlayingCard.fromItem(cardlike).getName()));
        } else if (isFaceDownCard(cardlike)) {
            // need to reset the title because it may be modified by the card count indicator
            cardlike.editMeta(meta -> meta.displayName(PlayingCard.FACE_DOWN_CARD_NAME));
        } else {
            throw new IllegalArgumentException("ItemStack supposed to be a broken cardlike is not a card or card stack");
        }
        return cardToDrop;
    }

    static void flipCardOnGround(ItemFrame itemFrame) {
        ItemStack flippedCard = flipCardlike(itemFrame.getItem());
        flippedCard.editMeta(meta -> meta.displayName(null));
        itemFrame.setItem(flippedCard);
    }

    private static void addItemToInventoryOrDrop(Player player, ItemFrame itemFrame, ItemStack cardToDrop) {
        HashMap<Integer, ItemStack> overflowItems = player.getInventory().addItem(cardToDrop);
        if (!overflowItems.isEmpty()) {
            itemFrame.getWorld().dropItem(itemFrame.getLocation(), cardToDrop);
        }
    }

}
