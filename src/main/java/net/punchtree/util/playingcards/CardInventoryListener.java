package net.punchtree.util.playingcards;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.punchtree.util.playingcards.PlayingCardUtils.*;

@SuppressWarnings("UnstableApiUsage")
public class CardInventoryListener implements Listener {

    private static final Sound SHUFFLING_SOUND = Sound.sound(Key.key("punchtree", "playing_cards.shuffle"), Sound.Source.AMBIENT, 1, 1);
    private static final Sound CARD_FLIP_SOUND = Sound.sound(Key.key("minecraft", "ui.toast.in"), Sound.Source.AMBIENT, 1, 1);
    private static final Sound CARD_INTERACTION_FAILURE_SOUND = Sound.sound(Key.key("minecraft", "entity.villager.no"), Sound.Source.AMBIENT, 1, 1);

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
        Bukkit.broadcastMessage(clickType.name());
        Bukkit.broadcastMessage(action.name());
        Bukkit.broadcastMessage("Cursor is: " + (isCardStack(cursor) ? "Card Stack" : isCardlike(cursor) ? "Card" : " Not a card"));
        Bukkit.broadcastMessage("Current is: " + (isCardStack(currentItem) ? "Card Stack" : isCardlike(currentItem) ? "Card" : "Not a card"));
//        Bukkit.broadcastMessage(event.getSlotType().name() + " " + event.getSlot());
        Bukkit.broadcastMessage("=============");

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


        if (clickType == ClickType.RIGHT) {
            onRightClickInInventory(event, cursor, currentItem);
            return;
        }

