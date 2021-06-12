package de.rexlmanu.mlgrush.plugin.arena;

import de.rexlmanu.mlgrush.arenalib.ArenaPosition;
import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.configuration.ArenaConfiguration;
import de.rexlmanu.mlgrush.plugin.arena.position.ArenaLocationMapper;
import de.rexlmanu.mlgrush.plugin.arena.position.ArenaRegion;
import de.rexlmanu.mlgrush.plugin.arena.team.GameTeam;
import de.rexlmanu.mlgrush.plugin.arena.team.TeamColor;
import de.rexlmanu.mlgrush.plugin.game.Environment;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.utility.PlayerUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

@Accessors(fluent = true)
@Getter
@Setter
public class Arena {
  private List<Block> placedBlocks = new ArrayList<>();


  private ArenaConfiguration configuration;
  private List<GamePlayer> players, spectators;
  private List<GameTeam> gameTeams;
  private long gameStart;

  private ArenaLocationMapper mapper;
  private ArenaRegion region;

  private Map<UUID, ArenaStatistics> arenaStatisticsMap;

  public Arena(ArenaConfiguration configuration, List<GamePlayer> players) {
    this.configuration = configuration;
    this.players = players;
    this.spectators = new ArrayList<>();
    this.gameTeams = new ArrayList<>();
    this.gameStart = System.currentTimeMillis();

    this.mapper = new ArenaLocationMapper(this.configuration.startPoint());
    this.region = new ArenaRegion(
      this.getPosition(ArenaPosition.FIRST_CORNER),
      this.getPosition(ArenaPosition.SECOND_CORNER)
    );
    this.arenaStatisticsMap = new HashMap<>();

    this.players.forEach(gamePlayer -> gamePlayer.environment(Environment.ARENA));
    this.createTeams();
    this.addPlayersToTeams();
    this.setup();

    GameManager.instance().spectatorInventory().addArena(this);
  }

  public void resetBlocks() {
    this.placedBlocks.forEach(block -> block.setType(Material.AIR));
    this.placedBlocks.clear();
  }

  public int buildHeightLimit() {
    return this.gameTeams.get(0).spawnLocation().getBlockY();
  }

  public GameTeam getTeam(GamePlayer gamePlayer) {
    return this.gameTeams.stream().filter(gameTeam -> gameTeam.members().contains(gamePlayer)).findFirst().orElseThrow(IllegalStateException::new);
  }

  public GameTeam getTeam(Location location) {
    return this.gameTeams.stream().min(Comparator.comparing(o -> o.spawnLocation().distance(location))).orElse(null);
  }

  public void respawnPlayer(GamePlayer gamePlayer) {
    GameTeam team = this.getTeam(gamePlayer);
    Player player = gamePlayer.player();
    player.teleport(team.spawnLocation());
    player.closeInventory();
    player.getInventory().clear();
    player.setVelocity(new Vector(0, 0, 0));
    gamePlayer.giveEquipment();
    gamePlayer.sound(Sound.ORB_PICKUP, 2f);
    GameManager.instance().scoreboardHandler().update(gamePlayer);
  }

  public void resetGame() {
    this.resetBlocks();
    this.players.forEach(this::respawnPlayer);

    this.checkWinCondition();
  }

  public ArenaStatistics statsFromPlayer(GamePlayer player) {
    return this.arenaStatisticsMap.get(player.uniqueId());
  }

  private void checkWinCondition() {
    this.gameTeams
      .stream()
      .filter(gameTeam -> gameTeam.points() == this.configuration.maximalPoints())
      .findFirst()
      .ifPresent(gameTeam -> {
        GameManager.instance().arenaManager().delete(this);
      });
  }

  public GameTeam getWinningTeam() {
    return this.gameTeams
      .stream()
      .filter(gameTeam -> gameTeam.points() == this.configuration.maximalPoints())
      .findFirst().orElseGet(() -> {
        if (this.gameTeams.stream().filter(gameTeam -> !gameTeam.members().isEmpty()).count() == 1) {
          return this.gameTeams.stream().filter(gameTeam -> !gameTeam.members().isEmpty()).findAny().get();
        }
        return null;
      });
  }

  private void addPlayersToTeams() {
    Collections.shuffle(this.players);
    int teamIndex = 0;
    for (int playerIndex = 0; playerIndex < this.players.size(); playerIndex++) {
      if (teamIndex >= this.gameTeams.size()) teamIndex = 0;
      this.gameTeams.get(teamIndex).members().add(this.players.get(playerIndex));
      teamIndex++;
    }
  }

  private void createTeams() {
    for (int index = 0; index < this.configuration.teamAmount(); index++) {
      TeamColor teamColor = TeamColor.values()[index];
      this.gameTeams.add(new GameTeam(teamColor, this.getPosition(teamColor.key() + "-spawn")));
    }
  }

  private Location getPosition(String key) {
    return this.mapper.toLocation(this.configuration.arenaTemplate().positionMap().get(key));
  }

  private void setup() {
    this.players.forEach(gamePlayer -> {
      this.arenaStatisticsMap.put(gamePlayer.uniqueId(), new ArenaStatistics());
      Player player = gamePlayer.player();
      PlayerProvider.PLAYERS.stream()
        .filter(gamePlayer1 -> !this.players.contains(gamePlayer1))
        .map(GamePlayer::player)
        .forEach(player::hidePlayer);
      PlayerUtils.resetPlayer(player);
      player.setGameMode(GameMode.SURVIVAL);
    });
    Bukkit.getScheduler().runTaskLater(GamePlugin.getProvidingPlugin(GamePlugin.class), () -> {
      GameManager.instance().scoreboardHandler().updateAll(Environment.LOBBY);
      this.players.forEach(this::respawnPlayer);
    }, 1);
  }
}
