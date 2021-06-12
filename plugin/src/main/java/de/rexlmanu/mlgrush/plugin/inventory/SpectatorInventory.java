package de.rexlmanu.mlgrush.plugin.inventory;

import de.rexlmanu.mlgrush.arenalib.ArenaTemplate;
import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.Arena;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class SpectatorInventory implements Listener, Runnable {

  private static Map<Character, ItemStack> PATTERN_ITEM = new HashMap<Character, ItemStack>() {{
    put('t', ItemStackBuilder.of(Material.STAINED_GLASS_PANE).name("&r").data(9).build());
    put('b', ItemStackBuilder.of(Material.STAINED_GLASS_PANE).name("&r").data(11).build());
  }};

  private static final char[][] PATTERN = {
    { 'b', 'b', 'b', 'b', 't', 'b', 'b', 'b', 'b' },
    { 'b', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'b' },
    { 't', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 't' },
    { 't', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 't' },
    { 'b', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'b' },
    { 'b', 'b', 'b', 'b', 't', 'b', 'b', 'b', 'b' },
  };

  private Inventory inventory;
  private List<ArenaDisplayItem> arenaDisplayItems;

  public SpectatorInventory() {
    this.inventory = Bukkit.createInventory(null, 6 * 9, ChatColor.YELLOW + "Spectator");
    this.arenaDisplayItems = new ArrayList<>();

    JavaPlugin plugin = GamePlugin.getProvidingPlugin(GamePlugin.class);
    Bukkit.getPluginManager().registerEvents(this, plugin);
    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 0, 1);

    this.createPattern();
  }

  @Override
  public void run() {
    if (this.inventory.getViewers().isEmpty()) return;

    this.arenaDisplayItems.forEach(arenaDisplayItem -> {
      arenaDisplayItem.itemStack(
        ItemStackBuilder.of(arenaDisplayItem.itemStack()).clearLore().lore(this.generateLore(arenaDisplayItem.arena())).build()
      );

      this.inventory.setItem(arenaDisplayItem.slot(), arenaDisplayItem.itemStack());
    });
  }

  @EventHandler
  public void handle(InventoryClickEvent event) {
    if (!this.inventory.equals(event.getClickedInventory()) || event.getCurrentItem() == null) return;
    event.setCancelled(true);
    if (!event.isLeftClick()) return;
    this.arenaDisplayItems.stream()
      .filter(arenaDisplayItem -> arenaDisplayItem.itemStack().equals(event.getCurrentItem())).findFirst()
      .ifPresent(arenaDisplayItem -> PlayerProvider
        .find(event.getWhoClicked().getUniqueId())
        .ifPresent(gamePlayer -> GameManager
          .instance()
          .arenaManager()
          .addSpectator(arenaDisplayItem.arena(), gamePlayer)));
  }

  public void open(Player player) {
    player.openInventory(this.inventory);
  }

  public void addArena(Arena arena) {
    this.arenaDisplayItems.add(new ArenaDisplayItem(arena, this.createDisplayItemStack(arena), this.getNextFreeSlot()));
  }

  public void remove(Arena arena) {
    this.arenaDisplayItems.stream()
      .filter(arenaDisplayItem -> arenaDisplayItem.arena().equals(arena))
      .findAny()
      .ifPresent(arenaDisplayItem -> {
        this.arenaDisplayItems.remove(arenaDisplayItem);
        this.reorderItems();
      });
  }

  private void reorderItems() {
    int index = 0;
    for (int slot = 0; slot < this.getInnerSlots().toArray().length; slot++) {
      if (this.arenaDisplayItems.size() <= index) {
        this.inventory.setItem(slot, null);
        continue;
      }
      ArenaDisplayItem displayItem = this.arenaDisplayItems.get(index);
      displayItem.slot(slot);
      this.inventory.setItem(slot, displayItem.itemStack());
    }
  }

  private IntStream getInnerSlots() {
    return IntStream.range(9, this.inventory.getSize() - 8).filter(value -> value % 9 != 0 && value % 8 != 0);
  }

  private int getNextFreeSlot() {
    for (int slot = 0; slot < this.inventory.getSize(); slot++) {
      if (this.inventory.getItem(slot) == null || Material.AIR.equals(this.inventory.getItem(slot).getType()))
        return slot;
    }
    return -1;
  }

  private ItemStack createDisplayItemStack(Arena arena) {
    ArenaTemplate template = arena.configuration().arenaTemplate();
    return ItemStackBuilder.of(Material.valueOf(template.displayMaterial().toUpperCase()))
      .name(template.name())
      .lore(this.generateLore(arena))
      .build();
  }

  private List<String> generateLore(Arena arena) {
    ArrayList<String> lore = new ArrayList<>();
    lore.add("");
    lore.add("&eSpieler&8:");
    arena.gameTeams().forEach(gameTeam -> {
      lore.add(String.format("&8- &7Team %s &8» %s%s &7Punkte", gameTeam.name().displayName(), gameTeam.name().color(), gameTeam.points()));
      gameTeam.members().stream().map(GamePlayer::player).map(HumanEntity::getName).forEach(s -> lore.add("  &8- &7" + s));
    });
    lore.add("");
    long seconds = (System.currentTimeMillis() - arena.gameStart()) / 1000;
    lore.add(String.format("&eSpiellänge&8: &7%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60));
    lore.add("");
    return lore;
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

  @AllArgsConstructor
  @Accessors(fluent = true)
  @Getter
  private class ArenaDisplayItem {
    private Arena arena;
    @Setter
    private ItemStack itemStack;
    @Setter
    private int slot;
  }
}
