package de.rexlmanu.mlgrush.plugin.game;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.ArenaManager;
import de.rexlmanu.mlgrush.plugin.database.DatabaseContext;
import de.rexlmanu.mlgrush.plugin.database.DatabaseFactory;
import de.rexlmanu.mlgrush.plugin.database.DatabaseType;
import de.rexlmanu.mlgrush.plugin.detection.DetectionController;
import de.rexlmanu.mlgrush.plugin.equipment.BlockEquipment;
import de.rexlmanu.mlgrush.plugin.equipment.EquipmentMob;
import de.rexlmanu.mlgrush.plugin.equipment.StickEquipment;
import de.rexlmanu.mlgrush.plugin.event.EventCoordinator;
import de.rexlmanu.mlgrush.plugin.event.cancel.EventCancel;
import de.rexlmanu.mlgrush.plugin.game.environment.ArenaEnvironment;
import de.rexlmanu.mlgrush.plugin.game.environment.LobbyEnvironment;
import de.rexlmanu.mlgrush.plugin.game.npc.ArmorStandInteraction;
import de.rexlmanu.mlgrush.plugin.game.npc.InteractionHandler;
import de.rexlmanu.mlgrush.plugin.game.npc.InteractiveMob;
import de.rexlmanu.mlgrush.plugin.inventory.ShopInventory;
import de.rexlmanu.mlgrush.plugin.inventory.SpectatorInventory;
import de.rexlmanu.mlgrush.plugin.location.LocationProvider;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.queue.QueueController;
import de.rexlmanu.mlgrush.plugin.scoreboard.ScoreboardHandler;
import de.rexlmanu.mlgrush.plugin.stats.StatsHologramManager;
import de.rexlmanu.mlgrush.plugin.utility.FlyingItem;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import de.rexlmanu.mlgrush.plugin.utility.cooldown.Cooldown;
import eu.miopowered.nickapi.NickAPI;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;

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
      .lines(Arrays.asList("&8?? &aBlock", "&7??ndere den Typ deiner Bl??cke."))
      .inventoryName("&aBl??cke")
      .armorStandName(MessageFormat.replaceColors("&a&lBl??cke"))
      .locationName("block-change-npc")
      .elements(BlockEquipment.values())
      .build(),
    EquipmentMob.builder()
      .entityType(EntityType.VILLAGER)
      .lines(Arrays.asList("&8?? &aStick", "&7??ndere den Stick, mit dem du k??mpfst."))
      .inventoryName("&aStick")
      .armorStandName(MessageFormat.replaceColors("&a&lSticks"))
      .locationName("stick-change-npc")
      .elements(StickEquipment.values())
      .build()
  );

  public static void create() {
    new GameManager();
  }

  @Getter
  private static GameManager instance;

  private DatabaseContext databaseContext;
  private LocationProvider locationProvider;
  private EventCoordinator eventCoordinator;
  private QueueController queueController;
  private ArenaManager arenaManager;
  private List<InteractiveMob> interactiveMobs;
  private DetectionController detectionController;
  private List<GameEnvironment> environments;
  private ScoreboardHandler scoreboardHandler;
  private NickAPI nickAPI;

  private SpectatorInventory spectatorInventory;

  private StatsHologramManager statsHologramManager;
  //  private StatsNPCProvider statsNPCProvider;
  private Cooldown queueCooldown;
  private FlyingItem flyingItem;

  private GameManager() {
    GameManager.instance = this;
    File dataFolder = GamePlugin.getPlugin(GamePlugin.class).getDataFolder();
    this.databaseContext = DatabaseFactory.create(DatabaseType.PLUGINSTUBE);
    this.locationProvider = new LocationProvider(dataFolder);
    this.eventCoordinator = new EventCoordinator();
    this.queueController = new QueueController();
    this.arenaManager = new ArenaManager();
    this.interactiveMobs = new ArrayList<>();
    this.detectionController = new DetectionController();
    this.environments = Arrays.asList(new LobbyEnvironment(), new ArenaEnvironment());
    this.scoreboardHandler = new ScoreboardHandler();
    this.nickAPI = NickAPI.create(GamePlugin.getProvidingPlugin(GamePlugin.class));

    this.spectatorInventory = new SpectatorInventory();
    this.statsHologramManager = new StatsHologramManager();
//    this.statsNPCProvider = new StatsNPCProvider();
    this.queueCooldown = new Cooldown(1500);
    this.flyingItem = new FlyingItem();
    this.flyingItem.setItemStack(ItemStackBuilder.of(Material.STICK).enchant(Enchantment.ARROW_INFINITE, 1).build());
    // Sometimes in development it happens when the server don't get nicely shutdown, the 'old' are still there and you can't use the new spawned one.
    // Bukkit.getWorlds().stream().map(World::getLivingEntities).forEach(livingEntities -> livingEntities.forEach(Entity::remove));
    Bukkit.getWorlds().forEach(world -> world.setDifficulty(Difficulty.EASY));

    InteractionHandler queueInteractionHandler = player -> {
      if (player.environment().equals(Environment.ARENA)) {
        player.sendMessage("Du bist bereits in einem Spiel.");
        return;
      }
      if (this.queueCooldown.currently(player.uniqueId())) return;
      this.queueCooldown.add(player.uniqueId());
      if (this.queueController.inQueue(player)) {
        this.queueController.playerQueue().remove(player);
        player.sendMessage("Du hast die &aWarteschlange &7verlassen.");
        this.scoreboardHandler.updateAll(Environment.LOBBY);
        player.sound(Sound.PISTON_RETRACT, 2f);
        return;
      }
      this.queueController.playerQueue().offer(player);
      player.sendMessage("Du hast die &aWarteschlange &7betreten.");
      this.scoreboardHandler.updateAll(Environment.LOBBY);
      player.sound(Sound.PISTON_EXTEND, 2f);
    };
    this.locationProvider.get("hologram").ifPresent(location -> {
      this.flyingItem.setLocation(location);
      this.flyingItem.setHeight(1);
      this.flyingItem.spawn();
    });
//    this.locationProvider.get("queue-npc").ifPresent(location -> {
//      this.interactiveMobs.add(new InteractiveMob(EntityType.WITCH, Arrays.asList(
//        "&8?? &aQueue",
//        "&7Suche nach einem Gegner."
//      ), queueInteractionHandler).create(location));
//    });

//    EQUIPMENT_MOBS
//      .forEach(equipmentMob -> this.locationProvider.get(equipmentMob.locationName())
//        .ifPresent(location -> this.interactiveMobs.add(
//          new InteractiveMob(equipmentMob.entityType(), equipmentMob.lines(), player ->
//            new ShopInventory(player, equipmentMob.inventoryName(), equipmentMob.elements()))
//            .create(location)
//        )));
    EQUIPMENT_MOBS.forEach(equipmentMob -> {
      new ArmorStandInteraction(equipmentMob.armorStandName(), player ->
        new ShopInventory(player, equipmentMob.inventoryName(), equipmentMob.elements()));
    });
    new ArmorStandInteraction(MessageFormat.replaceColors("&a&lQueue"), queueInteractionHandler);

    this.environments.forEach(gameEnvironment -> Bukkit.getPluginManager().registerEvents(gameEnvironment, GamePlugin.getProvidingPlugin(GamePlugin.class)));

    // Cancel all basic events that are not necessary
    Arrays.asList(
      FoodLevelChangeEvent.class,
      WeatherChangeEvent.class,
      PlayerDropItemEvent.class,
      PlayerPickupItemEvent.class,
      PlayerAchievementAwardedEvent.class,
      PlayerArmorStandManipulateEvent.class,
      PlayerBedEnterEvent.class,
      PlayerItemDamageEvent.class,
      BlockPhysicsEvent.class,
      BlockSpreadEvent.class,
      BlockGrowEvent.class,
      BlockIgniteEvent.class,
      EntityCombustEvent.class
    ).forEach(EventCancel::on);
  }

  public void onDisable() {
    this.interactiveMobs.forEach(InteractiveMob::remove);
    this.flyingItem.remove();
    // this.arenaManager.arenaContainer().activeArenas().forEach(this.arenaManager::destroy);
  }

  public void giveLobbyItems(Player player) {
    player.getInventory().setItem(8, LobbyEnvironment.LEAVE_ITEM);
    player.getInventory().setItem(3, LobbyEnvironment.SPECTATOR_ITEM);
    player.getInventory().setItem(5, LobbyEnvironment.SETTINGS_ITEM);
    player.getInventory().setItem(0, LobbyEnvironment.CHALLENGER_ITEM);
    player.getInventory().setItem(4, LobbyEnvironment.TUTORIAL_ITEM);

    PlayerProvider.find(player.getUniqueId()).ifPresent(gamePlayer -> GameManager.instance().statsHologramManager().show(gamePlayer));
  }
}
