package de.rexlmanu.mlgrush.plugin.inventory.configuration;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.ArenaManager;
import de.rexlmanu.mlgrush.plugin.arena.configuration.ArenaConfiguration;
import de.rexlmanu.mlgrush.plugin.events.PlayerIngameEvent;
import de.rexlmanu.mlgrush.plugin.inventory.configuration.types.BooleanOptionItem;
import de.rexlmanu.mlgrush.plugin.inventory.configuration.types.IntegerOptionItem;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ArenaConfigurationInventory implements Listener {

  private static final ItemStack FINISH = ItemStackBuilder.of(Material.INK_SACK).name("&aAnfrage senden").data(10).build();

  private static Map<Character, ItemStack> PATTERN_ITEM = new HashMap<Character, ItemStack>() {{
    put('t', ItemStackBuilder.of(Material.STAINED_GLASS_PANE).name("&r").data(9).build());
    put('b', ItemStackBuilder.of(Material.STAINED_GLASS_PANE).name("&r").data(11).build());
    put('f', FINISH);
  }};

  private static final char[][] PATTERN = {
    { 'b', 'b', 'b', 'b', 't', 'b', 'b', 'b', 'b' },
    { 'b', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'b' },
    { 't', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 't' },
    { 'b', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'b' },
    { 'b', 'b', 'b', 'b', 'f', 'b', 'b', 'b', 'b' },
  };


  /*
  settings:
  max points: 1-30
  build height: 4 - 250
  nohitdelay, boolean
  block break, boolean
  knockbac nach oben nur noch, boolean

   */

  private GamePlayer owner;
  private GamePlayer target;

  private Inventory inventory;
  private List<OptionItem<?>> optionItems = new ArrayList<>();

  private OptionItem<Integer> maximalPointsOption = this.register(new IntegerOptionItem(
    ItemStackBuilder.of(Material.BED).name("&ePunkte").lore("&7Wie viele &ePunkte&7 braucht", "&7man um das Spiel zu-", "&7gewinnen.").build(),
    20, 10, 30, 1
  ));
  private OptionItem<Boolean> nohitdelayOption = this.register(new BooleanOptionItem(
    ItemStackBuilder.of(Material.ARROW).name("&eNoHitDelay").build(),
    21, false
  ));
  private OptionItem<Boolean> blockBreakOption = this.register(new BooleanOptionItem(
    ItemStackBuilder.of(Material.IRON_PICKAXE).name("&eAuto. Block-Entferner").lore("&7Platzierte Blöcke werden nach", "&7einigen Sekunden wieder &eentfernt&7.").build(),
    22, false
  ));
  private OptionItem<Integer> buildHeightOption = this.register(new IntegerOptionItem(
    ItemStackBuilder.of(Material.SANDSTONE).name("&eMaximale Bauhöhe").build(),
    23, 4, 250, 4
  ));
  private OptionItem<Boolean> knockbackOnlyHeightOption = this.register(new BooleanOptionItem(
    ItemStackBuilder.of(Material.LEATHER_BOOTS).color(Color.YELLOW).name("&eKnockback nur nach Oben").lore("&7Nur noch Knockback nach", "&eoben &7wird möglich sein.").build(),
    24, false
  ));

  public ArenaConfigurationInventory(GamePlayer owner, GamePlayer target) {
    this.owner = owner;
    this.target = target;
    this.inventory = Bukkit.createInventory(null, 5 * 9, ChatColor.YELLOW + "Spiel konfigurieren");

    Bukkit.getPluginManager().registerEvents(this, GamePlugin.getProvidingPlugin(GamePlugin.class));
    this.createPattern();
    this.optionItems.forEach(optionItem -> this.inventory.setItem(optionItem.slot(), optionItem.itemStack()));
    this.owner.player().openInventory(this.inventory);
  }

  @EventHandler
  public void handle(InventoryClickEvent event) {
    if (!this.inventory.equals(event.getClickedInventory())) return;

    event.setCancelled(true);

    this.optionItems
      .stream()
      .filter(optionItem -> optionItem.itemStack().equals(event.getCurrentItem()))
      .findAny()
      .ifPresent(optionItem -> optionItem.interact(event));

    if (FINISH.equals(event.getCurrentItem())) {
      this.sendRequest();
    }
  }

  private void sendRequest() {
    ArenaConfiguration.ArenaConfigurationBuilder configuration = ArenaManager.DEFAULT_CONFIGURATION.get().maximalPoints(this.maximalPointsOption.value())
      .autoBlockBreak(this.blockBreakOption.value())
      .buildHeight(this.buildHeightOption.value())
      .nohitdelay(nohitdelayOption.value())
      .knockbackOnlyHeight(this.knockbackOnlyHeightOption.value());

    this.owner.player().closeInventory();
    target.challengeRequests().put(this.owner.uniqueId(), configuration);
    target.sendMessage(String.format("Du wurdest von &e%s&7 zu einem eigenen Spiel gefordert mit &efolgenden &7Einstellungen:", this.owner.player().getName()));
    Stream.of("",
      String.format("  &8■ &7NoHitDelay &8× &e%s", this.nohitdelayOption.value() ? "&eaktiv" : "&7deaktiviert"),
      String.format("  &8■ &7Knockback nur Oben &8× &e%s", this.knockbackOnlyHeightOption.value() ? "&eaktiv" : "&7deaktiviert"),
      String.format("  &8■ &7Auto. Block-Entferner &8× &e%s", this.blockBreakOption.value() ? "&eaktiv" : "&7deaktiviert"),
      String.format("  &8■ &7Maximale Bauhöhe &8× &e%s", this.buildHeightOption.value()),
      String.format("  &8■ &7Benötigte Siegespunkte &8× &e%s", this.maximalPointsOption.value()),
      "").map(MessageFormat::replaceColors).forEach(s -> target.player().sendMessage(s));
    this.owner.sendMessage(String.format("Du hast &e%s&7 zu einem eigenen Spiel gefordert.", target.player().getName()));
    this.owner.sound(Sound.LEVEL_UP, 2f);
  }

  @EventHandler
  public void handle(InventoryCloseEvent event) {
    if (!this.inventory.equals(event.getInventory())) return;

    this.unregister();
  }

  @EventHandler
  public void handle(PlayerIngameEvent event) {
    if (!event.getPlayer().getUniqueId().equals(this.target.uniqueId())) return;
    this.owner.player().closeInventory();
    this.owner.sendMessage(String.format("Die Erstellung des Spiels wurde abgebrochen weil &e%s &7in ein Spiel gegangen ist.", this.target.player().getName()));
    this.owner.sound(Sound.ANVIL_LAND, 2f);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void handle(PlayerQuitEvent event) {
    if (!event.getPlayer().getUniqueId().equals(this.target.uniqueId())) return;
    this.owner.player().closeInventory();
    this.owner.sendMessage(String.format("Die Erstellung des Spiels wurde abgebrochen weil &e%s &7das Spiel verlassen hat.", this.target.player().getName()));
    this.owner.sound(Sound.ANVIL_LAND, 2f);
  }

  private <T> OptionItem<T> register(OptionItem<T> optionItem) {
    this.optionItems.add(optionItem);
    return optionItem;
  }

  private void unregister() {
    HandlerList.unregisterAll(this);
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