        if (clickType == ClickType.SHIFT_RIGHT) {
            onShiftRightClickInInventory(event, cursor, currentItem);
        } else if (clickType == ClickType.LEFT) {
            onLeftClickInInventory(event, cursor, currentItem);
        } else if (clickType == ClickType.SHIFT_LEFT) {
            onShiftLeftClickInInventory(event, cursor, currentItem);
        } else if (action == InventoryAction.DROP_ONE_SLOT) {
            placeOneCard(event, cursor, currentItem);
        } else if (action == InventoryAction.DROP_ALL_SLOT) {
            placeAllCards(event, cursor, currentItem);
        }
    }


    private void onLeftClickInInventory(InventoryClickEvent event, ItemStack cursor, ItemStack currentItem) {
        placeOneCard(event, cursor, currentItem);
    }

    private void onShiftLeftClickInInventory(InventoryClickEvent event, ItemStack cursor, ItemStack currentItem) {
        // Shift left click -> place all
        placeAllCards(event, cursor, currentItem);
    }

    private void onRightClickInInventory(InventoryClickEvent event, ItemStack cursor, ItemStack currentItem) {
        drawOneCard(event, cursor, currentItem);
    }

    private static void onShiftRightClickInInventory(InventoryClickEvent event, ItemStack cursor, ItemStack currentItem) {
        drawAllCards(event, cursor, currentItem);
    }

    private static void placeOneCard(InventoryClickEvent event, ItemStack cursor, ItemStack currentItem) {
        if (!isCardlike(cursor)) {
            // In this case, the cursor is not a cardlike, but the stack is, and we're left-clicking
            // The expected default behavior would be to swap out the cursor and the cardlike item beneath it
            // But afterward, left-clicking with the card will attempt to place one card from the stack, NOT swap again
            // So this is kind of mixing control schemas and affordances - it's not clear what the user should expect
            // So we'll just cancel the event instead
            event.setCancelled(true);
            return;
        }

        if (isSingleCard(cursor)) {
            if (isCardlike(currentItem)) {
                event.setCurrentItem(combineCardStacks(cursor, currentItem));
                event.setCursor(null);
            } else {
                event.setCurrentItem(cursor);
                event.setCursor(currentItem);
            }
            event.setCancelled(true);
            return;
        }

        // Cursor is holding a cardstack
        // Because we're dropping ONE card, we can't support the operation on a slot that
        // already has a non-cardlike item in it, because that item will have nowhere to go

        if (currentItem != null && currentItem.getType() != Material.AIR && !isCardlike(currentItem)) {
            event.getWhoClicked().playSound(CARD_INTERACTION_FAILURE_SOUND);
            event.setCancelled(true);
            return;
        }

        // Current item is empty or a cardlike and cursor is a stack
        BundleMeta cursorMeta = (BundleMeta) cursor.getItemMeta();
        ItemStack topCard = cursorMeta.getItems().get(0);
        if (event.getCurrentItem().getType() == Material.AIR) {
            event.setCurrentItem(topCard);
        } else if (isCardlike(currentItem)) {
            event.setCurrentItem(combineCardStacks(topCard, currentItem));
        } else {
            throw new IllegalStateException("This should never happen");
        }

        if (cursorMeta.getItems().size() == 2) {
            event.setCursor(cursorMeta.getItems().get(1));
        } else {
            cursorMeta.setItems(cursorMeta.getItems().subList(1, cursorMeta.getItems().size()));
            updateTopCardOfStack(cursorMeta);
            cursor.setItemMeta(cursorMeta);
        }

        event.setCancelled(true);
    }

    private static void placeAllCards(InventoryClickEvent event, ItemStack cursor, ItemStack currentItem) {
        if (!isCardlike(cursor)) {
            event.setCancelled(true);
            return;
        }

        if (isCardlike(currentItem)) {
            event.setCurrentItem(combineCardStacks(cursor, currentItem));
            event.setCursor(null);
        } else {
            event.setCurrentItem(cursor);
            event.setCursor(currentItem);
        }
        event.setCancelled(true);
    }

    private static void drawOneCard(InventoryClickEvent event, ItemStack cursor, ItemStack currentItem) {
        if (!isCardlike(currentItem)) {
            event.setCancelled(true);
            return;
        }

        // Right-clicking on a bundle actually is a draw-one-thing-out behavior in vanilla minecraft
        // But for the sake of legibility, we'll reimplement it. It would really only be useful in the case of
        // drawing a card out of a card-stack with more than 2 cards in it and nothing in the cursor anyway

        if (cursor.getType() == Material.AIR) {
            ItemStack drawnCard = drawCardFromCurrentItem(event);
            event.setCursor(drawnCard);
            event.setCancelled(true);
        } else if (isCardlike(cursor)) {
            ItemStack newCursor = combineCardStacks(drawCardFromCurrentItem(event), cursor);
            event.setCursor(newCursor);
            event.setCancelled(true);
        } else {
            // We're holding a non-card, and we're drawing one - so this needs to only work if we're drawing from
            // a single card current item that will leave behind an empty slot for the current item to go into
            // not worth bothering to support legacy broken card stack behavior in this case
            if (isSingleCard(currentItem)) {
                event.setCurrentItem(cursor);
                event.setCursor(currentItem);
                event.setCancelled(true);
            } else {
                event.getWhoClicked().playSound(CARD_INTERACTION_FAILURE_SOUND);
                event.setCancelled(true);
            }
        }
    }

    private static ItemStack drawCardFromCurrentItem(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        ItemStack drawnCard;
        if (isSingleCard(currentItem)) {
            event.setCurrentItem(null);
            drawnCard = currentItem;
        } else {
            BundleMeta bundleMeta = (BundleMeta) currentItem.getItemMeta();
            drawnCard = bundleMeta.getItems().get(0);

            if (isLastCardInStack(bundleMeta)) {
                event.getWhoClicked().sendMessage("Warning: fixed a broken card stack that had 1 item in it!");
                event.setCurrentItem(null);
            } else if (bundleMeta.getItems().size() == 2) {
                event.setCurrentItem(bundleMeta.getItems().get(1));
            } else {
                bundleMeta.setItems(bundleMeta.getItems().subList(1, bundleMeta.getItems().size()));
                updateTopCardOfStack(bundleMeta);
                currentItem.setItemMeta(bundleMeta);
            }
        }
        return drawnCard;
    }

    private static void drawAllCards(InventoryClickEvent event, ItemStack cursor, ItemStack currentItem) {
        if (!isCardlike(currentItem)) return;

        if (isCardlike(cursor)) {
            event.setCursor(combineCardStacks(currentItem, cursor));
            event.setCurrentItem(null);
        } else if (cursor.getType() == Material.AIR) {
            event.setCursor(currentItem);
            event.setCurrentItem(null);
        } else {
            ItemStack tempCursor = event.getCursor();
            event.setCursor(currentItem);
            event.setCurrentItem(tempCursor);
        }
        event.setCancelled(true);
    }

    private static void onDoubleClickWhileHoldingCardlike(InventoryClickEvent event, ItemStack cursor) {
        List<ItemStack> allCardlikes = Stream.of(event.getWhoClicked().getInventory().getStorageContents())
                .filter(PlayingCardUtils::isCardlike)
                .toList();

        if (allCardlikes.isEmpty() && isCardStack(cursor)) {
            explodeHeldCardStack(event, cursor);
        } else {
            collectAllCardsToCursor(event, cursor, allCardlikes);
        }
        event.setCancelled(true);
    }

    private static void collectAllCardsToCursor(InventoryClickEvent event, ItemStack cursor, List<ItemStack> allCardlikes) {
        ItemStack allCardsInOneStack = allCardlikes.stream().reduce(cursor, PlayingCardUtils::combineCardStacks, PlayingCardUtils::combineCardStacks);
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

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (isCardlike(event.getOldCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRightClickWithBundleInCreative(PlayerInteractEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) return;
        if ( ! (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (!isCardlike(event.getItem())) return;
        Component warning = Component.text("You're in creative mode, you should be in ")
                            .append(Component.text("survival").decorate(TextDecoration.BOLD)
                                    .hoverEvent(Component.text("Click to be put in survival mode").color(NamedTextColor.RED).asHoverEvent())
                                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/gamemode survival")))
                            .append(Component.text(" for cards to work properly!"))
                            .color(NamedTextColor.RED);
        event.getPlayer().sendMessage(warning);
        event.getPlayer().playSound(CARD_INTERACTION_FAILURE_SOUND);
        event.setCancelled(true);
    }

    // TODO try PlayerInventorySlotChangeEvent for creative picking?

}
