package de.rexlmanu.mlgrush.plugin.utility;

import net.kyori.adventure.title.Title;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.time.Duration;

public final class PlayerUtils {

  private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

  private PlayerUtils() {
  }

  public static void resetPlayer(Player player) {
    player.closeInventory();
    player.getInventory().clear();
    player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
    double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH) == null ? 20.0D : player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    player.setHealth(Math.min(maxHealth, 20.0D));
    player.setFoodLevel(20);
    player.setFireTicks(0);
    player.setGameMode(GameMode.ADVENTURE);
    player.setAllowFlight(false);
    player.setFlying(false);
    player.setVelocity(new Vector(0, 0, 0));
    player.setLevel(0);
    player.setExp(0);
    player.setWalkSpeed(0.2f);
    player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    player.setMaximumNoDamageTicks(20);
  }

  public static void updateGameMode(Player player, GameMode gameMode) {
    player.setGameMode(gameMode);
  }

  public static Location faceLocation(Entity entity, Location to) {
    if (entity.getWorld() != to.getWorld()) {
      return null;
    }
    Location fromLocation = entity.getLocation();

    double xDiff = to.getX() - fromLocation.getX();
    double yDiff = to.getY() - fromLocation.getY();
    double zDiff = to.getZ() - fromLocation.getZ();

    double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
    double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);

    double yaw = Math.toDegrees(Math.acos(xDiff / distanceXZ));
    double pitch = Math.toDegrees(Math.acos(yDiff / distanceY)) - 90.0D;
    if (zDiff < 0.0D) {
      yaw += Math.abs(180.0D - yaw) * 2.0D;
    }
    Location location = entity.getLocation();
    location.setYaw((float) (yaw - 90.0F));
    location.setPitch((float) (pitch - 90.0F));
    return location;
  }

  public static void sendTitle(Player player, int fadeInTime, int showTime, int fadeOutTime, String title, String subtitle) {
    player.showTitle(Title.title(
      LEGACY_SERIALIZER.deserialize(MessageFormat.replaceColors(title)),
      LEGACY_SERIALIZER.deserialize(MessageFormat.replaceColors(subtitle)),
      Title.Times.times(Duration.ofMillis(fadeInTime * 50L), Duration.ofMillis(showTime * 50L), Duration.ofMillis(fadeOutTime * 50L))
    ));
  }
}
