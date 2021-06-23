package de.rexlmanu.mlgrush.plugin.arena.inventory;

import de.rexlmanu.mlgrush.arenalib.ArenaTemplate;
import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.inventory.exception.NotEnoughPlayerException;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import de.rexlmanu.mlgrush.plugin.utility.RandomElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ArenaChoosingInventory implements Listener, Runnable {
  public static CompletableFuture<ArenaTemplate> create(List<GamePlayer> players) {
    return new ArenaChoosingInventory(players).future;
  }

  private static Map<Character, ItemStack> PATTERN_ITEM = new HashMap<Character, ItemStack>() {{
    put('t', ItemStackBuilder.of(Material.STAINED_GLASS_PANE).name("&r").data(13).build());
    put('b', ItemStackBuilder.of(Material.STAINED_GLASS_PANE).name("&r").data(5).build());
  }};

  private static final char[][] PATTERN = {
    { 'b', 'b', 'b', 'b', 't', 'b', 'b', 'b', 'b' },
    { 'b', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'b' },
    { 't', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 't' },
    { 't', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 't' },
    { 'b', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'b' },
    { 'b', 'b', 'b', 'b', 't', 'b', 'b', 'b', 'b' },
  };

  private List<VotedTemplate> votedTemplates = new ArrayList<>();

  private List<GamePlayer> players;
  private CompletableFuture<ArenaTemplate> future;
  private Inventory inventory;
  private int remainingSeconds = 3;
  private BukkitTask task;

  private ArenaChoosingInventory(List<GamePlayer> players) {
    this.players = players;
    this.future = new CompletableFuture<>();
    JavaPlugin plugin = GamePlugin.getProvidingPlugin(GamePlugin.class);
    this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 0, 20);
    this.inventory = Bukkit.createInventory(null, 6 * 9, MessageFormat.replaceColors("&8● &aMapauswahl"));

    Bukkit.getPluginManager().registerEvents(this, plugin);
    this.createPattern();
    this.createTemplateItems();

    this.players.forEach(gamePlayer -> gamePlayer.player().openInventory(this.inventory));
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

  private void createTemplateItems() {
    List<ArenaTemplate> templates = GameManager
      .instance()
      .arenaManager()
      .templateLoader()
      .templates();
    for (int i = 0; i < templates.size(); i++) {
      ArenaTemplate arenaTemplate = templates.get(i);
      int slot = i + 2;
      if (i > 4) slot += 4;
      ItemStack itemStack = this.createItem(arenaTemplate);
      this.inventory.setItem(slot + 9 + 9, itemStack);
      this.votedTemplates.add(new VotedTemplate(slot + 9 + 9, itemStack, arenaTemplate, new ArrayList<>()));
    }
  }

  private ItemStack createItem(ArenaTemplate template) {
    return ItemStackBuilder
      .of(Material.valueOf(template.displayMaterial().toUpperCase()))
      .name("&8» &a" + template.name())
      .amount(1)
      .lore("", "&7Builder: &a" + template.description(), "&7Votes: &a0")
      .build();
  }

  private void updateVotes() {
    this.votedTemplates.forEach(votedTemplate -> {
      votedTemplate.itemStack = ItemStackBuilder.of(votedTemplate.itemStack).clearLore().lore("", "&7Builder: &a" + votedTemplate.template.description(), "&7Votes: &a" + votedTemplate.voters.size()).build();
      this.inventory.setItem(votedTemplate.slot, votedTemplate.itemStack);
    });
  }

  private void unregister() {
    this.players.forEach(gamePlayer -> gamePlayer.player().closeInventory());
    HandlerList.unregisterAll(this);
  }

  @Override
  public void run() {
    if (this.remainingSeconds == 0) {
      this.task.cancel();
      VotedTemplate votedTemplate = this.getMostVotedTemplate();
      votedTemplate.itemStack = ItemStackBuilder.of(votedTemplate.itemStack).hideAttributes().enchant(Enchantment.DURABILITY, 1).build();
      this.inventory.setItem(votedTemplate.slot, votedTemplate.itemStack);
      this.players.forEach(gamePlayer -> gamePlayer.sound(Sound.LEVEL_UP, 1f));
      this.future.complete(votedTemplate.template);
      this.unregister();
      return;
    }
    this.players.forEach(gamePlayer -> gamePlayer.player().setLevel(this.remainingSeconds));
    this.remainingSeconds--;
  }

  @EventHandler
  public void handle(InventoryClickEvent event) {
    if (!this.inventory.equals(event.getClickedInventory())) return;
    event.setCancelled(true);
    event.setResult(Event.Result.DENY);

    if (event.getCurrentItem() == null)
      return;
    PlayerProvider.find(event.getWhoClicked().getUniqueId()).ifPresent(gamePlayer -> {
      this.votedTemplates.forEach(votedTemplate -> votedTemplate.voters().remove(gamePlayer));
      this.votedTemplates.stream().filter(votedTemplate -> votedTemplate.itemStack.equals(event.getCurrentItem())).findAny().ifPresent(votedTemplate -> {
        votedTemplate.voters.add(gamePlayer);
        gamePlayer.sound(Sound.ORB_PICKUP, 1.2f);
        this.updateVotes();
      });
    });
  }

  @EventHandler
  public void handle(PlayerQuitEvent event) {
    if (this.players.stream().noneMatch(gamePlayer -> gamePlayer.player().equals(event.getPlayer()))) {
      return;
    }
    this.future.completeExceptionally(new NotEnoughPlayerException());
    this.unregister();
  }

  private VotedTemplate getMostVotedTemplate() {
    if (votedTemplates.stream().map(votedTemplate -> votedTemplate.voters().size()).reduce(Integer::sum).get() == 0) {
      return RandomElement.of(this.votedTemplates);
    }
    return this.votedTemplates.stream().max(Comparator.comparingInt(o -> o.voters.size())).orElse(null);
  }

  @AllArgsConstructor
  @Getter
  @Accessors(fluent = true)
  public class VotedTemplate {
    private int slot;
    private ItemStack itemStack;
    private ArenaTemplate template;
    private List<GamePlayer> voters;
  }
}
