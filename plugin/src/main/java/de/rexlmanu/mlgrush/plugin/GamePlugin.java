package de.rexlmanu.mlgrush.plugin;

import com.github.retrooper.packetevents.PacketEvents;
import de.rexlmanu.mlgrush.plugin.command.InventoryCommand;
import de.rexlmanu.mlgrush.plugin.command.MainCommand;
import de.rexlmanu.mlgrush.plugin.command.QuitCommand;
import de.rexlmanu.mlgrush.plugin.command.StatsCommand;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.integration.IntegrationHandler;
import de.rexlmanu.mlgrush.plugin.task.ArenaActionbarTask;
import de.rexlmanu.mlgrush.plugin.task.ArenaPlayingTimeExtendCheckerTask;
import de.rexlmanu.mlgrush.plugin.task.UpdateStatsWallTask;
import de.rexlmanu.mlgrush.plugin.task.arena.ArenaBlockRemoveTask;
import de.rexlmanu.mlgrush.plugin.task.arena.ArenaShowCpsTask;
import de.rexlmanu.mlgrush.plugin.task.particle.FloorParticleTask;
import de.rexlmanu.mlgrush.plugin.task.particle.QueueParticleTask;
import de.rexlmanu.mlgrush.plugin.task.particle.TwinsParticleTask;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class GamePlugin extends JavaPlugin {

  @Override
  public void onLoad() {
    PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
    PacketEvents.getAPI().load();
  }

  @Override
  public void onEnable() {
    if (!this.getDataFolder().exists()) {
      this.getDataFolder().mkdirs();
    }

    Path playersDirectory = this.getDataFolder().toPath().resolve("players");
    try {
      Files.createDirectories(playersDirectory);
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to create player data directory", exception);
    }

    IntegrationHandler.enableIntegration();

    GameManager.create();
    PacketEvents.getAPI().init();
    IntegrationHandler.gameInitIntegration();

    PluginCommand command = this.getCommand("mlgrush");
    MainCommand executor = new MainCommand();
    if (command != null) {
      command.setExecutor(executor);
      command.setTabCompleter(executor);
    }

    if (this.getCommand("inventory") != null) {
      this.getCommand("inventory").setExecutor(new InventoryCommand());
    }
    if (this.getCommand("quit") != null) {
      this.getCommand("quit").setExecutor(new QuitCommand());
    }
    if (this.getCommand("stats") != null) {
      this.getCommand("stats").setExecutor(new StatsCommand());
    }

    Bukkit.getWorlds().forEach(world -> {
      world.setDifficulty(Difficulty.NORMAL);
      world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
      world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
      world.setFullTime(2000);
    });

    new ArenaActionbarTask();
    new ArenaPlayingTimeExtendCheckerTask();
    new UpdateStatsWallTask();
    new ArenaBlockRemoveTask();
    new ArenaShowCpsTask();

    Arrays.asList("queue-npc")
      .forEach(key -> GameManager.instance().locationProvider().get(key)
        .ifPresent(FloorParticleTask::new));

    Arrays.asList("stick-change-npc", "block-change-npc")
      .forEach(key -> GameManager.instance().locationProvider().get(key)
        .ifPresent(TwinsParticleTask::new));

    GameManager.instance().locationProvider().get("queue-npc")
      .ifPresent(QueueParticleTask::new);
  }

  @Override
  public void onDisable() {
    if (GameManager.instance() != null) {
      GameManager.instance().onDisable();
    }
    if (PacketEvents.getAPI() != null && !PacketEvents.getAPI().isTerminated()) {
      PacketEvents.getAPI().terminate();
    }
  }
}
