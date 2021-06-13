package de.rexlmanu.mlgrush.plugin;

import de.rexlmanu.mlgrush.plugin.command.InventoryCommand;
import de.rexlmanu.mlgrush.plugin.command.MainCommand;
import de.rexlmanu.mlgrush.plugin.command.QuitCommand;
import de.rexlmanu.mlgrush.plugin.command.StatsCommand;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.integration.IntegrationHandler;
import de.rexlmanu.mlgrush.plugin.task.ArenaActionbarTask;
import de.rexlmanu.mlgrush.plugin.task.ArenaPlayingTimeExtendCheckerTask;
import de.rexlmanu.mlgrush.plugin.task.UpdateStatsWallTask;
import de.rexlmanu.mlgrush.plugin.task.particle.FloorParticleTask;
import de.rexlmanu.mlgrush.plugin.task.particle.QueueParticleTask;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.Arrays;

@Accessors(fluent = true)
public class GamePlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    if (!this.getDataFolder().exists()) this.getDataFolder().mkdir();

    Path playersDirectory = this.getDataFolder().toPath().resolve("players");
    if (!playersDirectory.toFile().exists()) {
      playersDirectory.toFile().mkdir();
    }

    IntegrationHandler.enableIntegration();

    // Creates the actually game that handles everything
    GameManager.create();
    IntegrationHandler.gameInitIntegration();

    PluginCommand command = this.getCommand("mlgrush");
    MainCommand executor = new MainCommand();
    command.setExecutor(executor);
    command.setTabCompleter(executor);

    this.getCommand("inventory").setExecutor(new InventoryCommand());
    this.getCommand("quit").setExecutor(new QuitCommand());
    this.getCommand("stats").setExecutor(new StatsCommand());

    Bukkit.getWorlds().forEach(world -> {
      world.setDifficulty(Difficulty.NORMAL);
      world.setGameRuleValue("doDaylightCycle", "false");
      world.setGameRuleValue("doMobSpawning", "false");
      world.setFullTime(2000);
    });

    new ArenaActionbarTask();
    new ArenaPlayingTimeExtendCheckerTask();
    new UpdateStatsWallTask();

    Arrays.asList("queue-npc", "stick-change-npc", "block-change-npc")
      .forEach(s -> GameManager.instance().locationProvider().get(s)
        .ifPresent(FloorParticleTask::new));

    GameManager.instance().locationProvider().get("queue-npc")
      .ifPresent(QueueParticleTask::new);
  }

  @Override
  public void onDisable() {
    GameManager.instance().onDisable();
  }
}
