package net.punchtree.util.playingcards;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.punchtree.util.PunchTreeUtilPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class PlayingCardUtils {

    private static final NamespacedKey PLAYING_CARDS_KEY = new NamespacedKey(PunchTreeUtilPlugin.getInstance(), "playing_cards");

    private static final int PLAYING_CARD_MIN_CUSTOM_MODEL_DATA = 1001;
    private static final int PLAYING_CARD_MAX_CUSTOM_MODEL_DATA = 1052;

    private static final int PLAYING_CARD_STACK_MIN_CUSTOM_MODEL_DATA = 1001;
    private static final int PLAYING_CARD_STACK_MAX_CUSTOM_MODEL_DATA = 1052;
    static final String PUNCHTREE_CARDS_LEFT_CLICK_PLACE_FLAG = "punchtree:cards:left-click-place";

    static boolean isCardlike(ItemStack item) {
        return isFaceUpCard(item) || isFaceUpCardStack(item) || isFaceDownCard(item) || isFaceDownCardStack(item);
    }

    static boolean isSingleCard(ItemStack cardlikeToAdd) {
        return isFaceUpCard(cardlikeToAdd) || isFaceDownCard(cardlikeToAdd);
    }

    static boolean isCardStack(ItemStack cardlikeToAdd) {
        return isFaceUpCardStack(cardlikeToAdd) || isFaceDownCardStack(cardlikeToAdd);
    }

    static boolean isFaceUpCardStack(ItemStack item) {
        return item != null
                && item.getType() == PlayingCard.PLAYING_CARD_STACK_MATERIAL
                && item.hasItemMeta()
                && item.getItemMeta().hasCustomModelData()
                && item.getItemMeta().getCustomModelData() >= PLAYING_CARD_STACK_MIN_CUSTOM_MODEL_DATA
                && item.getItemMeta().getCustomModelData() <= PLAYING_CARD_STACK_MAX_CUSTOM_MODEL_DATA;
    }

    static boolean isFaceUpCard(ItemStack item) {
        return item != null
                && item.getType() == PlayingCard.PLAYING_CARD_MATERIAL
                && item.hasItemMeta()
                && item.getItemMeta().hasCustomModelData()
                && item.getItemMeta().getCustomModelData() >= PLAYING_CARD_MIN_CUSTOM_MODEL_DATA
                && item.getItemMeta().getCustomModelData() <= PLAYING_CARD_MAX_CUSTOM_MODEL_DATA;
    }

    static boolean isFaceDownCard(ItemStack item) {
        return item != null &&
                item.getType() == PlayingCard.PLAYING_CARD_MATERIAL
                && item.hasItemMeta()
                && item.getItemMeta().hasCustomModelData()
                && item.getItemMeta().getCustomModelData() == PlayingCard.CARD_BACK_CUSTOM_MODEL_DATA
                && item.getItemMeta().getPersistentDataContainer().has(PLAYING_CARDS_KEY);
    }

    static boolean isFaceDownCardStack(ItemStack item) {
        return item != null &&
                item.getType() == PlayingCard.PLAYING_CARD_STACK_MATERIAL
                && item.hasItemMeta()
                && item.getItemMeta().hasCustomModelData()
                && item.getItemMeta().getCustomModelData() == PlayingCard.CARD_BACK_CUSTOM_MODEL_DATA;
    }

    static void updateTopCardOfStack(BundleMeta bundleMeta) {
        if (bundleMeta.getCustomModelData() == PlayingCard.CARD_BACK_CUSTOM_MODEL_DATA) return;

        ItemMeta topCardMeta = bundleMeta.getItems().get(0).getItemMeta();
        bundleMeta.setCustomModelData(topCardMeta.getCustomModelData());
        bundleMeta.displayName(topCardMeta.displayName());
    }

    static boolean isLastCardInStack(BundleMeta bundleMeta) {
        return bundleMeta.getItems().size() == 1;
    }

    static ItemStack flipCardlike(ItemStack item) {
        if (isFaceDownCard(item)) {
            return flipFaceDownCard(item);
        } else if (isFaceDownCardStack(item)) {
            item.editMeta(meta -> {
                BundleMeta bundleMeta = (BundleMeta) meta;
                ItemStack topCard = flipFaceDownCard(bundleMeta.getItems().get(0));
                meta.setCustomModelData(topCard.getItemMeta().getCustomModelData());
                meta.displayName(PlayingCard.fromItem(topCard).getName());
                bundleMeta.setItems(bundleMeta.getItems().stream().map(PlayingCardUtils::flipFaceDownCard).collect(Collectors.toList()));
            });
            return item;
        } else if (isFaceUpCard(item)) {
            return flipFaceUpCard(item);
        } else if (isFaceUpCardStack(item)) {
            item.editMeta(meta -> {
                meta.setCustomModelData(PlayingCard.CARD_BACK_CUSTOM_MODEL_DATA);
                BundleMeta bundleMeta = (BundleMeta) meta;
                bundleMeta.setItems(bundleMeta.getItems().stream().map(PlayingCardUtils::flipFaceUpCard).collect(Collectors.toList()));
                bundleMeta.displayName(PlayingCard.FACE_DOWN_CARD_PILE_NAME);
            });

//            ItemStack faceDownPile = PlayingCard.getNewFaceDownPileItem();
//            List<PlayingCard> cards = ((BundleMeta) item.getItemMeta()).getItems().stream().map(PlayingCard::fromItem).collect(Collectors.toList());
//            writeCardsToPdc(faceDownPile, cards);
            return item;
        } else {
            return item;
        }
    }

    @Nullable
    private static ItemStack flipFaceDownCard(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<PlayingCard> cards = meta.getPersistentDataContainer().get(PLAYING_CARDS_KEY, new CardStackPersistentDataType());
        return PlayingCard.getItemForCardList(cards);
    }

    @NotNull
    private static ItemStack flipFaceUpCard(ItemStack item) {
        ItemStack faceDownCard = PlayingCard.getNewFaceDownCardItem();
        writeCardToPdc(faceDownCard, PlayingCard.fromItem(item));
        return faceDownCard;
    }

    static void writeCardToPdc(ItemStack faceDownCard, PlayingCard fromItem) {
        writeCardsToPdc(faceDownCard, List.of(fromItem));
    }

    static void writeCardsToPdc(ItemStack faceDownCard, List<PlayingCard> cards) {
        faceDownCard.editMeta(meta -> {
            meta.getPersistentDataContainer().set(PLAYING_CARDS_KEY, new CardStackPersistentDataType(), cards);
        });
    }

    static ItemStack combineCardStacks(ItemStack topCards, ItemStack bottomCards) {

        // We combine in the order top then bottom, but use the face-up status of the bottom

        ItemStack combinedStack = bottomCards;
        if (isSingleCard(combinedStack)) {
            combinedStack = PlayingCard.fromItem(combinedStack).getNewPileItem();
            // Result of newPileItem will always be face up, so flip it back the way it was
            combinedStack = isFaceUpCard(bottomCards) ? combinedStack : flipCardlike(combinedStack);
        }

        BundleMeta combinedStackMeta = (BundleMeta) combinedStack.getItemMeta();
        Stream<ItemStack> bottomCardsStream = combinedStackMeta.getItems().stream();

        ItemStack faceUpTopCards =
                isFaceUpCard(topCards) || isFaceUpCardStack(topCards) ? topCards :
                flipCardlike(topCards);

        ItemStack facedCorrectlyTopCards = isFaceUpCardStack(combinedStack) ?  faceUpTopCards : flipCardlike(faceUpTopCards);

        Stream<ItemStack> topCardsStream = isSingleCard(facedCorrectlyTopCards) ? Stream.of(facedCorrectlyTopCards) : ((BundleMeta) facedCorrectlyTopCards.getItemMeta()).getItems().stream();
        List<ItemStack> combinedCardsList = Stream.concat(topCardsStream, bottomCardsStream).toList();

        combinedStackMeta.setItems(combinedCardsList);

        if (isFaceUpCardStack(combinedStack)) {
            updateTopCardOfStack(combinedStackMeta);
        }

        combinedStack.setItemMeta(combinedStackMeta);
        return combinedStack;
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
