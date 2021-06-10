package de.rexlmanu.mlgrush.plugin.game.environment;

import de.rexlmanu.mlgrush.plugin.arena.events.ArenaPlayerLeftEvent;
import de.rexlmanu.mlgrush.plugin.event.EventCoordinator;
import de.rexlmanu.mlgrush.plugin.event.cancel.EventCancel;
import de.rexlmanu.mlgrush.plugin.game.Environment;
import de.rexlmanu.mlgrush.plugin.game.GameEnvironment;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import de.rexlmanu.mlgrush.plugin.utility.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class LobbyEnvironment implements GameEnvironment {

  private static ItemStack LEAVE_ITEM = ItemStackBuilder.of(Material.IRON_DOOR).name("&8» &eSpiel verlassen").build();
  private static ItemStack CHALLENGER_ITEM = ItemStackBuilder.of(Material.IRON_SWORD).name("&8» &eQueue")
    .lore("&7<Rechtsklick> &8- &eQueue beitreten / verlassen",
      "&7<Linksklick> &8- &eSpieler herausfordern").build();

  public LobbyEnvironment() {
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

    EventCoordinator coordinator = GameManager.instance().eventCoordinator();
    Environment environment = Environment.LOBBY;
    coordinator.add(environment, PlayerJoinEvent.class, event -> {
      Player player = event.target().getPlayer();
      PlayerUtils.resetPlayer(player);
      GameManager.instance().locationProvider().get("spawn").ifPresent(player::teleport);
      event.gamePlayer().fastBoard().updateTitle(MessageFormat.replaceColors("&e&lMLGRush"));
      GameManager.instance().scoreboardHandler().updateAll(Environment.LOBBY);

      GameManager.instance().arenaManager().arenaContainer().activeArenas().forEach(arena ->
        arena.players().forEach(gamePlayer -> gamePlayer.player().hidePlayer(player)));
    });
    coordinator.add(environment, AsyncPlayerChatEvent.class, event -> {
      event.target().setCancelled(true);
      String message = MessageFormat.replaceColors(String.format("&e%s &8» &7", event.gamePlayer().player().getName())) + event.target().getMessage();

      PlayerProvider.getPlayers(environment).forEach(gamePlayer -> gamePlayer.player().sendMessage(message));
    });
    coordinator.add(environment, BlockPlaceEvent.class, event -> event.target().setCancelled(true));
    coordinator.add(environment, BlockBreakEvent.class, event -> event.target().setCancelled(true));
    coordinator.add(environment, PlayerInteractEvent.class, event -> {
      if (LEAVE_ITEM.equals(event.target().getItem())) {
        event.target().setCancelled(true);
        event.gamePlayer().sound(Sound.LEVEL_UP, 2f);
        event.gamePlayer().player().kickPlayer("");
        return;
      }
    });
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void handle(PlayerJoinEvent event) {
    event.setJoinMessage(null);
    Player player = event.getPlayer();
    PlayerProvider.PLAYERS.add(new GamePlayer(player.getUniqueId()));
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void handle(PlayerQuitEvent event) {
    event.setQuitMessage(null);
    PlayerProvider.find(event.getPlayer().getUniqueId()).ifPresent(gamePlayer -> {
      GameManager.instance()
        .arenaManager()
        .arenaContainer()
        .findArenaByPlayer(gamePlayer)
        .ifPresent(arena -> Bukkit.getPluginManager().callEvent(new ArenaPlayerLeftEvent(gamePlayer, arena)));

      gamePlayer.save();
      PlayerProvider.PLAYERS.remove(gamePlayer);
    });
  }

  @EventHandler
  public void handle(CreatureSpawnEvent event) {
    if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void handle(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      event.setCancelled(true);
      return;
    }
    PlayerProvider.find(event.getEntity().getUniqueId())
      .filter(gamePlayer -> gamePlayer.environment().equals(Environment.LOBBY))
      .ifPresent(gamePlayer -> event.setCancelled(true));
  }

  @EventHandler
  public void handle(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player)) return;
    PlayerProvider.find(event.getWhoClicked().getUniqueId())
      .filter(gamePlayer -> gamePlayer.environment().equals(Environment.LOBBY))
      .ifPresent(gamePlayer -> event.setCancelled(true));
  }
}
