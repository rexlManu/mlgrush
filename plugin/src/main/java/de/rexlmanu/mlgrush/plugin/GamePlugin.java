package de.rexlmanu.mlgrush.plugin;

import de.rexlmanu.mlgrush.plugin.command.InventoryCommand;
import de.rexlmanu.mlgrush.plugin.command.MainCommand;
import de.rexlmanu.mlgrush.plugin.command.QuitCommand;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayerData;
import de.rexlmanu.mlgrush.plugin.task.ArenaActionbarTask;
import de.rexlmanu.mlgrush.plugin.task.ArenaParticleTask;
import de.rexlmanu.mlgrush.plugin.task.ArenaPlayingTimeExtendCheckerTask;
import eu.miopowered.repository.Repository;
import eu.miopowered.repository.impl.GsonRepository;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

@Accessors(fluent = true)
public class GamePlugin extends JavaPlugin {

  @Getter
  private Repository<GamePlayerData> repository;

  @Override
  public void onEnable() {
    if (!this.getDataFolder().exists()) this.getDataFolder().mkdir();

    Path playersDirectory = this.getDataFolder().toPath().resolve("players");
    if (!playersDirectory.toFile().exists()) {
      playersDirectory.toFile().mkdir();
    }

    this.repository = GsonRepository.of(playersDirectory, GamePlayerData.class);

    // Creates the actually game that handles everything
    GameManager.create();

    PluginCommand command = this.getCommand("mlgrush");
    MainCommand executor = new MainCommand();
    command.setExecutor(executor);
    command.setTabCompleter(executor);

    this.getCommand("inventory").setExecutor(new InventoryCommand());
    this.getCommand("quit").setExecutor(new QuitCommand());

    // StatsHandler.updateStatsWall();
    // Bukkit.getScheduler().runTaskTimerAsynchronously(this, StatsHandler::updateStatsWall, 0, 10 * 60 * 20);

    Bukkit.getWorlds().forEach(world -> {
      world.setDifficulty(Difficulty.NORMAL);
      world.setGameRuleValue("doDaylightCycle", "false");
      world.setGameRuleValue("doMobSpawning", "false");
      world.setFullTime(2000);
    });

    new ArenaActionbarTask();
    new ArenaPlayingTimeExtendCheckerTask();
    new ArenaParticleTask();
  }

  @Override
  public void onDisable() {
    GameManager.instance().onDisable();
  }
}
