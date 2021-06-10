package de.rexlmanu.mlgrush.plugin.arena.team;

import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
public class GameTeam {

  private TeamColor name;
  private Location spawnLocation;
  private List<GamePlayer> members;
  private int points;

  public GameTeam(TeamColor name, Location spawnLocation) {
    this.name = name;
    this.spawnLocation = spawnLocation;
    this.members = new ArrayList<>();
    this.points = 0;
  }

  public void addPoint() {
    this.points++;
  }
}
