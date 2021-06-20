package de.rexlmanu.mlgrush.arenacreator.process;

import de.rexlmanu.mlgrush.arenacreator.ArenaCreatorPlugin;
import de.rexlmanu.mlgrush.arenacreator.Constants;
import de.rexlmanu.mlgrush.arenacreator.utility.ItemStackBuilder;
import de.rexlmanu.mlgrush.arenacreator.utility.MessageFormat;
import de.rexlmanu.mlgrush.arenacreator.utility.ParticleEffect;
import de.rexlmanu.mlgrush.arenalib.*;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Accessors(fluent = true)
@Getter
public class ArenaCreationProcess implements Listener, Runnable {

  private static final ItemStack SELECT_FIRST_LOCATION = ItemStackBuilder.of(Material.STICK).name("&aErste Position markieren &7<Rechtsklick>").build();
  private static final ItemStack SELECT_SECOND_LOCATION = ItemStackBuilder.of(Material.BLAZE_ROD).name("&aZweite Position markieren &7<Rechtsklick>").build();
  private static final ItemStack SELECT_BLUE_TEAM = ItemStackBuilder.of(Material.WOOL).data(11).name("&aSpawn für Team Blau &7<Rechtsklick>").build();
  private static final ItemStack SELECT_RED_TEAM = ItemStackBuilder.of(Material.WOOL).data(14).name("&aSpawn für Team Rot &7<Rechtsklick>").build();
  private static final ItemStack BUILD_HEIGHT = ItemStackBuilder.of(Material.SANDSTONE).data(14).name("&aStartbauhöhe &7<Rechtsklick>").build();

  private final Map<String, Location> locationMap = new HashMap<>();
  private boolean waitForInput = false;
  private String name, description, material;
  private int red = 255, green = 0, blue = 0;

  private BukkitTask task;

  private final Player player;

  public ArenaCreationProcess(Player player) {
    this.player = player;

    JavaPlugin plugin = ArenaCreatorPlugin.getProvidingPlugin(ArenaCreatorPlugin.class);
    Bukkit.getPluginManager().registerEvents(this, plugin);
    this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 0, 1);

