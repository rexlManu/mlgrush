package de.rexlmanu.mlgrush.plugin.inventory;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.equipment.BlockEquipment;
import de.rexlmanu.mlgrush.plugin.equipment.Buyable;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

@Data
public class ShopInventory implements Listener {

  private static final Map<Character, ItemStack> PATTERN_ITEM =
      new HashMap<Character, ItemStack>() {
        {
          put('t', ItemStackBuilder.of(Material.LIME_STAINED_GLASS_PANE).name("&r").build());
          put('b', ItemStackBuilder.of(Material.GREEN_STAINED_GLASS_PANE).name("&r").build());
          put('c', ItemStackBuilder.of(Material.BARRIER).name("&8» &cSchließen").build());
        }
      };

  private static final ItemStack ABORT_ITEM =
      ItemStackBuilder.of(Material.RED_DYE).name("&8» &cAbbrechen").build();
  private static final ItemStack BUY_ITEM =
      ItemStackBuilder.of(Material.LIME_DYE).name("&8» &aErwerben").build();

  private static final char[][] PATTERN = {
    {'t', 'b', 't', 't', 'b', 't', 't', 'b', 't'},
    {'b', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'b'},
    {'t', 'b', 't', 't', 'c', 't', 't', 'b', 't'}
  };

  private Buyable currentBoughtItem;
  private BukkitTask animationTask;
  private int animationTick;

  private final GamePlayer gamePlayer;
  private final Buyable[] elements;
  private final Inventory inventory;

  public ShopInventory(GamePlayer gamePlayer, String title, Buyable[] elements) {
    this.gamePlayer = gamePlayer;
    this.elements = elements;
    this.inventory = Bukkit.createInventory(null, 27, MessageFormat.replaceColors(title));

    Bukkit.getPluginManager().registerEvents(this, GamePlugin.getProvidingPlugin(GamePlugin.class));

    for (int x = 0; x < PATTERN.length; x++) {
      for (int y = 0; y < PATTERN[x].length; y++) {
        char code = PATTERN[x][y];
        if (code == 'x') {
          continue;
        }
        this.inventory.setItem(x * 9 + y, PATTERN_ITEM.get(code));
      }
    }

    this.setElementItems();
    this.open();
  }

  private void setElementItems() {
    this.inventory.setItem(13, null);
    this.inventory.setItem(11, null);
    this.inventory.setItem(15, null);
    for (int i = 0; i < this.elements.length; i++) {
      int slot = i + 10;
      if (i > 2) {
        slot++;
      }
      this.inventory.setItem(slot, this.createItem(this.elements[i]));
    }
  }

  private ItemStack createItem(Buyable buyable) {
    ItemStackBuilder builder =
        ItemStackBuilder.of(buyable.material()).name("&8» " + buyable.displayName());
    if (buyable.cost() > 0) {
      if (this.owns(buyable)) {
        builder.lore("", "&a&lIN BESITZ");
      } else {
        builder.lore("", String.format("&7Kosten: &a%s Coins", buyable.cost()));
      }
    }
    if (buyable.name().toLowerCase().equals(this.gamePlayer.data().selectedStick())
        || buyable.name().toLowerCase().equals(this.gamePlayer.data().selectedBlock())) {
      builder.enchant(Enchantment.UNBREAKING, 1);
      builder.hideAttributes();
    }
    return builder.build();
  }

  private boolean owns(Buyable buyable) {
    return this.gamePlayer.player().hasPermission(buyable.permission())
        || this.gamePlayer.data().boughtItems().contains(buyable.name().toLowerCase())
        || buyable.cost() == 0;
  }

  private void open() {
    Player player = this.gamePlayer.player();
    player.openInventory(this.inventory);
    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.7f, 1.3f);
  }

  @EventHandler
  public void handle(InventoryClickEvent event) {
    if (event.getClickedInventory() == null
        || !event.getClickedInventory().equals(this.inventory)) {
      return;
    }

    event.setCancelled(true);
    event.setResult(Event.Result.DENY);
    Player player = (Player) event.getWhoClicked();

    ItemStack item = event.getCurrentItem();
    if (item == null) {
      return;
    }
    if (item.getType() == Material.BARRIER) {
      player.closeInventory();
      player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.3f);
      return;
    }
    if (item.getType() == Material.LIME_STAINED_GLASS_PANE
        || item.getType() == Material.GREEN_STAINED_GLASS_PANE) {
      return;
    }
    if (item.isSimilar(BUY_ITEM)) {
      if (this.currentBoughtItem == null) {
        return;
      }
      if (this.gamePlayer.data().coins() < this.currentBoughtItem.cost()) {
        this.gamePlayer.sendMessage(
            String.format(
                "Dir fehlen noch &a%s &7Coins dafür.",
                this.currentBoughtItem.cost() - this.gamePlayer.data().coins()));
        return;
      }
      this.gamePlayer.data().coins(this.gamePlayer.data().coins() - this.currentBoughtItem.cost());
      this.gamePlayer.data().boughtItems().add(this.currentBoughtItem.name().toLowerCase());
      this.gamePlayer.sendMessage(
          String.format(
              "Du hast erfolgreich folgendes erworben: %s", this.currentBoughtItem.displayName()));
      player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.7f);
      this.setElementItems();
      this.currentBoughtItem = null;
      return;
    }
    if (item.isSimilar(ABORT_ITEM)) {
      player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1f, 1.4f);
      this.setElementItems();
      this.currentBoughtItem = null;
      return;
    }
    if (this.currentBoughtItem != null) {
      return;
    }
    Arrays.stream(this.elements)
        .filter(buyable -> buyable.material().equals(item.getType()))
        .findAny()
        .ifPresent(
            buyable -> {
              if (this.owns(buyable)) {
                if (buyable.name().toLowerCase().equals(this.gamePlayer.data().selectedStick())
                    || buyable
                        .name()
                        .toLowerCase()
                        .equals(this.gamePlayer.data().selectedBlock())) {
                  return;
                }
                if (buyable instanceof BlockEquipment) {
                  this.gamePlayer.data().selectedBlock(buyable.name().toLowerCase());
                } else {
                  this.gamePlayer.data().selectedStick(buyable.name().toLowerCase());
                }
                this.gamePlayer.sendMessage(
                    String.format("Du hast folgendes ausgerüstet: %s", buyable.displayName()));
                player.playSound(
                    player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.4f);
                this.setElementItems();
                return;
              }
              if (this.animationTick != 0) {
                return;
              }

              this.currentBoughtItem = buyable;
              this.animationTick = 0;

              this.animationTask =
                  Bukkit.getScheduler()
                      .runTaskTimer(
                          GamePlugin.getProvidingPlugin(GamePlugin.class),
                          () -> {
                            if (this.animationTick > 6) {
                              this.animationTick = 0;
                              this.animationTask.cancel();
                              this.inventory.setItem(13, this.createItem(buyable));
                              this.inventory.setItem(11, ABORT_ITEM);
                              this.inventory.setItem(15, BUY_ITEM);
                              player.playSound(
                                  player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 0.8f, 1f);
                              return;
                            }
                            this.inventory.setItem(this.animationTick + 10, null);
                            player.playSound(
                                player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 0.6f, 2f);
                            this.animationTick++;
                          },
                          0L,
                          1L);
            });
  }

  @EventHandler
  public void handle(InventoryCloseEvent event) {
    if (!event.getInventory().equals(this.inventory)) {
      return;
    }
    if (this.animationTask != null) {
      this.animationTask.cancel();
    }
    HandlerList.unregisterAll(this);
  }
}
