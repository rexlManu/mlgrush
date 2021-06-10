package de.rexlmanu.mlgrush.plugin.arena.position;

import de.rexlmanu.mlgrush.arenalib.Position;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Location;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public class ArenaLocationMapper {

  private Location startPoint;

  public Location toLocation(Position position) {
    Location location = this.startPoint.clone().add(position.x(), position.y(), position.z());
    location.setYaw(position.yaw());
    location.setPitch(position.pitch());
    return location;
  }

}
