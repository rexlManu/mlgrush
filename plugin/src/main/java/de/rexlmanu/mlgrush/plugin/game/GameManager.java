package de.rexlmanu.mlgrush.plugin.game;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.ArenaManager;
import de.rexlmanu.mlgrush.plugin.equipment.BlockEquipment;
import de.rexlmanu.mlgrush.plugin.equipment.EquipmentMob;
import de.rexlmanu.mlgrush.plugin.equipment.StickEquipment;
import de.rexlmanu.mlgrush.plugin.event.EventCoordinator;
import de.rexlmanu.mlgrush.plugin.game.environment.ArenaEnvironment;
import de.rexlmanu.mlgrush.plugin.game.environment.LobbyEnvironment;
import de.rexlmanu.mlgrush.plugin.game.npc.InteractiveMob;
import de.rexlmanu.mlgrush.plugin.inventory.ShopInventory;
import de.rexlmanu.mlgrush.plugin.location.LocationProvider;
import de.rexlmanu.mlgrush.plugin.queue.QueueController;
import de.rexlmanu.mlgrush.plugin.scoreboard.ScoreboardHandler;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The main logic will get manages from here
 */
@Accessors(fluent = true)
@Getter
public class GameManager {

  private static final List<EquipmentMob> EQUIPMENT_MOBS = Arrays.asList(
    EquipmentMob.builder()
      .entityType(EntityType.ZOMBIE)
      .lines(Arrays.asList("&8» &eBlock", "&7Ändere den Typ deiner Blöcke."))
      .inventoryName("&eBlöcke")
      .locationName("block-change-npc")
      .elements(BlockEquipment.values())
      .build(),
    EquipmentMob.builder()
      .entityType(EntityType.VILLAGER)
      .lines(Arrays.asList("&8» &eStick", "&7Ändere den Stick, mit dem du kämpfst."))
      .inventoryName("&eStick")
      .locationName("stick-change-npc")
      .elements(StickEquipment.values())
      .build()
  );

  public static void create() {
    new GameManager();
  }

  @Getter
  private static GameManager instance;


  private LocationProvider locationProvider;
  private EventCoordinator eventCoordinator;
  private QueueController queueController;
  private ArenaManager arenaManager;
  private List<InteractiveMob> interactiveMobs;
  private List<GameEnvironment> environments;
  private ScoreboardHandler scoreboardHandler;

  private GameManager() {
    GameManager.instance = this;
    File dataFolder = GamePlugin.getPlugin(GamePlugin.class).getDataFolder();
    this.locationProvider = new LocationProvider(dataFolder);
    this.eventCoordinator = new EventCoordinator();
    this.queueController = new QueueController();
    this.arenaManager = new ArenaManager();
    this.interactiveMobs = new ArrayList<>();
    this.environments = Arrays.asList(new LobbyEnvironment(), new ArenaEnvironment());
    this.scoreboardHandler = new ScoreboardHandler();

    this.locationProvider.get("queue-npc").ifPresent(location -> this.interactiveMobs.add(new InteractiveMob(EntityType.WITCH, Arrays.asList(
      "&8» &eQueue",
      "&7Suche nach einem Gegner."
    ), player -> {
      if (player.environment().equals(Environment.ARENA)) {
        player.sendMessage("Du bist bereits in einem Spiel.");
        return;
      }
      if (this.queueController.inQueue(player)) {
        this.queueController.playerQueue().remove(player);
        player.sendMessage("Du hast die &eWarteschlange &7verlassen.");
        this.scoreboardHandler.updateAll(Environment.LOBBY);
        player.sound(Sound.PISTON_RETRACT, 2f);
        return;
      }
      this.queueController.playerQueue().offer(player);
      player.sendMessage("Du hast die &eWarteschlange &7betreten.");
      this.scoreboardHandler.updateAll(Environment.LOBBY);
      player.sound(Sound.PISTON_EXTEND, 2f);
    }).create(location)));

    EQUIPMENT_MOBS
      .forEach(equipmentMob -> this.locationProvider.get(equipmentMob.locationName())
        .ifPresent(location -> this.interactiveMobs.add(
          new InteractiveMob(equipmentMob.entityType(), equipmentMob.lines(), player ->
            new ShopInventory(player, equipmentMob.inventoryName(), equipmentMob.elements()))
            .create(location)
        )));

    this.environments.forEach(gameEnvironment -> Bukkit.getPluginManager().registerEvents(gameEnvironment, GamePlugin.getProvidingPlugin(GamePlugin.class)));
  }

  public void onDisable() {
    this.interactiveMobs.forEach(InteractiveMob::remove);
  }

}
