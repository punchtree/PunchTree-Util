package net.punchtree.util.playingcards;

import net.kyori.adventure.text.Component;
import net.punchtree.util.PunchTreeUtilPlugin;
import org.bukkit.*;
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
import java.util.List;
import java.util.stream.Stream;

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

        if (isCardStack(itemInFrame)) {
            event.setCancelled(true);
            onInteractWithPlacedCards(hand, itemFrame, player, itemInHand);
        }
    }

    private void onInteractWithPlacedCards(EquipmentSlot hand, ItemFrame itemFrame, Player player, ItemStack itemInHand) {
        if (player.isSneaking()) {
            attemptToPlaceCardOnCardStack(hand, itemFrame, player, itemInHand);
            return;
        }

        attemptToDrawCardFromCardStack(hand, itemFrame, player, itemInHand);
    }

    private void attemptToDrawCardFromCardStack(EquipmentSlot hand, ItemFrame itemFrame, Player player, ItemStack itemInHand) {
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

    private void attemptToPlaceCardOnCardStack(EquipmentSlot hand, ItemFrame itemFrame, Player player, ItemStack itemInHand) {
        if (isCardOrCardStack(itemInHand)) {
            addCardOrCardStackToFrame(itemFrame, itemInHand);
            player.getInventory().setItem(hand, null);
        } else {
            showCardCount(itemFrame);
        }
    }

    private void addCardOrCardStackToFrame(ItemFrame itemFrame, ItemStack cardOrCardStackToAdd) {
        ItemStack itemStack = itemFrame.getItem();
        BundleMeta bundleMeta = (BundleMeta) itemStack.getItemMeta();
        if (isFaceUpCard(cardOrCardStackToAdd)) {
            List<ItemStack> items = Stream.concat(Stream.of(cardOrCardStackToAdd), bundleMeta.getItems().stream()).toList();
            bundleMeta.setItems(items);
        } else if (isCardStack(cardOrCardStackToAdd)) {
            List<ItemStack> items = Stream.concat(((BundleMeta) cardOrCardStackToAdd.getItemMeta()).getItems().stream(), bundleMeta.getItems().stream()).toList();
            bundleMeta.setItems(items);
        } else if (isFaceDownCard(cardOrCardStackToAdd)) {
            List<ItemStack> items = Stream.concat(Stream.of(flipCardOrCardStack(cardOrCardStackToAdd)), bundleMeta.getItems().stream()).toList();
            bundleMeta.setItems(items);
        }
        if (!isFaceDownCardStack(itemStack)) {
            updateTopCardOfStack(bundleMeta);
        }
        itemStack.setItemMeta(bundleMeta);
        itemFrame.setItem(itemStack);
        showCardCount(itemFrame);
    }

    private PlayingCard peekCard(ItemFrame itemFrame) {
        ItemStack itemStack = itemFrame.getItem();
        BundleMeta bundleMeta = (BundleMeta) itemStack.getItemMeta();
        ItemStack drawnCard = bundleMeta.getItems().get(0);
        return PlayingCard.fromItem(drawnCard);
    }

    private PlayingCard drawCard(ItemFrame itemFrame) {
        ItemStack itemStack = itemFrame.getItem();

        BundleMeta bundleMeta = (BundleMeta) itemStack.getItemMeta();
        ItemStack drawnCard = bundleMeta.getItems().get(0);

        if (isLastCardInStack(bundleMeta)) {
            itemFrame.remove();
        } else {
            bundleMeta.setItems(bundleMeta.getItems().subList(1, bundleMeta.getItems().size()));

            if (!isFaceDownCardStack(itemStack)) {
                updateTopCardOfStack(bundleMeta);
            }

            itemStack.setItemMeta(bundleMeta);
            itemFrame.setItem(itemStack);
            showCardCount(itemFrame);
        }
        return PlayingCard.fromItem(drawnCard);
    }

    private void showCardCount(ItemFrame frame) {
        ItemStack item = frame.getItem();
        if (!isCardStack(item)) return;
        item.editMeta(meta -> {
            BundleMeta bundleMeta = (BundleMeta) meta;
            bundleMeta.displayName(Component.text(bundleMeta.getItems().size()));
        });
        frame.setItem(item);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (frame.isValid()) {
                    ItemStack item = frame.getItem();
                    item.editMeta(meta -> meta.displayName(null));
                    frame.setItem(item);
                }
            }
        }.runTaskLater(PunchTreeUtilPlugin.getInstance(), 20);
    }







}
