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
import de.rexlmanu.mlgrush.plugin.command.StatsCommand;
import de.rexlmanu.mlgrush.plugin.events.PlayerIngameEvent;
import de.rexlmanu.mlgrush.plugin.game.Environment;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.Statistics;
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
import java.util.function.Supplier;
import java.util.stream.Stream;

@Accessors(fluent = true)
@Getter
public class ArenaManager {

  public static final int HEIGHT = 75;
  public static final int SPACE_X = 250;
  public static final int SPACE_Z = 250;

  public static final ItemStack PICKAXE = ItemStackBuilder
    .of(Material.STONE_PICKAXE)
    .name("&aSpitzhacke")
    .breakable(false)
    .hideAttributes()
    .enchant(Enchantment.DIG_SPEED, 1)
    .build();

  public static final Supplier<ArenaConfiguration.ArenaConfigurationBuilder> DEFAULT_CONFIGURATION = () -> ArenaConfiguration.builder()
    .maximalGameLength(Math.toIntExact(TimeUnit.MINUTES.toSeconds(30)))
    .maximalPoints(10)
    .teamAmount(2)
    .teamSize(1)
    .spawnProtection(3)
    .nohitdelay(false)
    .buildHeight(16)
    .autoBlockBreak(false)
    .custom(true)
    .fallDamage(false)
    .unlimitedBlocks(false)
    .showCps(false)
    .knockbackOnlyHeight(false);

  private ArenaTemplateLoader templateLoader;
  private ArenaContainer arenaContainer;

  public ArenaManager() {
    this.templateLoader = new ArenaTemplateLoader();
    this.arenaContainer = new ArenaContainer();
  }

  public void create(List<GamePlayer> players) {
    this.create(players, DEFAULT_CONFIGURATION.get().custom(false));
  }

  public void create(List<GamePlayer> players, ArenaConfiguration.ArenaConfigurationBuilder configurationBuilder) {
    // Prevent dupe arenas, I dont know when could that happen but better to protect against that
    if (players.stream().anyMatch(GamePlayer::creatingGame)) return;
    players.forEach(gamePlayer -> {
      GameManager.instance().statsHologramManager().destroy(gamePlayer);
      gamePlayer.creatingGame(true);
      Bukkit.getPluginManager().callEvent(new PlayerIngameEvent(gamePlayer.player()));
    });
    ArenaChoosingInventory.create(players).whenComplete((template, throwable) -> {
      if (throwable != null) {
        players.forEach(gamePlayer -> gamePlayer.sendMessage("Das Spiel konnte nicht erstellt werden."));
        return;
      }
      ArenaConfiguration configuration = configurationBuilder
        .arenaTemplate(template)
        .startPoint(new Location(this.arenaContainer.world(), this.getNextFreeX(), HEIGHT, SPACE_Z))
        .build();
      players.forEach(gamePlayer -> gamePlayer.sendMessage("Das Spiel wird erstellt."));
      ArenaWriter.generateTemplate(configuration);
      if (configuration.custom()) {
        players.forEach(gamePlayer -> gamePlayer.sendMessage("In dieser Runde werden &akeine&7 Statistiken gewertet."));
      }
      this.arenaContainer.register(players, configuration);
      GameManager.instance().scoreboardHandler().updateAll(Environment.LOBBY);
    });
  }

