package de.rexlmanu.mlgrush.plugin.database.file;

import de.rexlmanu.mlgrush.plugin.Constants;
import de.rexlmanu.mlgrush.plugin.database.DatabaseContext;
import de.rexlmanu.mlgrush.plugin.equipment.BlockEquipment;
import de.rexlmanu.mlgrush.plugin.equipment.StickEquipment;
import de.rexlmanu.mlgrush.plugin.player.GamePlayerData;
import de.rexlmanu.mlgrush.plugin.player.Statistics;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.pluginstube.api.database.SQLConnection;
import net.pluginstube.api.database.coins.CoinsFactory;
import net.pluginstube.api.database.credentials.Credential;
import net.pluginstube.api.database.credentials.CredentialFactory;
import net.pluginstube.api.database.invsort.InvsortDatabase;
import net.pluginstube.api.database.invsort.model.InventorySort;
import net.pluginstube.api.database.stats.StatsDatabase;
import net.pluginstube.api.database.stats.StatsTable;
import net.pluginstube.api.perk.PerkCategory;
import net.pluginstube.api.perk.PerkFactory;
import net.pluginstube.api.perk.database.PerkDatabase;
import net.pluginstube.api.perk.exception.PerkNotFoundException;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

// todo implement it
@Getter
@Accessors(fluent = true)
public class PluginStubeDatabase implements DatabaseContext {

  @Getter
  private static PluginStubeDatabase instance;
  public static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);

  private CredentialFactory credentialFactory;
  private SQLConnection connection;
  private StatsDatabase statsDatabase;
  private PerkDatabase perkDatabase;
  private PerkFactory perkFactory;
  private CoinsFactory coinsFactory;
  private InvsortDatabase invsortDatabase;

  public PluginStubeDatabase() {
    instance = this;
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
    this.perkFactory = new PerkFactory();
    this.perkDatabase = new PerkDatabase(this.connection, this.perkFactory);
    this.invsortDatabase = new InvsortDatabase(this.connection);

    this.perkFactory.initPerkIndex();
  }

  @Override
  public CompletableFuture<GamePlayerData> loadData(UUID uniqueId) {
    return CompletableFuture.supplyAsync(() -> {
      long coins = this.coinsFactory.getCoins(uniqueId.toString());
      GamePlayerData data = new GamePlayerData(uniqueId);
      data.coins(coins);

      try {
        this.perkDatabase.getAllPerksFromPlayer(uniqueId.toString()).forEach(extendedPerk -> {
          if (extendedPerk.getPerk().perkCategory().equals(PerkCategory.MLGRUSH_BLOCKS)) {
            Arrays.stream(BlockEquipment.values()).filter(blockEquipment -> blockEquipment.perk().id().equals(extendedPerk.getPerk().id()))
              .findAny()
              .ifPresent(blockEquipment -> {
                data.boughtItems().add(blockEquipment.name().toLowerCase());
                if (extendedPerk.isSelected()) {
                  data.selectedBlock(blockEquipment.name().toLowerCase());
                }
              });
          }

          if (extendedPerk.getPerk().perkCategory().equals(PerkCategory.MLGRUSH_STICKS)) {
            Arrays.stream(StickEquipment.values()).filter(stickEquipment -> stickEquipment.perk().id().equals(extendedPerk.getPerk().id()))
              .findAny()
              .ifPresent(stickEquipment -> {
                data.boughtItems().add(stickEquipment.name().toLowerCase());
                if (extendedPerk.isSelected()) {
                  data.selectedStick(stickEquipment.name().toLowerCase());
                }
              });
          }
        });
      } catch (PerkNotFoundException ignored) {
      }
      try {
        if (this.invsortDatabase.dataExists(uniqueId)) {
          InventorySort sorting = this.invsortDatabase.getInventorySorting(uniqueId.toString());
          data.inventorySorting().clear();
          for (int i = 0; i < 9; i++) {
            data.inventorySorting().add(sorting.inventorySorting.getOrDefault(i, null));
          }
          if (Constants.OWNER.equals(uniqueId)) {
            Bukkit.getPlayer(Constants.OWNER).sendMessage("Invsort loaded.");
          }
        }
      } catch (Exception e) {
        Bukkit.getPlayer(Constants.OWNER).sendMessage("Invsort error." + e.getMessage());
        e.printStackTrace();
      }

      if (!this.statsDatabase.statsFromPlayerExists(uniqueId.toString(), StatsTable.MLGRUSH)) {
        statsDatabase.insertStatsPlayer(uniqueId.toString(), StatsTable.MLGRUSH);
        return data;
      }
      HashMap<String, Integer> stats = this.statsDatabase.getAllStats(uniqueId.toString(), StatsTable.MLGRUSH);
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
      if (gamePlayerData.selectedStick() != null) {
        PluginStubeDatabase
          .instance()
          .perkDatabase()
          .setPerkSelectedState(
            gamePlayerData.uniqueId().toString(),
            StickEquipment.valueOf(gamePlayerData.selectedStick().toUpperCase()).perk().id(),
            PerkCategory.MLGRUSH_STICKS
          );
      }
      if (gamePlayerData.selectedBlock() != null) {
        PluginStubeDatabase
          .instance()
          .perkDatabase()
          .setPerkSelectedState(
            gamePlayerData.uniqueId().toString(),
            BlockEquipment.valueOf(gamePlayerData.selectedBlock().toUpperCase()).perk().id(),
            PerkCategory.MLGRUSH_BLOCKS
          );
      }
      HashMap<String, Integer> statsMap = new HashMap<String, Integer>() {{
        Statistics statistics = gamePlayerData.statistics();
        put("kills", statistics.kills());
        put("deaths", statistics.deaths());
        put("wins", statistics.wins());
        put("played", statistics.games());
        put("beds", statistics.destroyedBeds());
      }};

      try {
        if (this.invsortDatabase.dataExists(gamePlayerData.uniqueId())) {
          InventorySort sort = new InventorySort();
          for (int i = 0; i < gamePlayerData.inventorySorting().size(); i++) {
            sort.inventorySorting.put(i, gamePlayerData.inventorySorting().get(i));
          }
          this.invsortDatabase.updateInventorySorting(gamePlayerData.uniqueId().toString(), sort);
        } else {
          InventorySort sort = new InventorySort();
          for (int i = 0; i < gamePlayerData.inventorySorting().size(); i++) {
            sort.inventorySorting.put(i, gamePlayerData.inventorySorting().get(i));
          }
          this.invsortDatabase.insertNewData(gamePlayerData.uniqueId().toString(), sort);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      this.statsDatabase.updateFullStatsDataset(gamePlayerData.uniqueId().toString(), statsMap, StatsTable.MLGRUSH);
      coinsFactory.setCoins(gamePlayerData.uniqueId().toString(), gamePlayerData.coins());
    });
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
