package de.rexlmanu.mlgrush.plugin.database.file;

import de.rexlmanu.mlgrush.plugin.database.DatabaseContext;
import de.rexlmanu.mlgrush.plugin.player.GamePlayerData;
import de.rexlmanu.mlgrush.plugin.player.Statistics;
import net.pluginstube.api.database.SQLConnection;
import net.pluginstube.api.database.coins.CoinsFactory;
import net.pluginstube.api.database.credentials.Credential;
import net.pluginstube.api.database.credentials.CredentialFactory;
import net.pluginstube.api.database.stats.StatsDatabase;
import net.pluginstube.api.database.stats.StatsTable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

// todo implement it
public class PluginStubeDatabase implements DatabaseContext {

  private static final ExecutorService EXECUTOR_SERVICE = Executors.newWorkStealingPool(4);

  private CredentialFactory credentialFactory;
  private SQLConnection connection;
  private StatsDatabase statsDatabase;
  private CoinsFactory coinsFactory;

  public PluginStubeDatabase() {
    this.credentialFactory = new CredentialFactory();
    try {
      this.credentialFactory.init();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Credential credential = this.credentialFactory.get("mysql");
    this.connection = new SQLConnection(
      credential.getCredentials().get("hostname").toString(),
      credential.getCredentials().get("database").toString(),
      credential.getCredentials().get("username").toString(),
      credential.getCredentials().get("password").toString()
    );

    this.connection.connect();

    this.statsDatabase = new StatsDatabase(this.connection);
    this.coinsFactory = new CoinsFactory(this.connection);
  }

  @Override
  public CompletableFuture<GamePlayerData> loadData(UUID uniqueId) {
    return CompletableFuture.supplyAsync(() -> {
      long coins = this.coinsFactory.getCoins(uniqueId.toString());
      HashMap<String, Integer> stats = this.statsDatabase.getAllStats(uniqueId.toString(), StatsTable.MLGRUSH);
      GamePlayerData data = new GamePlayerData(uniqueId);
      data.coins(coins);
      data.statistics().kills(stats.get("kills"));
      data.statistics().deaths(stats.get("deaths"));
      data.statistics().wins(stats.get("wins"));
      data.statistics().games(stats.get("played"));
      data.statistics().destroyedBeds(stats.get("beds"));
      return data;
    });
  }

  @Override
  public void saveData(GamePlayerData gamePlayerData) {
    EXECUTOR_SERVICE.submit(() -> {
      statsDatabase.updateFullStatsDataset(gamePlayerData.uniqueId().toString(), new HashMap<String, Integer>() {{
        Statistics statistics = gamePlayerData.statistics();
        put("kills", statistics.kills());
        put("deaths", statistics.deaths());
        put("wins", statistics.wins());
        put("played", statistics.games());
        put("beds", statistics.destroyedBeds());
      }}, StatsTable.MLGRUSH);
    });
    coinsFactory.setCoins(gamePlayerData.uniqueId().toString(), Math.toIntExact(gamePlayerData.coins()));
  }

  @Override
  public CompletableFuture<Integer> getRanking(UUID uniqueId) {
    return CompletableFuture.supplyAsync(() -> this.statsDatabase.getPlayersRank(uniqueId.toString(), StatsTable.MLGRUSH));
  }

  @Override
  public CompletableFuture<List<GamePlayerData>> getRankings(int amount) {
    return CompletableFuture.supplyAsync(() -> this
      .statsDatabase.getTopPlayers(StatsTable.MLGRUSH, amount)
      .entrySet()
      .stream()
      .sorted(Comparator.comparingInt(Map.Entry::getKey))
      .map(Map.Entry::getValue)
      .map(UUID::fromString)
      .map(this::loadData)
      .map(gamePlayerDataCompletableFuture -> {
        try {
          return gamePlayerDataCompletableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
          return null;
        }
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList()));
  }
}
