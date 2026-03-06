package de.rexlmanu.mlgrush.plugin.scoreboard.impl;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.Arena;
import de.rexlmanu.mlgrush.plugin.arena.team.GameTeam;
import de.rexlmanu.mlgrush.plugin.game.Environment;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.scoreboard.ScoreboardCreator;
import de.rexlmanu.mlgrush.plugin.scoreboard.packet.PacketTeamDefinition;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Accessors(fluent = true)
public class LobbyScoreboardCreator implements ScoreboardCreator, Runnable {

  private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
  private static final String SIDEBAR_TITLE = "&8« &a&lMLGRush &8»";

  public static final String[][] ADS = {
    { "Twitter", "&b@rexlManu" },
    { "GitHub", "&7github.com/rexlManu" },
  };

  private int currentStats;
  private final BukkitTask task;

  public LobbyScoreboardCreator() {
    this.currentStats = 0;
    this.task = Bukkit.getScheduler().runTaskTimer(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 20 * 3L);
  }

  @Override
  public void run() {
    PlayerProvider.getPlayers(Environment.LOBBY).forEach(this::updateLines);

    this.currentStats++;
    if (this.currentStats >= 3) {
      this.currentStats = 0;
    }
  }

  @Override
  public void updateLines(GamePlayer gamePlayer) {
    GameManager.instance().databaseContext().getRanking(gamePlayer.uniqueId()).whenComplete((rank, throwable) -> {
      if (throwable != null) {
        rank = -1;
      }
      String statsName = null;
      String statsValue = null;
      switch (this.currentStats) {
        case 0 -> {
          statsName = "Kills";
          statsValue = String.valueOf(gamePlayer.data().statistics().kills());
        }
        case 1 -> {
          statsName = "Tode";
          statsValue = String.valueOf(gamePlayer.data().statistics().deaths());
        }
        case 2 -> {
          statsName = "Siege";
          statsValue = String.valueOf(gamePlayer.data().statistics().wins());
        }
        default -> {
        }
      }
      gamePlayer.scoreboardSession().clearBelowName();
      gamePlayer.scoreboardSession().updateSidebar(SIDEBAR_TITLE, Stream.of(
        "",
        "&7Dein Ranking&8:",
        "&8 » &a" + (rank == -1 ? "?" : (rank + ". Platz")),
        "",
        "&7Deine " + statsName + "&8:",
        "&8 » &a" + statsValue,
        "",
        "&7Warteschlange&8:",
        "&8 » &a" + GameManager.instance().queueController().playerQueue().size() + " Spieler",
        "",
        "&7Spieler im Spiel&8:",
        "&8 » &a" + PlayerProvider.getPlayers(Environment.ARENA).size() + " Spieler",
        ""
      ).map(MessageFormat::replaceColors).collect(Collectors.toList()));
    });
  }

  @Override
  public void updateTablist(GamePlayer gamePlayer) {
    Player player = gamePlayer.player();
    if (player == null) {
      return;
    }
    List<PacketTeamDefinition> teams = new ArrayList<>();
    List<String> lobbyEntries = new ArrayList<>();
    List<String> arenaEntries = new ArrayList<>();
    Map<String, List<String>> spectatorArenaEntries = new HashMap<>();
    LinkedHashMap<java.util.UUID, Component> displayNames = new LinkedHashMap<>();
    Optional<Arena> ownArena = GameManager.instance().arenaManager().arenaContainer().activeArenas().stream()
      .filter(arena -> arena.spectators().contains(gamePlayer))
      .findFirst();
    final Optional<Arena> spectatorArena = ownArena;

    PlayerProvider.PLAYERS.forEach(target -> {
      if (target.player() == null) {
        return;
      }
      displayNames.put(target.uniqueId(), this.component(GameManager.instance().nicknameService().displayName(target.uniqueId(), target.player().getName())));
      if (target.environment().equals(Environment.ARENA)) {
        Optional<Arena> arena = GameManager.instance().arenaManager().arenaContainer().findArenaByPlayer(target);
        if (spectatorArena.isPresent() && arena.isPresent() && spectatorArena.get().equals(arena.get())) {
          GameTeam gameTeam = arena.get().getTeam(target);
          spectatorArenaEntries.computeIfAbsent(gameTeam.name().key(), key -> new ArrayList<>()).add(target.player().getName());
        } else {
          arenaEntries.add(target.player().getName());
        }
      } else {
        lobbyEntries.add(target.player().getName());
      }
    });

    if (!lobbyEntries.isEmpty()) {
      teams.add(new PacketTeamDefinition(
        "00-lobby",
        this.component("&7"),
        Component.empty(),
        com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
        com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.CollisionRule.ALWAYS,
        lobbyEntries
      ));
    }
    if (!arenaEntries.isEmpty()) {
      teams.add(new PacketTeamDefinition(
        "10-arena",
        this.component("&8"),
        Component.empty(),
        com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
        com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.CollisionRule.ALWAYS,
        arenaEntries
      ));
    }
    spectatorArenaEntries.forEach((key, entries) -> teams.add(new PacketTeamDefinition(
      "20-" + key,
      this.component(String.valueOf(de.rexlmanu.mlgrush.plugin.arena.team.TeamColor.valueOf(key.toUpperCase()).color())),
      Component.empty(),
      com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
      com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.CollisionRule.ALWAYS,
      entries
    )));

    gamePlayer.scoreboardSession().applyTeams(teams);
    gamePlayer.scoreboardSession().updateTabEntries(displayNames);
  }

  private Component component(String value) {
    return LEGACY_SERIALIZER.deserialize(MessageFormat.replaceColors(value));
  }
}
