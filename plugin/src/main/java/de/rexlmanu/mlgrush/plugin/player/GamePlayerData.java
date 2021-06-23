package de.rexlmanu.mlgrush.plugin.player;

import eu.miopowered.repository.Key;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@AllArgsConstructor
@Accessors(fluent = true)
@Getter
@Setter
public class GamePlayerData implements Key {

  private UUID uniqueId;
  private long coins;
  private List<String> boughtItems;
  private List<String> inventorySorting;
  private Statistics statistics;
  private String selectedStick, selectedBlock;

  public GamePlayerData(UUID uniqueId) {
    this.uniqueId = uniqueId;
    this.coins = 0;
    this.boughtItems = new CopyOnWriteArrayList<>();
    this.inventorySorting = new ArrayList<String>() {{
      add("stick");
      add("pickaxe");
      add("block");
      add(null);
      add(null);
      add(null);
      add(null);
      add(null);
      add(null);
    }};
    this.statistics = new Statistics();
    this.selectedStick = null;
    this.selectedBlock = null;
  }

  @Override
  public String getKey() {
    return uniqueId.toString();
  }
}
