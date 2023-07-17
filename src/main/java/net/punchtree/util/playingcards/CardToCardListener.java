package net.punchtree.util.playingcards;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.punchtree.util.PunchTreeUtilPlugin;
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
import org.bukkit.scheduler.BukkitRunnable;

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

        attemptToDrawCardFromCardlike(hand, itemFrame, player, itemInHand);
    }

    private void onShiftRightClickCardlike(EquipmentSlot hand, ItemFrame itemFrame, Player player, ItemStack itemInHand) {
        CardBreakListener.onPlayerPickupCardlike(itemFrame, player);
    }

    private void attemptToDrawCardFromCardlike(EquipmentSlot hand, ItemFrame itemFrame, Player player, ItemStack itemInHand) {
        // If player's hand is empty, draw a card
        if (itemInHand.getType() == Material.AIR) {
            player.getInventory().setItem(hand, drawCard(itemFrame).getNewItem());
            return;
        }

        // Otherwise cautiously draw a card only if it will fit in their inventory
        HashMap<Integer, ItemStack> overflowItems = player.getInventory().addItem(peekCard(itemFrame).getNewItem());
        if (overflowItems.isEmpty()) {
            drawCard(itemFrame);
        } else {
            player.sendMessage(ChatColor.RED + "Your inventory is full!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
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

    private PlayingCard peekCard(ItemFrame itemFrame) {
        ItemStack itemStack = itemFrame.getItem();
        if (isSingleCard(itemStack)) {
            return PlayingCard.fromItem(itemStack);
        }

        BundleMeta bundleMeta = (BundleMeta) itemStack.getItemMeta();
        ItemStack drawnCard = bundleMeta.getItems().get(0);
        return PlayingCard.fromItem(drawnCard);
    }

    private PlayingCard drawCard(ItemFrame itemFrame) {
        ItemStack itemStack = itemFrame.getItem();

        if (isSingleCard(itemStack)) {
            itemFrame.remove();
            return PlayingCard.fromItem(itemStack);
        }

        BundleMeta bundleMeta = (BundleMeta) itemStack.getItemMeta();
        ItemStack drawnCard = bundleMeta.getItems().get(0);
        if (isFaceDownCard(drawnCard)) {
            drawnCard = flipCardlike(drawnCard);
        }

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
        return PlayingCard.fromItem(drawnCard);
    }

    static void showCardCount(ItemFrame frame) {
        ItemStack item = frame.getItem();
        if (!isCardlike(item)) {
            throw new IllegalArgumentException("ItemFrame must contain a cardlike item to show card count!");
        }

        TextComponent numberCount;
        if (isSingleCard(item)) {
            numberCount = Component.text("1");
            item.editMeta(meta -> meta.displayName(numberCount));
        } else {
            numberCount = Component.text(((BundleMeta) item.getItemMeta()).getItems().size());
            item.editMeta(meta -> {
                BundleMeta bundleMeta = (BundleMeta) meta;
                bundleMeta.displayName(numberCount);
            });
        }
        frame.setItem(item);

        TextComponent numberCountFinal = numberCount;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (frame.isValid()) {
                    ItemStack item = frame.getItem();
                    item.editMeta(meta -> {
                        if (meta.hasDisplayName() && meta.displayName().equals(numberCountFinal)) {
                            meta.displayName(null);
                        }
                    });
                    frame.setItem(item);
                }
            }
        }.runTaskLater(PunchTreeUtilPlugin.getInstance(), 20);
    }







}
