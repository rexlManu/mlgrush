package de.rexlmanu.mlgrush.plugin.task;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.ArenaManager;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import org.bukkit.Bukkit;

public class ArenaPlayingTimeExtendCheckerTask implements Runnable {
  public ArenaPlayingTimeExtendCheckerTask() {
    Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 1);
  }

  @Override
  public void run() {
    ArenaManager arenaManager = GameManager
      .instance()
      .arenaManager();
    arenaManager
      .arenaContainer()
      .activeArenas()
      .stream()
      .filter(arena -> (System.currentTimeMillis() - arena.gameStart()) > arena.configuration().maximalGameLength() * 1000)
      .forEach(arenaManager::destroy);
  }
}
