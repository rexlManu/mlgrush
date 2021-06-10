package de.rexlmanu.mlgrush.plugin.utility;

import org.bukkit.Location;

public class LocationUtils {

  /**
   * Check if a location is in a range from the center
   *
   * @param center location of the center point
   * @param target location that should get checked
   * @param range  range that is used to check how far away the target can be
   * @return if the location is inside the range
   */
  public static boolean rangeContains(Location center, Location target, int range) {
    return target.getX() > Math.min(center.getX() + range, center.getX() - range)
      && target.getX() < Math.max(center.getX() + range, center.getX() - range)
      && target.getZ() > Math.min(center.getZ() + range, center.getZ() - range)
      && target.getZ() < Math.max(center.getZ() + range, center.getZ() - range);
  }

}
