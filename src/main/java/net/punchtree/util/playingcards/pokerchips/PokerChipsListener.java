package net.punchtree.util.playingcards.pokerchips;

import net.kyori.adventure.text.Component;
import net.punchtree.util.PunchTreeUtilPlugin;
import net.punchtree.util.color.PunchTreeColor;
import org.bukkit.Material;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class PokerChipsListener implements Listener {

    private static final Material POKER_CHIPS_MATERIAL = Material.SUNFLOWER;
    private static final int POKER_CHIPS_1_CUSTOM_MODEL_DATA = 100;
    private static final int[] POKER_CHIPS_CUSTOM_MODEL_DATAS = {POKER_CHIPS_1_CUSTOM_MODEL_DATA};

    @EventHandler
    public void onPokerChipsPlace(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!isHoldingPokerChips(player)) return;

        event.setCancelled(true);

//        player.sendMessage("You placed poker chips!");
//        Interaction pokerChips = player.getWorld().spawn(player.getLocation(), Interaction.class, interaction -> {
//            interaction.setInteractionHeight(.5f);
//            interaction.setInteractionWidth(.6f);
//            PunchTreeColor.RED.getGlowingTeam().addEntry(interaction.getUniqueId().toString());
//            interaction.setGlowing(true);
//            interaction.customName(Component.text("Poker Chips"));
//        });
//
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                Interaction.PreviousInteraction lastRightClick = pokerChips.getLastInteraction();
//                if (lastRightClick == null) {
//                    player.sendMessage(" - No right click!");
//                } else {
//                    player.sendMessage(" - Last right player: " + lastRightClick.getPlayer().getName());
//                    player.sendMessage(" - Last right time: " + lastRightClick.getTimestamp());
//                }
//                Interaction.PreviousInteraction lastLeftClick = pokerChips.getLastAttack();
//                if (lastLeftClick == null) {
//                    player.sendMessage(" - No left click!");
//                } else {
//                    player.sendMessage(" - Last left player: " + lastLeftClick.getPlayer().getName());
//                    player.sendMessage(" - Last left time: " + lastLeftClick.getTimestamp());
//                }
//                pokerChips.remove();
//                player.sendMessage("Poker chips removed!");
//            }
//        }.runTaskLater(PunchTreeUtilPlugin.getInstance(), 20 * 5);
    }

    private boolean isHoldingPokerChips(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        return itemInHand.getType() == POKER_CHIPS_MATERIAL
                && Arrays.stream(POKER_CHIPS_CUSTOM_MODEL_DATAS).anyMatch(data -> itemInHand.getItemMeta().getCustomModelData() == data);
    }

}
