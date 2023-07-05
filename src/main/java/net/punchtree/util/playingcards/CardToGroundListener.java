package net.punchtree.util.playingcards;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockSupport;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import static net.punchtree.util.playingcards.PlayingCardUtils.*;

@SuppressWarnings("UnstableApiUsage")
public class CardToGroundListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // On right click on block with a playing card in hand
        // if there's not an item frame there, create one and place a new card stack with the card in it
        ItemStack itemInHand = event.getItem();
        if (!isCardOrCardStack(itemInHand)) return;

        // Cancel all events while holding cards or decks, even if they don't cause a card related action
        event.setCancelled(true);

        if ( event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        BlockFace clickedFace = event.getBlockFace();
        EquipmentSlot hand = event.getHand();

//        if (player.isSneaking()) {
        tryPlacingCardOrCardStack(hand, itemInHand, player, clickedBlock, clickedFace);
//        } else {
//            drawCard();
//            if (isHoldingDeck(player, hand)) {
//                addCardToHeldDeck();
//            }
//        }
    }

    private void tryPlacingCardOrCardStack(EquipmentSlot hand, ItemStack itemInHand, Player player, Block clickedBlock, BlockFace clickedFace) {
        if (!canBlockFaceHaveCardPlacedOnIt(clickedBlock, clickedFace)) return;


        ItemFrame frame = clickedBlock.getWorld().spawn(clickedBlock.getRelative(clickedFace).getLocation(), ItemFrame.class, frameBeforeSpawn -> {
            frameBeforeSpawn.setFacingDirection(clickedFace, true);
            frameBeforeSpawn.setVisible(false);
            frameBeforeSpawn.setRotation(getRotationForYaw(player.getLocation().getYaw()));
//            frameBeforeSpawn.setFixed(true);
            if (isFaceUpCardStack(itemInHand) && player.isSneaking()) {
                BundleMeta meta = (BundleMeta) itemInHand.getItemMeta();
                ItemStack itemToBeInFrame = PlayingCard.fromItem(meta.getItems().get(0)).getNewPileItem();
                itemToBeInFrame.editMeta(itemToBeInFrameMeta -> itemToBeInFrameMeta.displayName(null));
                frameBeforeSpawn.setItem(itemToBeInFrame);
                if (isLastCardInStack(meta)) {
                    player.getInventory().setItem(hand, null);
                } else if (meta.getItems().size() == 2) {
                    player.getInventory().setItem(hand, meta.getItems().get(1));
                } else {
                    meta.setItems(meta.getItems().subList(1, meta.getItems().size()));
                    updateTopCardOfStack(meta);
                    itemInHand.setItemMeta(meta);
                }
            } else if (isFaceDownCardStack(itemInHand) && player.isSneaking()) {
                BundleMeta meta = (BundleMeta) itemInHand.getItemMeta();
                ItemStack itemToBeInFrame = flipCardOrCardStack(PlayingCard.fromItem(meta.getItems().get(0)).getNewPileItem());
                itemToBeInFrame.editMeta(itemToBeInFrameMeta -> itemToBeInFrameMeta.displayName(null));
                frameBeforeSpawn.setItem(itemToBeInFrame);
                if (isLastCardInStack(meta)) {
                    player.getInventory().setItem(hand, null);
                } else if (meta.getItems().size() == 2) {
                    player.getInventory().setItem(hand, flipCardOrCardStack(meta.getItems().get(1)));
                } else {
                    meta.setItems(meta.getItems().subList(1, meta.getItems().size()));
                    itemInHand.setItemMeta(meta);
                }
            } else if (isFaceDownCard(itemInHand)) {
//                ItemStack itemToBeInFrame = PlayingCard.getNewFaceDownPileItem();
//                itemToBeInFrame.editMeta(meta -> {
//                    BundleMeta bundleMeta = (BundleMeta) meta;
//                    meta.displayName(null);
//                    bundleMeta.addItem(flipCardOrCardStack(itemInHand));
//                });

                // This implementation would be for if a single card is represented using a different item than a card stack
                ItemStack itemToBeInFrame = itemInHand;
                itemToBeInFrame.editMeta(meta -> {
                    meta.displayName(null);
                });

                frameBeforeSpawn.setItem(itemToBeInFrame);
                player.getInventory().setItem(hand, null);
            } else {
//                ItemStack itemToBeInFrame = isCardStack(itemInHand) ? itemInHand : PlayingCard.fromItem(itemInHand).getNewPileItem();
                ItemStack itemToBeInFrame = itemInHand;
                itemToBeInFrame.editMeta(meta -> meta.displayName(null));
                frameBeforeSpawn.setItem(itemToBeInFrame);
                player.getInventory().setItem(hand, null);
            }
        });
    }

    private boolean canBlockFaceHaveCardPlacedOnIt(Block clickedBlock, BlockFace clickedFace) {
        if (!clickedBlock.getBlockData().isFaceSturdy(clickedFace, BlockSupport.CENTER)) return false;
        Material materialWhereCardWillBe = clickedBlock.getRelative(clickedFace).getType();
        boolean isClickedFaceUnobstructed = materialWhereCardWillBe == Material.AIR || materialWhereCardWillBe.name().contains("DOOR") || materialWhereCardWillBe.name().contains("PANE");
        return isClickedFaceUnobstructed && !blockFaceHasItemFrame(clickedBlock, clickedFace);
    }

    // TODO see if cards can skip the manual conversion from yaw to rotation
    private Rotation getRotationForYaw(double yaw) {
        if (Math.abs(yaw) > 135) return Rotation.NONE;
        else if (Math.abs(yaw) < 45) return Rotation.FLIPPED;
        else if (yaw < 0) return Rotation.CLOCKWISE;
        else return Rotation.COUNTER_CLOCKWISE;
    }

    private boolean blockFaceHasItemFrame(Block clickedBlock, BlockFace clickedFace) {
        return clickedBlock.getLocation().getNearbyEntities(2, 2, 2).stream()
//                .peek(entity -> {
//                    Bukkit.getPlayer("Cxom").sendMessage("====================================");
//                    Bukkit.getPlayer("Cxom").sendMessage("Entity: " + entity.getType().name());
//                    Bukkit.getPlayer("Cxom").sendMessage("Entity location: " + entity.getLocation());
//                    Bukkit.getPlayer("Cxom").sendMessage("Relatv location: " + clickedBlock.getRelative(clickedFace).getLocation());
//                    if (entity instanceof ItemFrame itemFrame) {
//                        Bukkit.getPlayer("Cxom").sendMessage("Item frame facing: " + itemFrame.getFacing().name());
//                    }
//                })
                .filter(entity -> entity instanceof ItemFrame)
                .map(entity -> (ItemFrame) entity)
                .anyMatch(itemFrame -> {
                    Location location = itemFrame.getLocation();
                    Block newFrameBlock = clickedBlock.getRelative(clickedFace);
                    return location.getBlockX() == newFrameBlock.getX()
                            && location.getBlockY() == newFrameBlock.getY()
                            && location.getBlockZ() == newFrameBlock.getZ();
//                            && itemFrame.getFacing() == clickedFace;
                });
    }

}
