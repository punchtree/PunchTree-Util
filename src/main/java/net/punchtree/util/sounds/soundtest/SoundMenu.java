package net.punchtree.util.sounds.soundtest;

import java.util.Arrays;

import net.punchtree.util.PunchTreeUtilPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

// TODO maybe should be a singleton
public class SoundMenu implements Listener {

	private static final String MENU_NAME = "Sound Testing Menu";
	
	private static final int ITEMS_PER_PAGE = 9*5;
	
	private static final int NUMBER_OF_PAGES = (int) Math.ceil((double) Sound.values().length / ITEMS_PER_PAGE);
	
	public static void openMenuFor(Player player, int page) {
		if (page < 1 || page > NUMBER_OF_PAGES) {
			player.sendMessage(ChatColor.RED + "Page 1 to " + NUMBER_OF_PAGES);
			return;
		}
		openToPage(player, page);
	}
	
	private static void openToPage(Player player, int page) {
		Inventory soundMenu = Bukkit.createInventory(null, 54, MENU_NAME);
		populatePage(soundMenu, page);
		player.openInventory(soundMenu);
	}
	
	private static void switchToPage(Inventory soundMenu, int page) {
		soundMenu.clear();
		populatePage(soundMenu, page);
	}
	
	private static void populatePage(Inventory soundMenu, int page) {
		if (page < 1 || page > NUMBER_OF_PAGES) return;
		int isi = 0;
		for(int i = ITEMS_PER_PAGE*(page-1); i < Math.min(Sound.values().length,  ITEMS_PER_PAGE*(page)); ++i) {
			ItemStack is = new ItemStack(Material.values()[(i+isi)%Material.values().length]);
			if (!is.getType().isItem() || is.getItemMeta() == null) {
				isi++;
				--i;
				continue;
			}
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(Sound.values()[i].name());
			is.setItemMeta(im);
			soundMenu.addItem(is);
		}
		ItemStack currPg = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
		ItemMeta currPgIm = currPg.getItemMeta();
		currPgIm.setDisplayName(String.valueOf(page));
		currPg.setItemMeta(currPgIm);
		soundMenu.setItem(45, currPg);
		if (page > 1) {
			ItemStack prev = new ItemStack(Material.ARROW);
			ItemMeta prevIm = prev.getItemMeta();
			prevIm.setDisplayName("Previous Page");
			prevIm.setLore(Arrays.asList(String.valueOf(page - 1)));
			prev.setItemMeta(prevIm);
			soundMenu.setItem(48, prev);
		}
		ItemStack close = new ItemStack(Material.BARRIER);
		ItemMeta closeIm = close.getItemMeta();
		closeIm.setDisplayName(ChatColor.RED + "Close");
		close.setItemMeta(closeIm);
		soundMenu.setItem(49, close);
		if (page < NUMBER_OF_PAGES) {
			ItemStack next = new ItemStack(Material.ARROW);
			ItemMeta nextIm = next.getItemMeta();
			nextIm.setDisplayName("Next Page");
			nextIm.setLore(Arrays.asList(String.valueOf(page + 1)));
			next.setItemMeta(nextIm);
			soundMenu.setItem(50, next);
		}
	}
	
	@EventHandler
	public void onMenuClick(InventoryClickEvent e) {
		if (!clickedInMenu(e)) return;
		if (! (e.getWhoClicked() instanceof Player)) return;
		Player player = (Player) e.getWhoClicked();
		e.setCancelled(true);
		int page = Integer.parseInt(e.getInventory().getItem(45).getItemMeta().getDisplayName());
		if (e.getSlot() == 48 && page > 1) {
			// PREVIOUS PAGE
			switchToPage(e.getInventory(), page-1);
			return;
		} else if (e.getSlot() == 49) {
			// CLOSE
			new BukkitRunnable() {
				public void run() {
					player.closeInventory();
				}
			}.runTaskLater(PunchTreeUtilPlugin.getInstance(), 1);
			return;
		} else if (e.getSlot() == 50 && page < NUMBER_OF_PAGES){
			// NEXT PAGE
			switchToPage(e.getInventory(), page+1);
			return;
		}
		if (!slotWasAValidSoundOnPage(e.getSlot(), page)) return;
		
		player.playSound(player.getLocation(), Sound.values()[e.getSlot() + ITEMS_PER_PAGE*(page-1)], 1, 1);
	}
	
	private boolean slotWasAValidSoundOnPage(int slot, int page) {
		if (page == NUMBER_OF_PAGES) {
			return slot < Sound.values().length % ITEMS_PER_PAGE;
		}
		return slot < ITEMS_PER_PAGE;
	}
	
	private boolean clickedInMenu(InventoryClickEvent e) {
		return e.getClickedInventory() != null
			&& e.getView().getTitle().startsWith(MENU_NAME);
	}
	
	//Prevent modifying menu
	@EventHandler
	private void onMenuDrag(InventoryDragEvent e){
		if(e.getView().getTitle().startsWith(MENU_NAME)){
			e.setCancelled(true);
		}
	}
	
}
