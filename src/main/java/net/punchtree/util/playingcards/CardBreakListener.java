package net.punchtree.util.playingcards;

import net.kyori.adventure.text.Component;
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

import javax.annotation.Nullable;
import java.util.HashMap;

import static net.punchtree.util.playingcards.PlayingCardUtils.*;

@SuppressWarnings("UnstableApiUsage")
public class CardBreakListener implements Listener {

    @EventHandler
    public void beforeHangBreak(EntityDamageByEntityEvent event) {
        if ( !(event.getEntity() instanceof ItemFrame itemFrame)) return;
        if ( !(event.getDamager() instanceof Player player)) return;
        if ( !isCardOrCardStack(itemFrame.getItem())) return;

        event.setCancelled(true);

        onBreakCards(itemFrame, itemFrame.getItem(), player);
    }

    @EventHandler
    public void onHangBreak(HangingBreakEvent event) {
        if (!(event.getEntity() instanceof ItemFrame itemFrame)) return;
        if (!(isCardOrCardStack(itemFrame.getItem()))) return;

        event.setCancelled(true);
        ItemStack cardOrCardStack = itemFrame.getItem();

        Player player = null;
        if (event instanceof HangingBreakByEntityEvent entityEvent
                && entityEvent.getRemover() instanceof Player) {
            player = (Player) entityEvent.getRemover();
        }

        onBreakCards(itemFrame, cardOrCardStack, player);
    }

    /**
     *
     * @param itemFrame
     * @param cardOrCardStack
     * @param player null if broken by non-player
     */
    private void onBreakCards(ItemFrame itemFrame, ItemStack cardOrCardStack, @Nullable Player player) {
        if (player != null && player.isSneaking()) {
            flipCardOnGround(itemFrame);
            return;
        }

        ItemStack cardToDrop = cardOrCardStack;
        if (cardOrCardStack.getItemMeta() instanceof BundleMeta bundleMeta) {
            if (isFaceDownCardStack(cardOrCardStack)) {
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
            cardOrCardStack.setItemMeta(bundleMeta);
        } else {
            // Card is a single card, not a stack - shouldn't happen
            cardOrCardStack.editMeta(meta -> meta.displayName(PlayingCard.fromItem(cardOrCardStack).getName()));
        }

        if (player != null) {
            addItemToInventoryOrDrop(player, itemFrame, cardToDrop);
        } else {
            itemFrame.getWorld().dropItem(itemFrame.getLocation(), cardToDrop);
        }
        itemFrame.getWorld().playSound(itemFrame.getLocation(), Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1, 1);
        itemFrame.remove();
    }

    private void flipCardOnGround(ItemFrame itemFrame) {
        ItemStack flippedCard = flipCardOrCardStack(itemFrame.getItem());
        flippedCard.editMeta(meta -> meta.displayName(null));
        itemFrame.setItem(flippedCard);
    }

    private void addItemToInventoryOrDrop(Player player, ItemFrame itemFrame, ItemStack cardToDrop) {
        HashMap<Integer, ItemStack> overflowItems = player.getInventory().addItem(cardToDrop);
        if (!overflowItems.isEmpty()) {
            itemFrame.getWorld().dropItem(itemFrame.getLocation(), cardToDrop);
        }
    }

}