    this.player.sendMessage(Constants.PREFIX + "Bitte markiere erst die Arena mittels zwei Punkte.");
    this.player.sendMessage(Constants.PREFIX + "Dafür bitte stelle dich an die gewünschte Position and rechtsklicke das Item in deiner Hotbar.");
    this.player.sendMessage("");
    this.player.sendMessage(Constants.PREFIX + "Bedenke dabei das man später im Spiel nicht außerhalb der Zone bauen kann und stirbt wenn man herausfällt.");
    this.player.getInventory().setItem(0, SELECT_FIRST_LOCATION);
  }

  @EventHandler
  public void handle(PlayerInteractEvent event) {
    if (!event.getAction().name().contains("RIGHT")
      || !event.getPlayer().equals(player)) return;

    if (SELECT_FIRST_LOCATION.equals(event.getItem())) {
      event.setCancelled(true);
      this.player.sendMessage(Constants.PREFIX + "Markiere nun die zweite Position.");
      this.player.getInventory().setItem(0, SELECT_SECOND_LOCATION);
      this.locationMap.put(ArenaPosition.FIRST_CORNER, this.player.getLocation());
      this.player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 2f);
    }

    if (SELECT_SECOND_LOCATION.equals(event.getItem())) {
      event.setCancelled(true);
      this.player.sendMessage(Constants.PREFIX + MessageFormat.replaceColors("Markiere nun den Spawn für das &bblaue &7Team."));
      this.player.getInventory().setItem(0, SELECT_BLUE_TEAM);
      this.locationMap.put(ArenaPosition.SECOND_CORNER, this.player.getLocation());
      this.player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 2f);
    }
    if (SELECT_BLUE_TEAM.equals(event.getItem())) {
      event.setCancelled(true);
      this.player.sendMessage(Constants.PREFIX + MessageFormat.replaceColors("Markiere nun den Spawn für das &crote &7Team."));
      this.player.getInventory().setItem(0, SELECT_RED_TEAM);
      this.locationMap.put(ArenaPosition.BLUE_SPAWN, this.player.getLocation());
      this.player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 2f);
    }
    if (SELECT_RED_TEAM.equals(event.getItem())) {
      event.setCancelled(true);
      this.locationMap.put(ArenaPosition.RED_SPAWN, this.player.getLocation());
      this.player.sendMessage(Constants.PREFIX + MessageFormat.replaceColors("Markiere nun den die Grundbauhöhe."));
      this.player.getInventory().setItem(0, BUILD_HEIGHT);
      this.player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 2f);
    }
    if (BUILD_HEIGHT.equals(event.getItem())) {
      event.setCancelled(true);
      this.locationMap.put(ArenaPosition.BUILD_HEIGHT, this.player.getLocation());
      this.player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 2f);
      this.player.sendMessage(Constants.PREFIX + "Du bist nun mit den Positionen fertig.");
      this.player.sendMessage(Constants.PREFIX + "Bitte tippe nun den Namen der Map ein:");
      this.waitForInput = true;
      this.player.getInventory().setItem(0, null);
    }
  }

  @EventHandler
  public void handle(AsyncPlayerChatEvent event) {
    if (!this.waitForInput || !event.getPlayer().equals(player)) return;
    event.setCancelled(true);
    if (this.name == null) {
      this.name = event.getMessage();
      this.player.sendMessage(Constants.PREFIX + "Bitte tippe nun die Beschreibung der Map ein:");
      return;
    }
    if (this.description == null) {
      this.description = event.getMessage();
      this.player.sendMessage(Constants.PREFIX + "Bitte nehme nun ein Item in die Hand und gibe etwas in den Chat ein, um das Item als DisplayItem zu bestätigen.");
      return;
    }
    if (this.material == null) {
      if (player.getItemInHand().getType().equals(Material.AIR)) {
        this.player.sendMessage(Constants.PREFIX + "Du hast kein Item in der Hand.");
        return;
      }
      this.waitForInput = false;
      this.material = player.getItemInHand().getType().name().toLowerCase();
      this.player.sendMessage(Constants.PREFIX + "Die Map wird nun erstellt.");
      long start = System.currentTimeMillis();
      ArenaTemplate arenaTemplate = this.create();
      this.player.sendMessage(MessageFormat.replaceColors(String.format(Constants.PREFIX + "Map wurde in &b%s&7ms erstellt.", System.currentTimeMillis() - start)));
      ArenaFormat.toFile(arenaTemplate,
        ArenaCreatorPlugin.getProvidingPlugin(ArenaCreatorPlugin.class)
          .getDataFolder()
          .toPath()
          .resolve(arenaTemplate.name() + ".arena"));
      this.player.sendMessage(MessageFormat.replaceColors(String.format(Constants.PREFIX + "Die Map wurde als &b%s &7gespeichert.", arenaTemplate.name() + ".arena")));
      this.abort();
    }
  }

  private boolean equalsIngredient(Ingredient ingredient, Block block) {
    return ingredient.material().equals(block.getType().name().toLowerCase()) && ingredient.code() == (int) block.getData();
  }

  private ArenaTemplate create() {

    Location firstCorner = this.locationMap.get(ArenaPosition.FIRST_CORNER);
    Location secondCorner = this.locationMap.get(ArenaPosition.SECOND_CORNER);
    World world = firstCorner.getWorld();

    int x1 = Math.min(firstCorner.getBlockX(), secondCorner.getBlockX());
    int y1 = Math.min(firstCorner.getBlockY(), secondCorner.getBlockY());
    int z1 = Math.min(firstCorner.getBlockZ(), secondCorner.getBlockZ());
    int x2 = Math.max(firstCorner.getBlockX(), secondCorner.getBlockX());
    int y2 = Math.max(firstCorner.getBlockY(), secondCorner.getBlockY());
    int z2 = Math.max(firstCorner.getBlockZ(), secondCorner.getBlockZ());

    List<Ingredient> ingredients = new ArrayList<>();
    AtomicInteger codeStep = new AtomicInteger();

    int[][][] layout = new int[x2 - x1][y2 - y1][z2 - z1];

    for (int x = 0; x < (x2 - x1); x++) {
      for (int y = 0; y < (y2 - y1); y++) {
        for (int z = 0; z < (z2 - z1); z++) {
          Location location = new Location(world, x1 + x, y1 + y, z1 + z);
          Ingredient ingredient = ingredients.stream().filter(i -> this.equalsIngredient(i, location.getBlock())).findAny().orElseGet(() -> {
            Ingredient i = new Ingredient(codeStep.getAndIncrement(), location.getBlock().getType().name().toLowerCase(), location.getBlock().getData());
            ingredients.add(i);
            return i;
          });

          layout[x][y][z] = ingredient.code();
        }
      }
    }

    return ArenaTemplate
      .builder()
      .name(this.name)
      .description(this.description)
      .positionMap(this.locationMap
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, stringLocationEntry -> this.toPosition(stringLocationEntry.getValue())))
      )
      .ingredients(ingredients)
      .displayMaterial(this.material)
      .layout(layout)
      .build();
  }

  private Position toPosition(Location location) {
    Location firstCorner = this.locationMap.get(ArenaPosition.FIRST_CORNER);
    Location secondCorner = this.locationMap.get(ArenaPosition.SECOND_CORNER);

    int x1 = Math.min(firstCorner.getBlockX(), secondCorner.getBlockX());
    int y1 = Math.min(firstCorner.getBlockY(), secondCorner.getBlockY());
    int z1 = Math.min(firstCorner.getBlockZ(), secondCorner.getBlockZ());

    return new Position(
      location.getX() - x1,
      location.getY() - y1,
      location.getZ() - z1,
      location.getYaw(),
      location.getPitch()
    );
  }

  public void abort() {
    HandlerList.unregisterAll(this);
    this.task.cancel();
    ArenaCreatorPlugin.ARENA_CREATION_PROCESSES.remove(this);
  }

  @Override
  public void run() {
    // From https://codepen.io/Codepixl/pen/ogWWaK/
    if (this.red > 0 && this.blue == 0) {
      this.red--;
      this.green++;
    }
    if (this.green > 0 && this.red == 0) {
      this.green--;
      this.blue++;
    }
    if (this.blue > 0 && this.green == 0) {
      this.red++;
      this.blue--;
    }
    if (!this.locationMap.containsKey(ArenaPosition.FIRST_CORNER)) return;
    Location firstCorner = this.locationMap.get(ArenaPosition.FIRST_CORNER);
    Location secondCorner = this.locationMap.containsKey(ArenaPosition.SECOND_CORNER) ?
      this.locationMap.get(ArenaPosition.SECOND_CORNER) : this.player.getLocation();
    World world = firstCorner.getWorld();

    int x1 = Math.min(firstCorner.getBlockX(), secondCorner.getBlockX());
    int y1 = Math.min(firstCorner.getBlockY(), secondCorner.getBlockY());
    int z1 = Math.min(firstCorner.getBlockZ(), secondCorner.getBlockZ());
    int x2 = Math.max(firstCorner.getBlockX(), secondCorner.getBlockX());
    int y2 = Math.max(firstCorner.getBlockY(), secondCorner.getBlockY());
    int z2 = Math.max(firstCorner.getBlockZ(), secondCorner.getBlockZ());

    for (int x = 0; x < (x2 - x1); x++) {
      spawnParticle(new Location(world, x1 + x, y1, z1));
      spawnParticle(new Location(world, x1 + x, y2, z1));
      spawnParticle(new Location(world, x1 + x, y1, z2));
      spawnParticle(new Location(world, x1 + x, y2, z2));
    }
    for (int y = 0; y < (y2 - y1); y++) {
      spawnParticle(new Location(world, x1, y1 + y, z1));
      spawnParticle(new Location(world, x1, y1 + y, z2));
      spawnParticle(new Location(world, x2, y1 + y, z1));
      spawnParticle(new Location(world, x2, y1 + y, z2));
    }
    for (int z = 0; z < (z2 - z1); z++) {
      spawnParticle(new Location(world, x1, y1, z1 + z));
      spawnParticle(new Location(world, x2, y1, z1 + z));
      spawnParticle(new Location(world, x1, y2, z1 + z));
      spawnParticle(new Location(world, x2, y2, z1 + z));
    }
  }

  private void spawnParticle(Location location) {
    location.add(location.getX() > 0 ? 0.5 : -0.5, 0.5, location.getZ() > 0 ? 0.5 : -0.5);
    ParticleEffect.REDSTONE.display(new ParticleEffect.OrdinaryColor(this.red, this.green, this.blue), location, this.player);
  }
}