  public void destroy(Arena arena) {
    arena.players().forEach(gamePlayer -> gamePlayer.creatingGame(false).environment(Environment.LOBBY));
    Bukkit.getScheduler().runTask(GamePlugin.getProvidingPlugin(GamePlugin.class), () -> {
      arena.spectators().forEach(this::removeSpectator);
      arena.players().stream().filter(gamePlayer -> Objects.nonNull(gamePlayer.player()))
        .forEach(gamePlayer -> {
          Player player = gamePlayer.player();
          PlayerUtils.resetPlayer(player);
          GameManager.instance().locationProvider().get("spawn").ifPresent(player::teleport);
          Bukkit.getOnlinePlayers().forEach(player::showPlayer);
          GameManager.instance().giveLobbyItems(player);
        });
      arena.resetBlocks();
      arena.region().clear(arena.configuration().startPoint());
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
      // Setting stats
      if (arena.configuration().custom()) return;
      gamePlayer.sendMessage("Deine Statistiken haben sich folgend ver??ndert:");
      ArenaStatistics arenaStatistics = arena.statsFromPlayer(gamePlayer);
      Statistics statistics = gamePlayer.data().statistics();
      double oldKd = StatsCommand.checkForNan(statistics.kills(), statistics.deaths());
      double newKd = StatsCommand.checkForNan(statistics.kills() + arenaStatistics.kills(), statistics.deaths() + arenaStatistics.deaths());
      double kdDifference = newKd - oldKd;
      double oldWinrate = StatsCommand.checkForNan(statistics.wins(), statistics.games()) * 100;
      double newWinrate = StatsCommand.checkForNan(statistics.wins() + (winningTeam.members().contains(gamePlayer) ? 1 : 0), statistics.games() + 1) * 100;
      double winrateDifference = newWinrate - oldWinrate;
      Stream.of(
        "",
        String.format("&8  ??? &7Kills &8??? &a%s &8(&a+%s&7&8)", statistics.kills(), arenaStatistics.kills()),
        String.format("&8  ??? &7Tode &8??? &a%s &8(&a+%s&7&8)", statistics.deaths(), arenaStatistics.deaths()),
        String.format("&8  ??? &7KD &8??? &a%.2f &8(&a%s%.2f&7&8)", oldKd, kdDifference > 0 ? "+" : "", kdDifference),
        "",
        String.format("&8  ??? &7Abgebaute Betten &8??? &a%s &8(&a+%s&7&8)", statistics.destroyedBeds(), arenaStatistics.destroyedBeds()),
        //String.format("&8  ??? &7Platzierte Bl??cke &8??? &a%s &8(&a+%s&7&8)", statistics.blocks(), arenaStatistics.blocks()),
        String.format("&8  ??? &7Siegreiche Spiele &8??? &a%s &8(&a+%s&7&8)", statistics.wins(), winningTeam.members().contains(gamePlayer) ? 1 : 0),
        String.format("&8  ??? &7Gespielte Spiele &8??? &a%s &8(&a+%s&7&8)", statistics.games(), 1),
        "",
        String.format("&8  ??? &7Siegesrate &8??? &a%.1f%% &8(&a%s%.1f%%&7&8)", oldWinrate, winrateDifference > 0 ? "+" : "", winrateDifference),
        ""
      ).map(MessageFormat::replaceColors).forEach(player::sendMessage);

      if (winningTeam.members().contains(gamePlayer)) {
        statistics.addWin();
      }
      statistics.addKills(arenaStatistics.kills())
        .addDeaths(arenaStatistics.deaths())
        .addBlocks(arenaStatistics.blocks())
        .addDestroyedBeds(arenaStatistics.destroyedBeds())
        .addGame();
    });
    Bukkit.getPluginManager().callEvent(new ArenaTeamWonEvent(arena, winningTeam));
    this.destroy(arena);
  }

  public void addSpectator(Arena arena, GamePlayer gamePlayer) {
    Player player = gamePlayer.player();
    GameManager.instance().statsHologramManager().destroy(gamePlayer);
    PlayerUtils.resetPlayer(player);
    arena.spectators().add(gamePlayer);
    player.teleport(RandomElement.of(arena.gameTeams()).spawnLocation());

    player.setGameMode(GameMode.SPECTATOR);
//    player.getInventory().setItem(4, LobbyEnvironment.BACK_TO_LOBBY_ITEM);
    gamePlayer.sound(Sound.LEVEL_UP, 2f);
    GameManager.instance().scoreboardHandler().update(gamePlayer);
    gamePlayer.sendMessage("Du kannst wieder zur Lobby\n &azur??ckkehren &7mit &8/&aquit&7.");
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
    GameManager.instance().scoreboardHandler().update(gamePlayer);
  }
}
