package net.punchtree.util.playingcards;

import net.punchtree.util.debugvar.DebugVars;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import java.util.HashMap;

import static net.punchtree.util.playingcards.PlayingCardUtils.*;

@SuppressWarnings("UnstableApiUsage")
public class CardToCardListener implements Listener {

    // Playing cards work by being an item in an item frame
    // Right clicking on the frame takes a card from it into the hot bar if the currently selected slot is open
    // Right clicking on the frame again puts the card back into the frame
    // There are three to four stack items depending on the number of cards in the stack
    // Cards can be played on any new surface and will create a new stack object when placed
    // Right clicking will place them face up, shift right clicking will place them face down
    // Shift right-clicking three times in quick succession will shuffle a deck
    // Left clicking three times in quick succession will offer the option to deal a deck

    @EventHandler
    public void onPlayerInteractWithEntity(PlayerInteractEntityEvent event) {
        if ( ! (event.getRightClicked() instanceof ItemFrame itemFrame)) return;

        Player player = event.getPlayer();
        ItemStack itemInFrame = itemFrame.getItem();
        EquipmentSlot hand = event.getHand();
        ItemStack itemInHand = player.getInventory().getItem(hand);

        if (isCardlike(itemInFrame)) {
            event.setCancelled(true);
            onRightClickCardlike(hand, itemFrame, player, itemInHand);
        }
    }

    // DebugVars.getBoolean("punchtree:right-click-draw-with-empty-hand", true)
    private void onRightClickCardlike(EquipmentSlot hand, ItemFrame itemFrame, Player player, ItemStack itemInHand) {
        if (player.isSneaking()) {
            onShiftRightClickCardlike(hand, itemFrame, player, itemInHand);
            return;
        }

        if (DebugVars.getBoolean(PUNCHTREE_CARDS_LEFT_CLICK_PLACE_FLAG, true)) {
            attemptToDrawCardFromCardlike(hand, itemFrame, player, itemInHand);
        } else {
            CardBreakListener.placeOneCardFromHandOntoCardlike(itemFrame, player);
        }
    }

    private void onShiftRightClickCardlike(EquipmentSlot hand, ItemFrame itemFrame, Player player, ItemStack itemInHand) {
        if (DebugVars.getBoolean(PUNCHTREE_CARDS_LEFT_CLICK_PLACE_FLAG, true)) {
            onPlayerPickupCardlike(itemFrame, player);
        } else {
            CardBreakListener.attemptToPlaceCardlikeOnCardlike(itemFrame, player);
        }
    }

    static void attemptToDrawCardFromCardlike(EquipmentSlot hand, ItemFrame itemFrame, Player player, ItemStack itemInHand) {
        // If player's hand is empty, draw a card
        if (itemInHand.getType() == Material.AIR) {
            player.getInventory().setItem(hand, drawCard(itemFrame));
            return;
        }

        // Otherwise cautiously draw a card only if it will fit in their inventory
        HashMap<Integer, ItemStack> overflowItems = player.getInventory().addItem(peekCard(itemFrame));
        if (overflowItems.isEmpty()) {
            drawCard(itemFrame);
        } else {
            player.sendMessage(ChatColor.RED + "Your inventory is full!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
        }
    }

    private static ItemStack peekCard(ItemFrame itemFrame) {
        ItemStack itemStack = itemFrame.getItem();
        if (isSingleCard(itemStack)) {
//            return PlayingCard.fromItem(itemStack);
            return itemStack;
        }

        BundleMeta bundleMeta = (BundleMeta) itemStack.getItemMeta();
        ItemStack drawnCard = bundleMeta.getItems().get(0);
        return drawnCard;
//        return PlayingCard.fromItem(drawnCard);
    }

    private static ItemStack drawCard(ItemFrame itemFrame) {
        ItemStack itemStack = itemFrame.getItem();

        if (isSingleCard(itemStack)) {
            itemFrame.remove();
//            return PlayingCard.fromItem(itemStack);
            return itemStack;
        }

        BundleMeta bundleMeta = (BundleMeta) itemStack.getItemMeta();
        ItemStack drawnCard = bundleMeta.getItems().get(0);
//        if (isFaceDownCard(drawnCard)) {
//            drawnCard = flipCardlike(drawnCard);
//        }

        if (isLastCardInStack(bundleMeta)) {
            itemFrame.remove();
        } else if (bundleMeta.getItems().size() == 2) {
            itemFrame.setItem(bundleMeta.getItems().get(1));
            showCardCount(itemFrame);
        } else {
            bundleMeta.setItems(bundleMeta.getItems().subList(1, bundleMeta.getItems().size()));

            if (isFaceUpCardStack(itemStack)) {
                updateTopCardOfStack(bundleMeta);
            }

            itemStack.setItemMeta(bundleMeta);
            itemFrame.setItem(itemStack);
            showCardCount(itemFrame);
        }
        return drawnCard;
//        return PlayingCard.fromItem(drawnCard);
    }

    static void onPlayerPickupCardlike(ItemFrame itemFrame, Player player) {
        ItemStack cardlikeToDrop = getBrokenCardItem(itemFrame.getItem());
        addItemToInventoryOrDrop(player, itemFrame, cardlikeToDrop);
        itemFrame.getWorld().playSound(itemFrame.getLocation(), Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1, 1);
        itemFrame.remove();
    }

    static ItemStack getBrokenCardItem(ItemStack cardlike) {
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

    private static void addItemToInventoryOrDrop(Player player, ItemFrame itemFrame, ItemStack cardToDrop) {
        HashMap<Integer, ItemStack> overflowItems = player.getInventory().addItem(cardToDrop);
        if (!overflowItems.isEmpty()) {
            itemFrame.getWorld().dropItem(itemFrame.getLocation(), cardToDrop);
        }
    }

}
