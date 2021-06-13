package de.rexlmanu.mlgrush.plugin.database;

import de.rexlmanu.mlgrush.plugin.player.GamePlayerData;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The functions that are handles the game data
 */
public interface DatabaseContext {
  CompletableFuture<GamePlayerData> loadData(UUID uniqueId);

  void saveData(GamePlayerData gamePlayerData);

  CompletableFuture<Integer> getRanking(UUID uniqueId);

  CompletableFuture<List<GamePlayerData>> getRankings(int amount);
}
