package de.rexlmanu.mlgrush.plugin.utility.hologram;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class VirtualHologram {

  private static final AtomicInteger ENTITY_IDS = new AtomicInteger(3_000_000);
  private static final byte ENTITY_FLAG_INVISIBLE = 0x20;
  private static final byte ARMOR_STAND_FLAG_SMALL = 0x01;
  private static final LegacyComponentSerializer LEGACY_SERIALIZER =
      LegacyComponentSerializer.legacySection();

  private Location location;
  private List<String> lines;
  private double distanceAbove = -0.27D;
  private final Set<UUID> viewers = new HashSet<>();
  private final List<LineEntity> lineEntities = new ArrayList<>();

  public VirtualHologram(Location location, String... lines) {
    this(location, Arrays.asList(lines));
  }

  public VirtualHologram(Location location, List<String> lines) {
    this.location = location;
    this.lines = new ArrayList<>(lines);
    this.rebuild();
  }

  public List<String> getLines() {
    return new ArrayList<>(this.lines);
  }

  public Location getLocation() {
    return this.location;
  }

  public void send(Player player) {
    if (player == null || !this.viewers.add(player.getUniqueId())) {
      return;
    }
    this.spawnFor(player);
  }

  public void destroy(Player player) {
    if (player == null || !this.viewers.remove(player.getUniqueId())) {
      return;
    }
    this.destroyFor(player);
  }

  public void destroy() {
    Set<UUID> currentViewers = new HashSet<>(this.viewers);
    this.viewers.clear();
    currentViewers.stream().map(Bukkit::getPlayer).forEach(this::destroyFor);
  }

  public void broadcast() {
    Bukkit.getOnlinePlayers().forEach(this::send);
  }

  public void broadcast(List<Player> players) {
    players.forEach(this::send);
  }

  public void setDistance_above(double distanceAbove) {
    this.distanceAbove = distanceAbove;
    this.reset();
  }

  public void setLines(List<String> lines) {
    this.lines = new ArrayList<>(lines);
    this.reset();
  }

  public void setLines(String... lines) {
    this.setLines(Arrays.asList(lines));
  }

  public void setLocation(Location location) {
    this.location = location;
    this.reset();
  }

  public double getDistance_above() {
    return this.distanceAbove;
  }

  private void spawnFor(Player player) {
    this.lineEntities.forEach(
        line -> {
          PacketEvents.getAPI()
              .getPlayerManager()
              .sendPacket(
                  player,
                  new WrapperPlayServerSpawnEntity(
                      line.entityId(),
                      line.uniqueId(),
                      EntityTypes.ARMOR_STAND,
                      SpigotConversionUtil.fromBukkitLocation(line.location()),
                      line.location().getYaw(),
                      0,
                      null));
          PacketEvents.getAPI()
              .getPlayerManager()
              .sendPacket(
                  player,
                  new WrapperPlayServerEntityMetadata(line.entityId(), this.metadata(line.text())));
        });
  }

  private void destroyFor(Player player) {
    if (player == null || this.lineEntities.isEmpty()) {
      return;
    }
    PacketEvents.getAPI()
        .getPlayerManager()
        .sendPacket(
            player,
            new WrapperPlayServerDestroyEntities(
                this.lineEntities.stream().mapToInt(LineEntity::entityId).toArray()));
  }

  private List<EntityData<?>> metadata(String text) {
    List<EntityData<?>> metadata = new ArrayList<>();
    metadata.add(new EntityData<>(0, EntityDataTypes.BYTE, ENTITY_FLAG_INVISIBLE));
    metadata.add(
        new EntityData<>(
            2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(this.component(text))));
    metadata.add(new EntityData<>(3, EntityDataTypes.BOOLEAN, true));
    metadata.add(new EntityData<>(5, EntityDataTypes.BOOLEAN, true));
    metadata.add(new EntityData<>(15, EntityDataTypes.BYTE, ARMOR_STAND_FLAG_SMALL));
    return metadata;
  }

  private Component component(String text) {
    return LEGACY_SERIALIZER.deserialize(MessageFormat.replaceColors(text));
  }

  private void reset() {
    Set<UUID> currentViewers = new HashSet<>(this.viewers);
    currentViewers.stream().map(Bukkit::getPlayer).forEach(this::destroyFor);
    this.rebuild();
    currentViewers.stream().map(Bukkit::getPlayer).forEach(this::spawnFor);
  }

  private void rebuild() {
    this.lineEntities.clear();
    if (this.location == null || this.location.getWorld() == null) {
      return;
    }
    double y = this.location.getY();
    for (String line : this.lines) {
      y += this.distanceAbove;
      if (line.isEmpty()) {
        continue;
      }
      this.lineEntities.add(
          new LineEntity(
              ENTITY_IDS.getAndIncrement(),
              UUID.randomUUID(),
              new Location(
                  this.location.getWorld(),
                  this.location.getX(),
                  y,
                  this.location.getZ(),
                  this.location.getYaw(),
                  this.location.getPitch()),
              line));
    }
  }

  private record LineEntity(int entityId, UUID uniqueId, Location location, String text) {}
}
