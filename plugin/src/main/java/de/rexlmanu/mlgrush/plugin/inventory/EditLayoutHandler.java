package de.rexlmanu.mlgrush.plugin.inventory;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class EditLayoutHandler implements Listener {

  private static final ItemStack SAVE_ITEM = ItemStackBuilder.of(Material.INK_SACK).data(10).name("&8» &aSpeichern").build();
  private static final ItemStack REVERT_ITEM = ItemStackBuilder.of(Material.REDSTONE).name("&8» &cVorherige Einstellung behalten").build();
  private static final ItemStack ABORT_ITEM = ItemStackBuilder.of(Material.BARRIER).name("&8» &cAbbrechen").build();

  private static final Map<ItemStack, String> SORT_ITEMS = new HashMap<ItemStack, String>() {{
    put(ItemStackBuilder.of(Material.STICK).name("&8» &aStick").build(), "stick");
    put(ItemStackBuilder.of(Material.STONE_PICKAXE).name("&8» &aPickaxe").build(), "pickaxe");
    put(ItemStackBuilder.of(Material.SANDSTONE).name("&8» &aBlöcke").build(), "block");
  }};

  private GamePlayer gamePlayer;
  private Player player;

  public EditLayoutHandler(GamePlayer gamePlayer) {
    this.gamePlayer = gamePlayer;
    this.player = gamePlayer.player();

    Bukkit.getPluginManager().registerEvents(this, GamePlugin.getProvidingPlugin(GamePlugin.class));

    this.gamePlayer.sendMessage("Bitte öffne dein Inventar.");
    this.gamePlayer.sendMessage("Du kannst nun dein Inventar bearbeiten. Nachdem du deine gewünschte Anpassungen getroffen hast," +
      " nutze die jeweilige Items um die Aktion durchzuführen.");
    Player player = this.gamePlayer.player();
    player.playSound(player.getLocation(), Sound.FIRE_IGNITE, 1, 2f);
    player.setWalkSpeed(0);
    PlayerInventory inventory = player.getInventory();
    inventory.clear();
    inventory.setItem(35, ABORT_ITEM);
    inventory.setItem(21, SAVE_ITEM);
    inventory.setItem(23, REVERT_ITEM);
    for (int i = 0; i < this.gamePlayer.data().inventorySorting().size(); i++) {
      String key = this.gamePlayer.data().inventorySorting().get(i);
      if (key == null) continue;
      inventory.setItem(i, SORT_ITEMS
        .entrySet()
        .stream()
        .filter(itemStackStringEntry -> itemStackStringEntry.getValue().equals(key))
        .map(Map.Entry::getKey)
        .findAny()
        .orElse(null));
    }
  }

  public void unregister() {
    Player player = this.gamePlayer.player();
    player.closeInventory();
    HandlerList.unregisterAll(this);
    player.setWalkSpeed(0.2f);
    player.getInventory().clear();
    GameManager.instance().giveLobbyItems(player);
  }

  @EventHandler
  public void handle(InventoryClickEvent event) {
    if (event.getClickedInventory() == null) return;
    if (!event.getClickedInventory().equals(gamePlayer.player().getInventory())) {
      return;
    }
    if (!event.getWhoClicked().equals(this.gamePlayer.player())) return;
    int rawSlot = event.getRawSlot();
    event.setCancelled(true);
    ItemStack currentItem = event.getCurrentItem();
    if (currentItem != null) {
      if (currentItem.equals(ABORT_ITEM)) {
        this.gamePlayer.sendMessage("Du hast den Vorgang abgebrochen.");
        this.unregister();
        this.gamePlayer.sound(Sound.ANVIL_BREAK, 2f);
        return;
      }
      if (currentItem.equals(REVERT_ITEM)) {
        this.gamePlayer.sendMessage("Deine vorherigen Einstellungen werden behalten.");
        this.gamePlayer.sound(Sound.ORB_PICKUP, 1f);
        this.unregister();
        return;
      }
      if (currentItem.equals(SAVE_ITEM)) {
        this.save();
        return;
      }
    }
    if (rawSlot > 35 && rawSlot < 45) {
      event.setCancelled(false);
      event.setResult(Event.Result.ALLOW);
      return;
    }
    event.setResult(Event.Result.DENY);
    for (int i = 0; i < 9; i++) {
      if (gamePlayer.player().getInventory().getItem(i) == null) {
        gamePlayer.player().getInventory().setItem(i, event.getCursor());
        break;
      }
    }
    event.setCursor(null);
  }

  private void save() {
    PlayerInventory inventory = this.gamePlayer.player().getInventory();
    List<String> inventorySort = new ArrayList<>();
    for (int i = 0; i < 9; i++) {
      ItemStack item = inventory.getItem(i);
      inventorySort.add(item == null ? null : SORT_ITEMS.get(item));
    }
    if (inventorySort.size() == 9 && inventorySort.stream().filter(Objects::nonNull).count() == 3) {
      gamePlayer.data().inventorySorting(inventorySort);
      this.gamePlayer.sendMessage("Deine Anpassungen wurden gespeichert.");
      this.gamePlayer.sound(Sound.LEVEL_UP, 2f);
    } else {
      this.gamePlayer.sendMessage("Deine Anpassungen konnten nicht gespeichert werden, da sie fehlerhaft sind.");
    }
    this.unregister();
  }

  @EventHandler
  public void handle(InventoryCloseEvent event) {
    if (event.getInventory().equals(gamePlayer.player().getInventory())) {
      this.unregister();
    }
  }

  @EventHandler
  public void handle(PlayerQuitEvent event) {
    if (event.getPlayer().getUniqueId().equals(this.player.getUniqueId())) {
      this.unregister();
    }
  }

  @EventHandler
  public void handle(PlayerMoveEvent event) {
    if (!event.getPlayer().equals(this.gamePlayer.player())) return;
    Location to = event.getTo();
    Location from = event.getFrom();
    if (to.getX() != from.getX() || to.getZ() != from.getZ()) {
      to.setX(event.getFrom().getX());
      to.setZ(event.getFrom().getZ());
      return;
    }
  }
}
