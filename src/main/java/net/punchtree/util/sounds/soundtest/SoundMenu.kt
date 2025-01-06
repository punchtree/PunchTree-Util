package net.punchtree.util.sounds.soundtest

import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.set.RegistrySet
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.punchtree.util.PunchTreeUtilPlugin.Companion.instance
import net.punchtree.util.extensionmethods.textContent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Registry
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.stream.Collectors
import kotlin.math.ceil
import kotlin.math.min

// TODO maybe should be a singleton
@Suppress("UnstableApiUsage")
class SoundMenu : Listener {

    @EventHandler
    fun onMenuClick(e: InventoryClickEvent) {
        if (!clickedInMenu(e)) return
        if (e.whoClicked !is Player) return
        val player = e.whoClicked as Player
        e.isCancelled = true
        val page = e.inventory.getItem(45)!!.itemMeta.displayName()!!.textContent().toInt()
        if (e.slot == 48 && page > 1) {
            // PREVIOUS PAGE
            switchToPage(e.inventory, page - 1)
            return
        } else if (e.slot == 49) {
            // CLOSE
            object : BukkitRunnable() {
                override fun run() {
                    player.closeInventory()
                }
            }.runTaskLater(instance, 1)
            return
        } else if (e.slot == 50 && page < getNumberOfPages(getSounds())) {
            // NEXT PAGE
            switchToPage(e.inventory, page + 1)
            return
        }

        if (e.slot >= ITEMS_PER_PAGE) return  // didn't click a sound

        if (e.currentItem == null) return  // didn't click a sound (last page only)


        val clickedItem = e.currentItem
        val soundName = clickedItem!!.itemMeta.displayName()!!.textContent()
        val soundKey = TypedKey.create(RegistryKey.SOUND_EVENT, Key.key(soundName))
        val sound = Registry.SOUNDS[soundKey]

        if (sound == null) {
            player.sendMessage(
                text("There was an error and cannot figure out what sound to play!").color(
                    RED
                )
            )
            return
        }

        player.playSound(player.location, sound, 1f, 1f)
    }

    private fun clickedInMenu(e: InventoryClickEvent): Boolean {
        return (e.clickedInventory != null
                && e.view.title().contains(MENU_NAME))
    }

    //Prevent modifying menu
    @EventHandler
    private fun onMenuDrag(e: InventoryDragEvent) {
        if (e.view.title().contains(MENU_NAME)) {
            e.isCancelled = true
        }
    }

    companion object {
        private val MENU_NAME = text("Sound Testing Menu")

        private const val ITEMS_PER_PAGE = 9 * 5

        private val ALL_ITEMS = Material.entries.stream().filter(Material::isItem).collect(Collectors.toList())

        private fun getNumberOfPages(sounds: Array<TypedKey<Sound>>): Int {
            return ceil(sounds.size.toDouble() / ITEMS_PER_PAGE)
                .toInt()
        }

        @JvmStatic
		fun openMenuFor(player: Player, page: Int) {
            val numberOfPages = getNumberOfPages(getSounds())
            if (page < 1 || page > numberOfPages) {
                player.sendMessage(text("Page 1 to $numberOfPages").color(RED))
                return
            }
            openToPage(player, page)
        }

        private fun openToPage(player: Player, page: Int) {
            val soundMenu = Bukkit.createInventory(null, 54, MENU_NAME)
            populatePage(soundMenu, page)
            player.openInventory(soundMenu)
        }

        private fun switchToPage(soundMenu: Inventory, page: Int) {
            soundMenu.clear()
            populatePage(soundMenu, page)
        }

        private fun populatePage(soundMenu: Inventory, page: Int) {

            val sounds: Array<TypedKey<Sound>> = getSounds()

            if (page < 1 || page > getNumberOfPages(sounds)) return
            var materialCounter = (ITEMS_PER_PAGE * (page - 1)) % ALL_ITEMS.size
            for (i in ITEMS_PER_PAGE * (page - 1) until min(
                sounds.size.toDouble(),
                (ITEMS_PER_PAGE * (page)).toDouble()
            ).toInt()) {

                var itemStack: ItemStack
                var material: Material
                do {
                    do {
                        while (materialCounter >= ALL_ITEMS.size) {
                            materialCounter -= ALL_ITEMS.size
                        }
                        material = ALL_ITEMS[materialCounter]
                        ++materialCounter
                    } while (!material.isItem)
                    itemStack = ItemStack.of(material)
                } while (itemStack.itemMeta == null)

                val im = itemStack.itemMeta
                val soundKey = sounds[i]
                im.displayName(text(soundKey.asMinimalString()))
                itemStack.setItemMeta(im)
                soundMenu.addItem(itemStack)
            }
            val currPg = ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
            val currPgIm = currPg.itemMeta
            currPgIm.displayName(text(page))
            currPg.setItemMeta(currPgIm)
            soundMenu.setItem(45, currPg)
            if (page > 1) {
                val prev = ItemStack(Material.ARROW)
                val prevIm = prev.itemMeta
                prevIm.displayName(text("Previous Page"))
                prevIm.lore(listOf(text(page - 1)))
                prev.setItemMeta(prevIm)
                soundMenu.setItem(48, prev)
            }
            val close = ItemStack(Material.BARRIER)
            val closeIm = close.itemMeta
            closeIm.displayName(text("Close").color(RED))
            close.setItemMeta(closeIm)
            soundMenu.setItem(49, close)
            if (page < getNumberOfPages(sounds)) {
                val next = ItemStack(Material.ARROW)
                val nextIm = next.itemMeta
                nextIm.displayName(text("Next Page"))
                nextIm.lore(listOf(text(page + 1)))
                next.setItemMeta(nextIm)
                soundMenu.setItem(50, next)
            }
        }

        private fun getSounds() = RegistrySet.keySetFromValues(
            RegistryKey.SOUND_EVENT,
            Registry.SOUNDS
        ).values().toTypedArray()
    }
}
