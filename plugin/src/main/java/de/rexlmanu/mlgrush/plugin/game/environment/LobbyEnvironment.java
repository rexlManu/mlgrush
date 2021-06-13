package de.rexlmanu.mlgrush.plugin.game.environment;

import de.rexlmanu.mlgrush.plugin.Constants;
import de.rexlmanu.mlgrush.plugin.arena.ArenaManager;
import de.rexlmanu.mlgrush.plugin.arena.events.ArenaPlayerLeftEvent;
import de.rexlmanu.mlgrush.plugin.event.EventCoordinator;
import de.rexlmanu.mlgrush.plugin.game.Environment;
import de.rexlmanu.mlgrush.plugin.game.GameEnvironment;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.inventory.SettingsInventory;
import de.rexlmanu.mlgrush.plugin.inventory.configuration.ArenaConfigurationInventory;
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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LobbyEnvironment implements GameEnvironment {

  private static final Environment ENVIRONMENT = Environment.LOBBY;

  public static ItemStack LEAVE_ITEM = ItemStackBuilder.of(Material.IRON_DOOR).name("&8» &eSpiel verlassen").build();
  public static ItemStack SPECTATOR_ITEM = ItemStackBuilder.of(Material.COMPASS).name("&8» &eSpectator").build();
  public static ItemStack SETTINGS_ITEM = ItemStackBuilder.of(Material.REDSTONE_COMPARATOR).name("&8» &eEinstellungen").build();

  public static ItemStack CHALLENGER_ITEM = ItemStackBuilder.of(Material.IRON_SWORD).name("&8» &eHerausfordern")
    .lore("&7<Linksklick> &8- &eSpieler herausfordern",
      "&7<Rechtsklick> &8- &eEigenes Spiel erstellen").build();

  public static final ItemStack BACK_TO_LOBBY_ITEM = ItemStackBuilder
    .of(Material.FIREWORK_CHARGE)
    .name("&8» &eZurück zur Lobby")
    .build();

  public LobbyEnvironment() {

    EventCoordinator coordinator = GameManager.instance().eventCoordinator();

    coordinator.add(ENVIRONMENT, PlayerJoinEvent.class, event -> {
      Player player = event.target().getPlayer();
      PlayerUtils.resetPlayer(player);
      GameManager.instance().locationProvider().get("spawn").ifPresent(location -> {
        player.teleport(location);
        ParticleEffect.FIREWORKS_SPARK.display(
          location,
          0f,
          0.5f,
          0f,
          0.5f,
          150,
          null,
          PlayerProvider.getPlayers(ENVIRONMENT).stream().map(GamePlayer::player).collect(Collectors.toList())
        );
      });

      event.gamePlayer().fastBoard().updateTitle(MessageFormat.replaceColors("&e&lMLGRush"));
      GameManager.instance().scoreboardHandler().updateAll(Environment.LOBBY);

      GameManager.instance().arenaManager().arenaContainer().activeArenas().forEach(arena ->
        arena.players().forEach(gamePlayer -> gamePlayer.player().hidePlayer(player)));
      PlayerProvider.getPlayers(ENVIRONMENT).stream().map(GamePlayer::player).forEach(target -> target.showPlayer(player));
      PlayerProvider.getPlayers(ENVIRONMENT).stream().map(GamePlayer::player).forEach(player::showPlayer);
      GameManager.instance().giveLobbyItems(player);
      player.getLocation().getWorld().playSound(player.getLocation(), Sound.FIREWORK_TWINKLE, 1f, 1.2f);
      IntStream.range(0, 20).forEach(value -> player.sendMessage(""));
      Stream.of(
        String.format(Constants.PREFIX + "Hey, &e%s &7hier findest du einige Informationen:", event.gamePlayer().player().getName()),
        "&7Commands&8:",
        "",
        "  &8■ &e/leave &8× &7Verlasse das laufende Spiel",
        "  &8■ &e/stats <Name> &8× &7Betrachte deine oder dem Spieler seine Stats",
        "  &8■ &e/inv &8× &7Passe deine Inventarsortierung an",
        "",
        "&7Herausfordern&8:",
        "",
        "  &8■ &7Mit dem &eEisenschwert &7kannst du mit &eLinksklick &7andere Spieler herausfordern zu einem &eDuell&7.",
        "  &8■ &7Mit &eRechtsklick&7 auf einem Spieler, kannst du ein &eeigenes Spiel &7erstellen und einstellen welche &eOptionen &7aktiviert sein sollen.",
        ""
      )
        .map(MessageFormat::replaceColors)
        .forEach(s -> event.gamePlayer().player().sendMessage(s));
    });
    coordinator.add(ENVIRONMENT, AsyncPlayerChatEvent.class, event -> {
      event.target().setCancelled(true);
      String message = MessageFormat.replaceColors(String.format("&e%s &8» &7", event.gamePlayer().player().getName())) + event.target().getMessage();

      PlayerProvider.getPlayers(ENVIRONMENT).forEach(gamePlayer -> gamePlayer.player().sendMessage(message));
    });
    coordinator.add(ENVIRONMENT, BlockPlaceEvent.class, event -> event.target().setCancelled(!event.gamePlayer().buildMode()));
    coordinator.add(ENVIRONMENT, BlockBreakEvent.class, event -> event.target().setCancelled(!event.gamePlayer().buildMode()));
    coordinator.add(ENVIRONMENT, PlayerInteractEvent.class, event -> {
      Player player = event.gamePlayer().player();
      if (event.target().getAction().name().contains("RIGHT")) {
        if (LEAVE_ITEM.equals(event.target().getItem())) {
          event.target().setCancelled(true);
          event.gamePlayer().sound(Sound.LEVEL_UP, 2f);
          player.kickPlayer("");
          return;
        }
        if (SPECTATOR_ITEM.equals(event.target().getItem())) {
          event.gamePlayer().sound(Sound.CHEST_OPEN, 2f);
          GameManager.instance().spectatorInventory().open(player);
          return;
        }
        if (SETTINGS_ITEM.equals(event.target().getItem())) {
          event.gamePlayer().sound(Sound.CHEST_OPEN, 2f);
          new SettingsInventory(event.gamePlayer());
          return;
        }
      }

      if (BACK_TO_LOBBY_ITEM.equals(event.target().getItem())) {
        GameManager.instance().arenaManager().removeSpectator(event.gamePlayer());
        return;
      }
    });
    coordinator.add(ENVIRONMENT, PlayerInteractAtEntityEvent.class, event -> {
      if (!(event.target().getRightClicked() instanceof Player)
        || !CHALLENGER_ITEM.equals(event.target().getPlayer().getItemInHand())) return;
      PlayerProvider.find(event.target().getRightClicked().getUniqueId()).ifPresent(target -> {
        GamePlayer gamePlayer = event.gamePlayer();
        if (!CHALLENGER_ITEM.equals(event.target().getPlayer().getItemInHand())
          || gamePlayer.creatingGame()
          || target.creatingGame())
          return;
        if (target.challengeRequests().containsKey(event.gamePlayer().uniqueId())) {
          gamePlayer.sendMessage(String.format("Du hast &e%s&7 bereits eine Anfrage gesendet.", target.player().getName()));
          return;
        }
        new ArenaConfigurationInventory(gamePlayer, target);
        gamePlayer.sound(Sound.CHEST_OPEN, 2f);
      });
    });
    coordinator.add(ENVIRONMENT, PlayerTeleportEvent.class, event -> {
      if (PlayerTeleportEvent.TeleportCause.SPECTATE.equals(event.target().getCause())) {
        event.target().setCancelled(true);
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

      GameManager.instance().queueController().playerQueue().remove(gamePlayer);
      GameManager.instance().arenaManager().arenaContainer().activeArenas().forEach(arena -> arena.spectators().remove(gamePlayer));
      gamePlayer.save();
      GameManager.instance().scoreboardHandler().updateAll(Environment.LOBBY);
      PlayerProvider.PLAYERS.remove(gamePlayer);
    });
    PlayerProvider.PLAYERS.forEach(gamePlayer -> gamePlayer.challengeRequests().remove(event.getPlayer().getUniqueId()));
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
      .ifPresent(target -> event.setCancelled(true));
  }

  @EventHandler
  public void handle(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player)
      || !(event.getDamager() instanceof Player)
      || !CHALLENGER_ITEM.equals(((Player) event.getDamager()).getItemInHand())) return;

    PlayerProvider.find(event.getDamager().getUniqueId())
      .filter(gamePlayer -> gamePlayer.environment().equals(Environment.LOBBY) || !gamePlayer.creatingGame())
      .ifPresent(gamePlayer -> {
        PlayerProvider.find(event.getEntity().getUniqueId()).filter(g -> !g.creatingGame()).ifPresent(target -> {
          if (gamePlayer.challengeRequests().containsKey(target.uniqueId())) {
            gamePlayer.sound(Sound.ORB_PICKUP, 2f);
            gamePlayer.sendMessage(String.format("Du hast zum Duell mit &e%s&7 zugestimmt.", target.player().getName()));
            target.sendMessage(String.format("&e%s&7 hat dem Duell zugestimmt.", gamePlayer.player().getName()));
            List<GamePlayer> players = Arrays.asList(gamePlayer, target);
            players.forEach(player -> GameManager.instance().queueController().playerQueue().remove(player));
            GameManager.instance().arenaManager().create(players, gamePlayer.challengeRequests().get(target.uniqueId()));
            gamePlayer.challengeRequests().remove(target.uniqueId());
            target.challengeRequests().remove(gamePlayer.uniqueId());
            return;
          }
          if (target.challengeRequests().containsKey(gamePlayer.uniqueId())) return;
          target.challengeRequests().put(gamePlayer.uniqueId(), ArenaManager.DEFAULT_CONFIGURATION.get());
          target.sendMessage(String.format("Du wurdest von &e%s&7 zum Duell herausgefordert.", gamePlayer.player().getName()));
          gamePlayer.sendMessage(String.format("Du hast &e%s&7 zu einem Duell herausgefordert.", target.player().getName()));
          gamePlayer.sound(Sound.ORB_PICKUP, 2f);
        });
      });
  }

  @EventHandler
  public void handle(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player)) return;
    PlayerProvider.find(event.getWhoClicked().getUniqueId())
      .filter(gamePlayer -> gamePlayer.environment().equals(Environment.LOBBY))
      .ifPresent(gamePlayer -> event.setCancelled(!gamePlayer.buildMode()));
  }
}
