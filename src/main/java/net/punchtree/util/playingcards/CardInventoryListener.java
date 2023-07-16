package net.punchtree.util.playingcards;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
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

    private static final Sound SHUFFLING_SOUND = Sound.sound(Key.key("punchtree", "playing_cards.shuffle"), Sound.Source.PLAYER, 1, 1);
    private static final Sound CARD_FLIP_SOUND = Sound.sound(Key.key("minecraft", "ui.toast.in"), Sound.Source.PLAYER, 1, 1);

    @EventHandler
    public void onFlipCard(PlayerSwapHandItemsEvent event) {
        ItemStack mainHandItem = event.getMainHandItem();
        ItemStack offHandItem = event.getOffHandItem();
        Player player = event.getPlayer();
        if (player.isSneaking() || (!isCardlike(mainHandItem) && !isCardlike(offHandItem))) {

            // This is maybe a questionable use of tick performance - has to search in world for entity being looked at
            boolean flippedACard = attemptToFlipCardInWorld(event);

            if (flippedACard) {
                event.setCancelled(true);
                return;
            }
        }

        if (isCardlike(mainHandItem)) {
            event.setCancelled(true);
            ItemStack flippedCard = flipCardlike(mainHandItem);
            player.getInventory().setItemInOffHand(flippedCard);
            player.playSound(CARD_FLIP_SOUND);
        }
        if (isCardlike(offHandItem)) {
            event.setCancelled(true);
            ItemStack flippedCard = flipCardlike(offHandItem);
            player.getInventory().setItemInMainHand(flippedCard);
            player.playSound(CARD_FLIP_SOUND);
        }
    }

    private boolean attemptToFlipCardInWorld(PlayerSwapHandItemsEvent event) {
        final int REACH = 5;
        Block clickedBlock = event.getPlayer().getTargetBlockExact(REACH, FluidCollisionMode.NEVER);
        if (clickedBlock == null) return false;
        BlockFace clickedFace = event.getPlayer().getTargetBlockFace(REACH, FluidCollisionMode.NEVER);
        Optional<ItemFrame> first = clickedBlock.getLocation().getNearbyEntities(2, 2, 2).stream()
                .filter(entity -> entity instanceof ItemFrame)
                .map(entity -> (ItemFrame) entity)
                .filter(itemFrame -> {
                    Location location = itemFrame.getLocation();
                    Block newFrameBlock = clickedBlock.getRelative(clickedFace);
                    return location.getBlockX() == newFrameBlock.getX()
                            && location.getBlockY() == newFrameBlock.getY()
                            && location.getBlockZ() == newFrameBlock.getZ()
                            && isCardlike(itemFrame.getItem());
                }).findFirst();
        first.ifPresent(CardBreakListener::flipCardOnGround);
        return first.isPresent();
    }

    @EventHandler
    public void onCardClick(InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        ItemStack currentItem = event.getCurrentItem();
        if (!isCardlike(cursor) && !isCardlike(currentItem)) return;

        ClickType clickType = event.getClick();
        InventoryAction action = event.getAction();
//        Bukkit.broadcastMessage(clickType.name());
//        Bukkit.broadcastMessage(action.name());
//        Bukkit.broadcastMessage("Cursor is: " + (isCardStack(cursor) ? "Card Stack" : isCardlike(cursor) ? "Card" : " Not a card"));
//        Bukkit.broadcastMessage("Current is: " + (isCardStack(currentItem) ? "Card Stack" : isCardlike(currentItem) ? "Card" : "Not a card"));
//        Bukkit.broadcastMessage(event.getSlotType().name() + " " + event.getSlot());

        // If clicking on output of a crafting grid
        if (event.getSlotType() == SlotType.RESULT && isCardStack(currentItem)) {
            if (clickType == ClickType.RIGHT) {
                event.setCancelled(true);
                return;
            } else {
                event.getWhoClicked().playSound(SHUFFLING_SOUND);
            }
            return;
        }

        // If double-clicking
        if (clickType == ClickType.DOUBLE_CLICK && isCardlike(cursor)) {
            onDoubleClickWhileHoldingCardlike(event, cursor);
            return;
        }

        // If flipping over a card
        if (clickType == ClickType.SWAP_OFFHAND && action == InventoryAction.HOTBAR_SWAP) {
            event.setCurrentItem(flipCardlike(currentItem));
            event.setCancelled(true);
            return;
        }

        // Equivalent of drawing a card only if hand is empty (right-clicking with nothing in cursor)
        if (!isCardlike(cursor) && isCardlike(currentItem)
                && clickType == ClickType.RIGHT && action == InventoryAction.PICKUP_HALF) {
            // FIXME this can be called with a single-card current item, but this method casts the meta as a bundle! ERROR!
            fixTakingCardsOutOfCardStacks(event, currentItem, false);
            return;
        }

        if (!isCardlike(cursor)) return;

        // Dropping single cards out of a stack
        if (action == InventoryAction.DROP_ONE_SLOT && isCardlike(currentItem)) {
            if (isSingleCard(cursor)) {
                event.setCurrentItem(combineCardStacks(cursor, currentItem));
                event.setCursor(null);
                event.setCancelled(true);
                return;
            }
            BundleMeta cursorMeta = (BundleMeta) cursor.getItemMeta();
            ItemStack topCard = cursorMeta.getItems().get(0);
            event.setCurrentItem(combineCardStacks(topCard, currentItem));
            if (cursorMeta.getItems().size() == 2) {
                event.setCursor(cursorMeta.getItems().get(1));
            } else {
                cursorMeta.setItems(cursorMeta.getItems().subList(1, cursorMeta.getItems().size()));
                updateTopCardOfStack(cursorMeta);
                cursor.setItemMeta(cursorMeta);
            }

            event.setCancelled(true);
        }

        // Checking for action NOTHING is only necessary for combining identical cards - meaning that if we are using a
        // randomized nbt to make paper nonstackable, it's probably not necessary - still, it's probably harmless
        if (clickType == ClickType.RIGHT && (action == InventoryAction.SWAP_WITH_CURSOR || action == InventoryAction.NOTHING) && isCardlike(currentItem)) {
            ItemStack newCardStack = combineCardStacks(cursor, currentItem);
            event.setCurrentItem(newCardStack);
            event.setCursor(null);
            event.setCancelled(true);
        } else if (clickType == ClickType.SHIFT_RIGHT && isCardlike(currentItem)) {
            ItemStack newCardStack = combineCardStacks(currentItem, cursor);
            event.setCurrentItem(null);
            event.setCursor(newCardStack);
            event.setCancelled(true);
        } else if (clickType == ClickType.RIGHT && action == InventoryAction.PLACE_ONE && isCardStack(cursor)) {
            fixTakingCardsOutOfCardStacks(event, cursor, true);
        }
    }

    private static void onDoubleClickWhileHoldingCardlike(InventoryClickEvent event, ItemStack cursor) {
        List<ItemStack> allCardslikes = Stream.of(event.getWhoClicked().getInventory().getStorageContents())
                .filter(PlayingCardUtils::isCardlike)
                .toList();

        if (allCardslikes.isEmpty() && isCardStack(cursor)) {
            explodeHeldCardStack(event, cursor);
        } else {
            collectAllCardsToCursor(event, cursor, allCardslikes);
        }
    }

    private static void collectAllCardsToCursor(InventoryClickEvent event, ItemStack cursor, List<ItemStack> allCardslikes) {
        ItemStack allCardsInOneStack = allCardslikes.stream().reduce(cursor, PlayingCardUtils::combineCardStacks, PlayingCardUtils::combineCardStacks);
        event.setCursor(allCardsInOneStack);
        event.getWhoClicked().getInventory().forEach(itemStack -> {
            if (isCardlike(itemStack)) {
                itemStack.setAmount(0);
            }
        });
    }

    private static void explodeHeldCardStack(InventoryClickEvent event, ItemStack cursor) {
        event.setCurrentItem(cursor); // This just prevents adding items from taking up the current item slot
        BundleMeta bundleMeta = (BundleMeta) cursor.getItemMeta();
        Collection<ItemStack> leftOverCards = event.getWhoClicked().getInventory().addItem(bundleMeta.getItems().toArray(new ItemStack[bundleMeta.getItems().size()])).values();
        ItemStack leftOverCardsInAStack = PlayingCard.getItemForCardList(leftOverCards.stream().map(PlayingCard::fromItem).collect(Collectors.toList()));
        event.setCurrentItem(leftOverCardsInAStack);
        event.setCursor(null);
        event.setCancelled(true);
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
            && ingredients.containsKey(Material.BUNDLE)
            && ingredients.get(Material.BUNDLE).size() == 1
            && isCardlike(ingredients.get(Material.BUNDLE).get(0))) {
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
