package de.rexlmanu.mlgrush.plugin.arena;

import com.cryptomorin.xseries.messages.Titles;
import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.configuration.ArenaConfiguration;
import de.rexlmanu.mlgrush.plugin.arena.events.ArenaDestroyEvent;
import de.rexlmanu.mlgrush.plugin.arena.events.ArenaTeamWonEvent;
import de.rexlmanu.mlgrush.plugin.arena.inventory.ArenaChoosingInventory;
import de.rexlmanu.mlgrush.plugin.arena.team.GameTeam;
import de.rexlmanu.mlgrush.plugin.arena.template.ArenaTemplateLoader;
import de.rexlmanu.mlgrush.plugin.arena.world.ArenaWriter;
import de.rexlmanu.mlgrush.plugin.game.Environment;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.game.environment.LobbyEnvironment;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import de.rexlmanu.mlgrush.plugin.utility.PlayerUtils;
import de.rexlmanu.mlgrush.plugin.utility.RandomElement;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Accessors(fluent = true)
@Getter
public class ArenaManager {

  public static final int HEIGHT = 75;
  public static final int SPACE_X = 250;
  public static final int SPACE_Z = 250;

  public static final ItemStack PICKAXE = ItemStackBuilder
    .of(Material.STONE_PICKAXE)
    .name("&eSpitzhacke")
    .breakable(false)
    .hideAttributes()
    .enchant(Enchantment.DIG_SPEED, 1)
    .build();

  private static final ArenaConfiguration.ArenaConfigurationBuilder DEFAULT_CONFIGURATION = ArenaConfiguration.builder()
    .maximalGameLength(Math.toIntExact(TimeUnit.MINUTES.toSeconds(30)))
    .maximalPoints(10)
    .teamAmount(2)
    .teamSize(1)
    .spawnProtection(3);

  private ArenaTemplateLoader templateLoader;
  private ArenaContainer arenaContainer;

  public ArenaManager() {
    this.templateLoader = new ArenaTemplateLoader();
    this.arenaContainer = new ArenaContainer();
  }

  public void create(List<GamePlayer> players) {
    ArenaChoosingInventory.create(players).whenComplete((template, throwable) -> {
      if (throwable != null) {
        players.forEach(gamePlayer -> gamePlayer.sendMessage("Das Spiel konnte nicht erstellt werden."));
        return;
      }
      ArenaConfiguration configuration = DEFAULT_CONFIGURATION
        .arenaTemplate(template)
        .startPoint(new Location(this.arenaContainer.world(), this.getNextFreeX(), HEIGHT, SPACE_Z))
        .build();
      players.forEach(gamePlayer -> gamePlayer.sendMessage("Das Spiel wird erstellt."));
      ArenaWriter.generateTemplate(configuration);
      this.arenaContainer.register(players, configuration);
      GameManager.instance().scoreboardHandler().updateAll(Environment.LOBBY);
    });
  }

  public void destroy(Arena arena) {
    arena.players().forEach(gamePlayer -> gamePlayer.environment(Environment.LOBBY));
    arena.spectators().forEach(this::removeSpectator);
    Bukkit.getScheduler().runTask(GamePlugin.getProvidingPlugin(GamePlugin.class), () -> {
      arena.players().stream().filter(gamePlayer -> Objects.nonNull(gamePlayer.player()))
        .forEach(gamePlayer -> {
          Player player = gamePlayer.player();
          PlayerUtils.resetPlayer(player);
          GameManager.instance().locationProvider().get("spawn").ifPresent(player::teleport);
          Bukkit.getOnlinePlayers().forEach(player::showPlayer);
        });
    });
    Bukkit.getScheduler().runTaskLaterAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), () ->
      GameManager.instance().scoreboardHandler().updateAll(Environment.LOBBY), 1);
    Bukkit.getPluginManager().callEvent(new ArenaDestroyEvent(arena));
    this.arenaContainer.remove(arena);
  }

  private int getNextFreeX() {
    Location baseLocation = this.arenaContainer.world().getSpawnLocation();
    int x = baseLocation.getBlockX();
    while (true) {
      int finalX = x;
      if (this.arenaContainer
        .activeArenas()
        .stream()
        .noneMatch(target -> target.configuration().startPoint().getBlockX() == finalX)) {
        break;
      }
      x += SPACE_X;
    }
    return x;
  }

  public void delete(Arena arena) {
    GameTeam winningTeam = arena.getWinningTeam();
    if (winningTeam == null) return;
    arena.players().forEach(gamePlayer -> {
      Player player = gamePlayer.player();
      Titles.sendTitle(player, 5, 20, 10,
        MessageFormat.replaceColors("&7Team " + winningTeam.name().displayName()),
        MessageFormat.replaceColors("&7hat gewonnen.")
      );
      gamePlayer.sound(Sound.LEVEL_UP, 1f);
      gamePlayer.sendMessage(String.format("Team %s &7hat das Spiel gewonnen!", winningTeam.name().displayName()));
    });
    Bukkit.getPluginManager().callEvent(new ArenaTeamWonEvent(arena, winningTeam));
    this.destroy(arena);
  }

  public void addSpectator(Arena arena, GamePlayer gamePlayer) {
    Player player = gamePlayer.player();
    PlayerUtils.resetPlayer(player);
    arena.spectators().add(gamePlayer);
    player.teleport(RandomElement.of(arena.gameTeams()).spawnLocation());

    player.setGameMode(GameMode.SPECTATOR);
    player.setAllowFlight(true);
    player.getInventory().setItem(4, LobbyEnvironment.BACK_TO_LOBBY_ITEM);
    gamePlayer.sound(Sound.LEVEL_UP, 2f);
    PlayerUtils.updateGameMode(player, GameMode.ADVENTURE);
  }

  public void removeSpectator(GamePlayer gamePlayer) {
    Player player = gamePlayer.player();
    PlayerUtils.resetPlayer(player);
    GameManager.instance().locationProvider().get("spawn").ifPresent(player::teleport);
    gamePlayer.sound(Sound.ORB_PICKUP, 2f);
    GameManager.instance().giveLobbyItems(player);
    this.arenaContainer
      .activeArenas()
      .stream()
      .filter(arena -> arena.spectators().contains(gamePlayer))
      .findAny()
      .ifPresent(arena -> {
        arena.spectators().remove(gamePlayer);
      });
  }
}
