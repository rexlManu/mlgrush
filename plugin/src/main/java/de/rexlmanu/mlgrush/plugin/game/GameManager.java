package de.rexlmanu.mlgrush.plugin.game;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.ArenaManager;
import de.rexlmanu.mlgrush.plugin.database.DatabaseContext;
import de.rexlmanu.mlgrush.plugin.database.DatabaseFactory;
import de.rexlmanu.mlgrush.plugin.detection.DetectionController;
import de.rexlmanu.mlgrush.plugin.event.EventCoordinator;
import de.rexlmanu.mlgrush.plugin.event.cancel.EventCancel;
import de.rexlmanu.mlgrush.plugin.game.environment.ArenaEnvironment;
import de.rexlmanu.mlgrush.plugin.game.environment.LobbyEnvironment;
import de.rexlmanu.mlgrush.plugin.game.npc.LobbyNpcManager;
import de.rexlmanu.mlgrush.plugin.inventory.SpectatorInventory;
import de.rexlmanu.mlgrush.plugin.location.LocationProvider;
import de.rexlmanu.mlgrush.plugin.nick.NicknameService;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.queue.QueueController;
import de.rexlmanu.mlgrush.plugin.scoreboard.ScoreboardHandler;
import de.rexlmanu.mlgrush.plugin.stats.StatsHologramManager;
import de.rexlmanu.mlgrush.plugin.utility.FlyingItem;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import de.rexlmanu.mlgrush.plugin.utility.cooldown.Cooldown;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.io.File;
import java.util.List;

/**
 * The main logic will get manages from here
 */
@Accessors(fluent = true)
@Getter
public class GameManager {

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
  private LobbyNpcManager lobbyNpcManager;
  private DetectionController detectionController;
  private List<GameEnvironment> environments;
  private ScoreboardHandler scoreboardHandler;
  private NicknameService nicknameService;

  private SpectatorInventory spectatorInventory;

  private StatsHologramManager statsHologramManager;
  //  private StatsNPCProvider statsNPCProvider;
  private Cooldown queueCooldown;
  private FlyingItem flyingItem;

  private GameManager() {
    GameManager.instance = this;
    File dataFolder = GamePlugin.getPlugin(GamePlugin.class).getDataFolder();
    this.databaseContext = DatabaseFactory.create();
    this.locationProvider = new LocationProvider(dataFolder);
    this.eventCoordinator = new EventCoordinator();
    this.queueController = new QueueController();
    this.arenaManager = new ArenaManager();
    this.lobbyNpcManager = new LobbyNpcManager();
    this.detectionController = new DetectionController();
    this.environments = java.util.Arrays.asList(new LobbyEnvironment(), new ArenaEnvironment());
    this.scoreboardHandler = new ScoreboardHandler();
    this.nicknameService = new NicknameService();

    this.spectatorInventory = new SpectatorInventory();
    this.statsHologramManager = new StatsHologramManager();
//    this.statsNPCProvider = new StatsNPCProvider();
    this.queueCooldown = new Cooldown(1500);
    this.flyingItem = new FlyingItem();
    this.flyingItem.setItemStack(ItemStackBuilder.of(Material.STICK).enchant(Enchantment.INFINITY, 1).build());
    // Sometimes in development it happens when the server don't get nicely shutdown, the 'old' are still there and you can't use the new spawned one.
    // Bukkit.getWorlds().stream().map(World::getLivingEntities).forEach(livingEntities -> livingEntities.forEach(Entity::remove));
    Bukkit.getWorlds().forEach(world -> world.setDifficulty(Difficulty.EASY));

    this.locationProvider.get("hologram").ifPresent(location -> {
      this.flyingItem.setLocation(location);
      this.flyingItem.setHeight(1.5);
      this.flyingItem.spawn();
    });

    this.environments.forEach(gameEnvironment -> Bukkit.getPluginManager().registerEvents(gameEnvironment, GamePlugin.getProvidingPlugin(GamePlugin.class)));

    // Cancel all basic events that are not necessary
    java.util.Arrays.asList(
      FoodLevelChangeEvent.class,
      WeatherChangeEvent.class,
      PlayerDropItemEvent.class,
      EntityPickupItemEvent.class,
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
    this.lobbyNpcManager.shutdown();
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
