package de.rexlmanu.mlgrush.plugin.game.npc;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.utility.NoAI;
import de.rexlmanu.mlgrush.plugin.utility.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InteractiveMob implements Runnable, Listener {

  private Entity entity;
  private BukkitTask viewTask;
  private Hologram hologram;
  private Location location;

  private final EntityType type;
  private final List<String> lines;
  private final InteractionHandler listener;

  public InteractiveMob(EntityType type, List<String> lines, InteractionHandler listener) {
    this.type = type;
    this.lines = lines;
    this.listener = listener;
  }

  public InteractiveMob create(Location location) {
    this.location = location;
    return this.spawn();
  }

  public InteractiveMob spawn() {
    this.entity = this.location.getWorld().spawnEntity(this.location, this.type);
    NoAI.setEntityAi(this.entity, false);
    this.hologram = new Hologram(this.lines, this.location.clone().add(0, 0.6, 0));

    JavaPlugin plugin = GamePlugin.getProvidingPlugin(GamePlugin.class);
    this.viewTask = Bukkit.getScheduler().runTaskTimer(plugin, this, 0, 1);
    Bukkit.getPluginManager().registerEvents(this, plugin);
    return this;
  }

  public void remove() {
    if (this.viewTask != null) {
      this.viewTask.cancel();
    }
    if (this.entity != null) {
      this.entity.remove();
    }
    if (this.hologram != null) {
      this.hologram.delete();
    }
    HandlerList.unregisterAll(this);
  }

  @Override
  public void run() {
    if (this.entity == null || !this.entity.isValid()) {
      return;
    }
    Player target = Bukkit.getOnlinePlayers()
      .stream()
      .filter(player -> player.getWorld().equals(this.entity.getWorld()))
      .filter(player -> player.getLocation().distanceSquared(this.entity.getLocation()) < 25)
      .min(Comparator.comparingDouble(player -> player.getLocation().distanceSquared(this.entity.getLocation())))
      .orElse(null);
    if (target != null) {
      Location facing = this.entity.getLocation().clone();
      facing.setDirection(target.getEyeLocation().toVector().subtract(facing.toVector()));
      this.entity.teleport(facing);
    }
  }

  @EventHandler
  public void handle(PlayerInteractEntityEvent event) {
    if (event.getRightClicked().equals(this.entity)) {
      event.setCancelled(true);
      PlayerProvider.find(event.getPlayer().getUniqueId()).ifPresent(this.listener::handle);
    }
  }
}
