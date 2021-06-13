package de.rexlmanu.mlgrush.plugin.inventory;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.equipment.BlockEquipment;
import de.rexlmanu.mlgrush.plugin.equipment.StickEquipment;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class SettingsInventory implements Listener {
  private static Map<Character, ItemStack> PATTERN_ITEM = new HashMap<Character, ItemStack>() {{
    put('t', ItemStackBuilder.of(Material.STAINED_GLASS_PANE).name("&r").data(9).build());
    put('b', ItemStackBuilder.of(Material.STAINED_GLASS_PANE).name("&r").data(11).build());
  }};

  private static final char[][] PATTERN = {
    { 't', 'b', 't', 't', 'b', 't', 't', 'b', 't' },
    { 'b', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'b' },
    { 't', 'b', 't', 't', 'b', 't', 't', 'b', 't' }
  };

  private static final ItemStack INVENTORY_SORTING = ItemStackBuilder.of(Material.BUCKET).name("&eInventarsortierung anpassen").build();
  private static final ItemStack STICK_SHOP = ItemStackBuilder.of(Material.STICK).name("&eStickauswahl öffnen").build();
  private static final ItemStack BLOCK_SHOP = ItemStackBuilder.of(Material.SANDSTONE).name("&eBlockauswahl öffnen").build();
  private boolean unregistered = false;

  private GamePlayer gamePlayer;
  private Inventory inventory;

  public SettingsInventory(GamePlayer gamePlayer) {
    this.gamePlayer = gamePlayer;
    this.inventory = Bukkit.createInventory(null, 27, ChatColor.YELLOW + "Einstellungen");
    JavaPlugin plugin = GamePlugin.getProvidingPlugin(GamePlugin.class);
    Bukkit.getPluginManager().registerEvents(this, plugin);

    this.inventory.setItem(4 + 9, INVENTORY_SORTING);
    this.inventory.setItem(2 + 9, STICK_SHOP);
    this.inventory.setItem(6 + 9, BLOCK_SHOP);
    this.createPattern();

    this.gamePlayer.player().openInventory(this.inventory);
  }

  @EventHandler
  public void handle(InventoryClickEvent event) {
    if (!this.inventory.equals(event.getClickedInventory())) return;
    event.setCancelled(true);

    if (event.getCurrentItem() == null) {
      return;
    }
    if (INVENTORY_SORTING.equals(event.getCurrentItem())) {
      gamePlayer.player().closeInventory();
      gamePlayer.player().chat("/inv");
    }
    if (STICK_SHOP.equals(event.getCurrentItem())) {
      this.unregister();
      new ShopInventory(gamePlayer, "&eStick", StickEquipment.values()).open();
    }
    if (BLOCK_SHOP.equals(event.getCurrentItem())) {
      this.unregister();
      new ShopInventory(gamePlayer, "&eBlöcke", BlockEquipment.values()).open();
    }
  }

  @EventHandler
  public void handle(InventoryCloseEvent event) {
    if (event.getInventory().equals(this.inventory)) this.unregister();
  }

  private void unregister() {
    if (this.unregistered) return;
    HandlerList.unregisterAll(this);
    this.unregistered = true;
  }

  private void createPattern() {
    for (int x = 0; x < PATTERN.length; x++) {
      for (int y = 0; y < PATTERN[x].length; y++) {
        char code = PATTERN[x][y];
        if (code == 'x') continue;
        this.inventory.setItem(x * 9 + y, PATTERN_ITEM.get(code));
      }
    }
  }
}
