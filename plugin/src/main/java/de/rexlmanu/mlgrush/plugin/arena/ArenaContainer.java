package de.rexlmanu.mlgrush.plugin.arena;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.configuration.ArenaConfiguration;
import de.rexlmanu.mlgrush.plugin.arena.world.ChunkArenaGenerator;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Accessors(fluent = true)
@Getter
public class ArenaContainer {

  private static final String ARENA_WORLD = "arena";

  private List<Arena> activeArenas = new ArrayList<>();

  private World world;

  public ArenaContainer() {
    this.world = Bukkit.createWorld(
      WorldCreator
        .name(ARENA_WORLD)
        .environment(World.Environment.NORMAL)
        .generator(new ChunkArenaGenerator())
        .generateStructures(false)
    );

    this.world.setDifficulty(Difficulty.PEACEFUL);
    this.world.setFullTime(2000);
    this.world.setGameRuleValue("doDaylightCycle", "false");
    this.world.setGameRuleValue("doMobSpawning", "false");
  }

  public void register(List<GamePlayer> players, ArenaConfiguration configuration) {
    Bukkit.getScheduler().runTask(GamePlugin.getProvidingPlugin(GamePlugin.class), () -> this.activeArenas.add(new Arena(configuration, players)));
  }

  public Optional<Arena> findArenaByPlayer(GamePlayer gamePlayer) {
    return this.activeArenas.stream().filter(arena -> arena.players().contains(gamePlayer)).findAny();
  }

  public void remove(Arena arena) {
    arena.region().clear();
    this.activeArenas.remove(arena);
  }
}
