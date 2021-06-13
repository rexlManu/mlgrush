package de.rexlmanu.mlgrush.plugin.database.file;

import de.rexlmanu.mlgrush.plugin.database.DatabaseContext;
import de.rexlmanu.mlgrush.plugin.player.GamePlayerData;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// todo implement it
public class PluginStubeDatabase implements DatabaseContext {
  @Override
  public CompletableFuture<GamePlayerData> loadData(UUID uniqueId) {
    return null;
  }

  @Override
  public void saveData(GamePlayerData gamePlayerData) {
  }

  @Override
  public CompletableFuture<Integer> getRanking(UUID uniqueId) {
    return null;
  }

  @Override
  public CompletableFuture<List<GamePlayerData>> getRankings(int amount) {
    return null;
  }
}
