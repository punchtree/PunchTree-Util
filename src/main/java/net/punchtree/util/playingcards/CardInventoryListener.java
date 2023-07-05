package net.punchtree.util.playingcards;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.punchtree.util.playingcards.PlayingCardUtils.*;

@SuppressWarnings("UnstableApiUsage")
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
    public void onCardClick(InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        ItemStack currentItem = event.getCurrentItem();
        if (!isCardOrCardStack(cursor) && !isCardOrCardStack(currentItem)) return;

        ClickType clickType = event.getClick();
        InventoryAction action = event.getAction();
//        Bukkit.broadcastMessage(clickType.name());
//        Bukkit.broadcastMessage(action.name());
//        Bukkit.broadcastMessage("Cursor is: " + (isCardStack(cursor) ? "Card Stack" : isCardOrCardStack(cursor) ? "Card" : " Not a card"));
//        Bukkit.broadcastMessage("Current is: " + (isCardStack(currentItem) ? "Card Stack" : isCardOrCardStack(currentItem) ? "Card" : "Not a card"));

        if (clickType == ClickType.DOUBLE_CLICK && isCardOrCardStack(cursor)) {
            List<ItemStack> allCardsOrCardStacks = Stream.of(event.getWhoClicked().getInventory().getStorageContents())
                    .filter(PlayingCardUtils::isCardOrCardStack)
                    .toList();

            if (allCardsOrCardStacks.isEmpty() && isCardStack(cursor)) {
                event.setCurrentItem(cursor); // This just prevents adding items from taking up the current item slot
                BundleMeta bundleMeta = (BundleMeta) cursor.getItemMeta();
                Collection<ItemStack> leftOverCards = event.getWhoClicked().getInventory().addItem(bundleMeta.getItems().toArray(new ItemStack[bundleMeta.getItems().size()])).values();
                ItemStack leftOverCardsInAStack = PlayingCard.getItemForCardList(leftOverCards.stream().map(PlayingCard::fromItem).collect(Collectors.toList()));
                event.setCurrentItem(leftOverCardsInAStack);
                event.setCursor(null);
                event.setCancelled(true);
                return;
            }

            ItemStack allCardsInOneStack = allCardsOrCardStacks.stream().reduce(cursor, PlayingCardUtils::combineCardStacks, PlayingCardUtils::combineCardStacks);
            event.setCursor(allCardsInOneStack);
            event.getWhoClicked().getInventory().forEach(itemStack -> {
                if (isCardOrCardStack(itemStack)) {
                    itemStack.setAmount(0);
                }
            });
            return;
        }

        if (clickType == ClickType.SWAP_OFFHAND && action == InventoryAction.HOTBAR_SWAP) {
            event.setCurrentItem(flipCardOrCardStack(currentItem));
            event.setCancelled(true);
            return;
        }

        if (!isCardOrCardStack(cursor) && isCardOrCardStack(currentItem) && clickType == ClickType.RIGHT && action == InventoryAction.PICKUP_HALF) {
            fixTakingCardsOutOfCardStacks(event, currentItem, false);
            return;
        }

        if (!isCardOrCardStack(cursor)) return;

        // Checking for action NOTHING is only necessary for combining identical cards - meaning that if we are using a
        // randomized nbt to make paper nonstackable, it's probably not necessary - still, it's probably harmless
        if (clickType == ClickType.RIGHT && (action == InventoryAction.SWAP_WITH_CURSOR || action == InventoryAction.NOTHING) && isCardOrCardStack(currentItem)) {
            ItemStack newCardStack = combineCardStacks(currentItem, cursor);
            event.setCurrentItem(null);
            event.setCursor(newCardStack);
            event.setCancelled(true);
        } else if (clickType == ClickType.RIGHT && action == InventoryAction.PLACE_ONE && isCardStack(cursor)) {
            fixTakingCardsOutOfCardStacks(event, cursor, true);
        }
    }

    private void fixTakingCardsOutOfCardStacks(InventoryClickEvent event, ItemStack cardStack, boolean isCardStackCursor) {
        boolean isFaceDown = isFaceDownCardStack(cardStack);
        BundleMeta bundleMeta = (BundleMeta) cardStack.getItemMeta();
        ItemStack topCard = bundleMeta.getItems().get(0);
        ItemStack secondCard = bundleMeta.getItems().get(1);

        if (bundleMeta.getItems().size() == 2) {
            if (isCardStackCursor) {
                event.setCursor(secondCard);
                event.setCurrentItem(topCard);
            } else {
                event.setCursor(topCard);
                event.setCurrentItem(secondCard);
            }
            event.setCancelled(true);
            return;
        }

        // Fix top card if not face down
        if (!isFaceDown) {
            bundleMeta.setCustomModelData(secondCard.getItemMeta().getCustomModelData());
            cardStack.setItemMeta(bundleMeta);
        }
    }

    @EventHandler
    public void onPlayerCraft(PrepareItemCraftEvent event) {
        Map<Material, List<ItemStack>> ingredients = Stream.of(event.getInventory().getStorageContents())
                .collect(Collectors.groupingBy(ItemStack::getType));
        if (ingredients.size() == 2
            && ingredients.containsKey(Material.AIR)
            && ingredients.get(Material.BUNDLE).size() == 1
            && isCardOrCardStack(ingredients.get(Material.BUNDLE).get(0))) {
            event.getInventory().setResult(getShuffledCopyOf(ingredients.get(Material.BUNDLE).get(0)));
        }
    }

    private ItemStack getShuffledCopyOf(ItemStack cardStack) {
        ItemStack clone = cardStack.clone();
        clone.editMeta(meta -> {
            BundleMeta bundleMeta = (BundleMeta) meta;
            ArrayList<ItemStack> shuffledCards = new ArrayList<>(bundleMeta.getItems());
            Collections.shuffle(shuffledCards);
            bundleMeta.setItems(shuffledCards);
            if (isFaceUpCardStack(cardStack)) {
                updateTopCardOfStack(bundleMeta);
            }
        });
        return clone;
    }

    // TODO try PlayerInventorySlotChangeEvent for creative picking?

}
