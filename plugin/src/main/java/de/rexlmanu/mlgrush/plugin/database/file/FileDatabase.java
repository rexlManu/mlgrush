package de.rexlmanu.mlgrush.plugin.database.file;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.database.DatabaseContext;
import de.rexlmanu.mlgrush.plugin.player.GamePlayerData;
import eu.miopowered.repository.Key;
import eu.miopowered.repository.Repository;
import eu.miopowered.repository.impl.GsonRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FileDatabase implements DatabaseContext {
  private Repository<GamePlayerData> repository;

  public FileDatabase() {
    this.repository = GsonRepository.of(
      GamePlugin.getProvidingPlugin(GamePlugin.class).getDataFolder().toPath().resolve("players"),
      GamePlayerData.class
    );
  }

  @Override
  public CompletableFuture<GamePlayerData> loadData(UUID uniqueId) {
    return CompletableFuture.supplyAsync(() -> this.repository.find(Key.wrap(uniqueId)).orElse(new GamePlayerData(uniqueId)));
  }

  @Override
  public void saveData(GamePlayerData gamePlayerData) {
    this.repository.update(gamePlayerData);
  }

  @Override
  public CompletableFuture<Integer> getRanking(UUID uniqueId) {
    return CompletableFuture.supplyAsync(() -> {
      List<GamePlayerData> playerDataList = this.repository.all().stream().sorted((o1, o2) -> o2.statistics().wins() - o1.statistics().wins()).collect(Collectors.toList());
      GamePlayerData data = playerDataList.stream().filter(gamePlayerData -> gamePlayerData.getKey().equals(uniqueId.toString())).findAny().orElse(null);
      if (data == null) return -1;
      return playerDataList.indexOf(data) + 1;
    });
  }

  @Override
  public CompletableFuture<List<GamePlayerData>> getRankings(int amount) {
    return CompletableFuture.supplyAsync(() -> this.repository
      .all()
      .stream()
      .sorted((o1, o2) -> o2.statistics().wins() - o1.statistics().wins())
      .limit(amount)
      .collect(Collectors.toList())
    );
  }
}
