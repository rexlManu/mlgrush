package de.rexlmanu.mlgrush.plugin.game.environment;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.Arena;
import de.rexlmanu.mlgrush.plugin.arena.ArenaManager;
import de.rexlmanu.mlgrush.plugin.arena.events.ArenaPlayerBlockPlaceEvent;
import de.rexlmanu.mlgrush.plugin.arena.events.ArenaPlayerDiedEvent;
import de.rexlmanu.mlgrush.plugin.arena.events.ArenaPlayerLeftEvent;
import de.rexlmanu.mlgrush.plugin.arena.events.ArenaTeamBedDestroyedEvent;
import de.rexlmanu.mlgrush.plugin.arena.team.GameTeam;
import de.rexlmanu.mlgrush.plugin.event.EventCoordinator;
import de.rexlmanu.mlgrush.plugin.game.Environment;
import de.rexlmanu.mlgrush.plugin.game.GameEnvironment;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.utility.LocationUtils;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import de.rexlmanu.mlgrush.plugin.utility.RandomElement;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ArenaEnvironment implements GameEnvironment {

  public static final Environment ENVIRONMENT = Environment.ARENA;
  private Cache<GamePlayer, GamePlayer> lastHitterCache;

  public ArenaEnvironment() {
    this.lastHitterCache = CacheBuilder.newBuilder()
      .expireAfterWrite(25, TimeUnit.SECONDS)
      .build();
    EventCoordinator coordinator = GameManager.instance().eventCoordinator();
    ArenaManager arenaManager = GameManager.instance().arenaManager();

    coordinator.add(ENVIRONMENT, AsyncPlayerChatEvent.class, event -> {
      event.target().setCancelled(true);
      String message = MessageFormat.replaceColors(String.format("&e%s &8» &7", event.gamePlayer().player().getName())) + event.target().getMessage();

      arenaManager
        .arenaContainer()
        .findArenaByPlayer(event.gamePlayer())
        .ifPresent(arena -> arena.players().forEach(gamePlayer -> gamePlayer.player().sendMessage(message)));
    });
    coordinator.add(ENVIRONMENT, BlockPlaceEvent.class, event -> arenaManager
      .arenaContainer()
      .findArenaByPlayer(event.gamePlayer()).ifPresent(arena -> {
        Block block = event.target().getBlock();
        Location location = block.getLocation();
        GameTeam team = arena.getTeam(event.gamePlayer());
        if (!arena.region().contains(location)
          || arena.buildHeightLimit() <= location.getBlockY()
          || LocationUtils.rangeContains(team.spawnLocation(), location, arena.configuration().spawnProtection())) {
          event.target().setCancelled(true);
          return;
        }

        arena.placedBlocks().add(block);
        Bukkit.getPluginManager().callEvent(new ArenaPlayerBlockPlaceEvent(event.gamePlayer(), block));
      }));
    coordinator.add(ENVIRONMENT, BlockBreakEvent.class, event -> arenaManager.arenaContainer().findArenaByPlayer(event.gamePlayer()).ifPresent(arena -> {
      Block block = event.target().getBlock();
      Location location = block.getLocation();
      if (!arena.region().contains(location)) {
        event.target().setCancelled(true);
        return;
      }

      if (block.getType().equals(Material.BED)
        || block.getType().equals(Material.BED_BLOCK)) {
        GameTeam destroyedBedTeam = arena.getTeam(location);
        GameTeam team = arena.getTeam(event.gamePlayer());
        if (arena.getTeam(location).equals(team)) {
          event.target().setCancelled(true);
          return;
        }
        team.addPoint();
        event.target().setCancelled(true);
        Bukkit.getPluginManager().callEvent(new ArenaTeamBedDestroyedEvent(event.gamePlayer(), destroyedBedTeam));
        return;
      }
      if (!arena.placedBlocks().contains(block)) {
        event.target().setCancelled(true);
        return;
      }
      Bukkit.getPluginManager().callEvent(new ArenaPlayerBlockPlaceEvent(event.gamePlayer(), block));
      block.setType(Material.AIR);
      arena.placedBlocks().remove(block);
    }));
    coordinator.add(ENVIRONMENT, PlayerMoveEvent.class, event -> arenaManager
      .arenaContainer()
      .findArenaByPlayer(event.gamePlayer()).ifPresent(arena -> {
        Location to = event.target().getTo();
        Location from = event.target().getFrom();
        if (to.getX() == from.getX()
          && to.getY() == from.getY()
          && to.getZ() == from.getZ()) return;
        if (!arena.region().contains(to)) {
          try {
            Bukkit.getPluginManager().callEvent(new ArenaPlayerDiedEvent(event.gamePlayer(), this.lastHitterCache.get(event.gamePlayer(), event::gamePlayer)));
          } catch (ExecutionException e) {
            e.printStackTrace();
          }
          arena.respawnPlayer(event.gamePlayer());
        }
      }));
    coordinator.add(ENVIRONMENT, ArenaPlayerLeftEvent.class, event -> {
      Arena arena = event.target().arena();
      arena.getTeam(event.gamePlayer()).members().remove(event.gamePlayer());
      arenaManager.delete(arena);
    });

    coordinator.add(ENVIRONMENT, ArenaTeamBedDestroyedEvent.class, event -> arenaManager.arenaContainer()
      .findArenaByPlayer(event.gamePlayer()).ifPresent(Arena::resetGame));

    coordinator.add(ENVIRONMENT, ArenaPlayerBlockPlaceEvent.class, event -> arenaManager.arenaContainer()
      .findArenaByPlayer(event.gamePlayer()).ifPresent(arena -> arena.statsFromPlayer(event.gamePlayer()).addBlock()));

    coordinator.add(ENVIRONMENT, ArenaPlayerDiedEvent.class, event -> arenaManager.arenaContainer()
      .findArenaByPlayer(event.gamePlayer()).ifPresent(arena -> {
        arena.statsFromPlayer(event.gamePlayer()).addDeath();
        if (event.target().killer() != null)
          arena.statsFromPlayer(event.target().killer()).addKill();
      }));
    coordinator.add(Environment.LOBBY, PlayerMoveEvent.class, event -> {
      Location to = event.target().getTo();
      Location from = event.target().getFrom();
      if (to.getX() == from.getX()
        && to.getY() == from.getY()
        && to.getZ() == from.getZ()) return;

      arenaManager
        .arenaContainer()
        .activeArenas()
        .stream()
        .filter(arena -> arena.spectators().contains(event.gamePlayer()))
        .findAny()
        .ifPresent(arena -> {
          if (!arena.region().contains(to)) {
            // Teleport back to random spawn
            event.gamePlayer().player().teleport(RandomElement.of(arena.gameTeams()).spawnLocation());
          }
        });
    });
  }

  @EventHandler
  public void handle(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) return;
    Player player = (Player) event.getEntity();
    PlayerProvider.find(player.getUniqueId()).filter(gamePlayer -> gamePlayer.environment().equals(ENVIRONMENT))
      .ifPresent(gamePlayer -> {
        if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
          event.setCancelled(true);
          return;
        }
        event.setDamage(0);
      });
  }

  @EventHandler
  public void handle(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;
    Player damager = (Player) event.getDamager();
    Player player = (Player) event.getEntity();
    PlayerProvider.find(player.getUniqueId()).filter(gamePlayer -> gamePlayer.environment().equals(ENVIRONMENT))
      .ifPresent(gamePlayer -> PlayerProvider.find(damager.getUniqueId()).filter(t -> t.environment().equals(ENVIRONMENT))
        .ifPresent(targetPlayer -> {
          GameManager.instance().arenaManager().arenaContainer().findArenaByPlayer(gamePlayer).ifPresent(arena -> {
            if (arena.configuration().knockbackOnlyHeight()) {
              player.setVelocity(new Vector(0, ThreadLocalRandom.current().nextDouble(0.311), 0));
              Bukkit.getScheduler().runTask(GamePlugin.getPlugin(GamePlugin.class), () ->
                player.setVelocity(new Vector(0, ThreadLocalRandom.current().nextDouble(0.311), 0)));
            }
          });
          this.lastHitterCache.put(gamePlayer, targetPlayer);
        }));
  }

  @EventHandler
  public void handle(PlayerBedEnterEvent event) {
    event.setCancelled(true);
  }

}
