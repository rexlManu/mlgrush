package de.rexlmanu.mlgrush.plugin.game.environment;

import de.rexlmanu.mlgrush.plugin.arena.events.ArenaPlayerLeftEvent;
import de.rexlmanu.mlgrush.plugin.event.EventCoordinator;
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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class LobbyEnvironment implements GameEnvironment {

  private static final Environment ENVIRONMENT = Environment.LOBBY;

  private static ItemStack LEAVE_ITEM = ItemStackBuilder.of(Material.IRON_DOOR).name("&8» &eSpiel verlassen").build();
  private static ItemStack SPECATOR_ITEM = ItemStackBuilder.of(Material.COMPASS).name("&8» &eSpectator").build();

  private static ItemStack CHALLENGER_ITEM = ItemStackBuilder.of(Material.IRON_SWORD).name("&8» &eQueue")
    .lore("&7<Rechtsklick> &8- &eHerausforderung annehmen",
      "&7<Linksklick> &8- &eSpieler herausfordern").build();

  public static final ItemStack BACK_TO_LOBBY_ITEM = ItemStackBuilder
    .of(Material.FIREWORK_CHARGE)
    .name("&8» &eZurück zur Lobby")
    .build();

  public LobbyEnvironment() {

    EventCoordinator coordinator = GameManager.instance().eventCoordinator();

    coordinator.add(ENVIRONMENT, PlayerJoinEvent.class, event -> {
      Player player = event.target().getPlayer();
      PlayerUtils.resetPlayer(player);
      GameManager.instance().locationProvider().get("spawn").ifPresent(player::teleport);
      event.gamePlayer().fastBoard().updateTitle(MessageFormat.replaceColors("&e&lMLGRush"));
      GameManager.instance().scoreboardHandler().updateAll(Environment.LOBBY);

      GameManager.instance().arenaManager().arenaContainer().activeArenas().forEach(arena ->
        arena.players().forEach(gamePlayer -> gamePlayer.player().hidePlayer(player)));
      this.giveLobbyItems(player);
    });
    coordinator.add(ENVIRONMENT, AsyncPlayerChatEvent.class, event -> {
      event.target().setCancelled(true);
      String message = MessageFormat.replaceColors(String.format("&e%s &8» &7", event.gamePlayer().player().getName())) + event.target().getMessage();

      PlayerProvider.getPlayers(ENVIRONMENT).forEach(gamePlayer -> gamePlayer.player().sendMessage(message));
    });
    coordinator.add(ENVIRONMENT, BlockPlaceEvent.class, event -> event.target().setCancelled(true));
    coordinator.add(ENVIRONMENT, BlockBreakEvent.class, event -> event.target().setCancelled(true));
    coordinator.add(ENVIRONMENT, PlayerInteractEvent.class, event -> {
      Player player = event.gamePlayer().player();
      if (LEAVE_ITEM.equals(event.target().getItem())) {
        event.target().setCancelled(true);
        event.gamePlayer().sound(Sound.LEVEL_UP, 2f);
        player.kickPlayer("");
        return;
      }
      if (SPECATOR_ITEM.equals(event.target().getItem())) {
        event.gamePlayer().sound(Sound.CHEST_OPEN, 2f);
        GameManager.instance().spectatorInventory().open(player);
        return;
      }

      if (BACK_TO_LOBBY_ITEM.equals(event.target().getItem())) {
        GameManager.instance().arenaManager().removeSpectator(event.gamePlayer());
        PlayerUtils.resetPlayer(player);
        GameManager.instance().locationProvider().get("spawn").ifPresent(player::teleport);
        this.giveLobbyItems(player);
        return;
      }
    });
    coordinator.add(ENVIRONMENT, PlayerInteractAtEntityEvent.class, event -> {
      if (!(event.target().getRightClicked() instanceof Player)
        || !CHALLENGER_ITEM.equals(event.target().getPlayer().getItemInHand())) return;
      PlayerProvider.find(event.target().getRightClicked().getUniqueId()).ifPresent(gamePlayer -> {
        if (!gamePlayer.challengeRequests().containsKey(event.gamePlayer().uniqueId())) return;
        gamePlayer.challengeRequests().remove(event.gamePlayer().uniqueId());
        gamePlayer.sendMessage(String.format("Du hast zum Duell mit &e%s&7 zugestimmt.", event.gamePlayer().player().getName()));
        event.gamePlayer().sendMessage(String.format("&e%s&7 hat dem Duell zugestimmt.", gamePlayer.player().getName()));
        event.gamePlayer().sound(Sound.FIREWORK_TWINKLE, 2f);
        gamePlayer.sound(Sound.FIREWORK_TWINKLE, 2f);

        GameManager.instance().arenaManager().create(Arrays.asList(event.gamePlayer(), gamePlayer));
      });
    });
  }

  private void giveLobbyItems(Player player) {
    player.getInventory().setItem(8, LEAVE_ITEM);
    player.getInventory().setItem(4, SPECATOR_ITEM);
    player.getInventory().setItem(0, CHALLENGER_ITEM);
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
      .ifPresent(target -> event.setCancelled(true));
  }

  @EventHandler
  public void handle(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player)
      || !(event.getDamager() instanceof Player)) return;

    PlayerProvider.find(event.getDamager().getUniqueId())
      .filter(gamePlayer -> gamePlayer.environment().equals(Environment.LOBBY))
      .ifPresent(gamePlayer -> {
        PlayerProvider.find(event.getEntity().getUniqueId()).ifPresent(target -> {
          if (target.challengeRequests().containsKey(gamePlayer.uniqueId())) return;
          target.challengeRequests().put(gamePlayer.uniqueId(), System.currentTimeMillis());
          target.sendMessage(String.format("Du wurdest von &e%s&7 zum Duell herausgefordert.", gamePlayer.player().getName()));
          gamePlayer.sendMessage(String.format("Du hast &e%s&7 zu einem Duell herausgefordert.", target.player().getName()));
          gamePlayer.sound(Sound.CHICKEN_EGG_POP, 2f);
        });
      });
  }

  @EventHandler
  public void handle(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player)) return;
    PlayerProvider.find(event.getWhoClicked().getUniqueId())
      .filter(gamePlayer -> gamePlayer.environment().equals(Environment.LOBBY))
      .ifPresent(gamePlayer -> event.setCancelled(true));
  }
}
