package de.rexlmanu.mlgrush.plugin.game.npc;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.npc.NPC;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.equipment.BlockEquipment;
import de.rexlmanu.mlgrush.plugin.equipment.StickEquipment;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.utility.hologram.VirtualHologram;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class LobbyNpcManager implements Listener {

  private static final AtomicInteger ENTITY_IDS = new AtomicInteger(2_000_000);

  private final List<ManagedNpc> npcs = new ArrayList<>();
  private final PacketNpcInteractionListener interactionListener = new PacketNpcInteractionListener();

  private BukkitTask rotationTask;

  public LobbyNpcManager() {
    org.bukkit.plugin.java.JavaPlugin plugin = GamePlugin.getProvidingPlugin(GamePlugin.class);
    Bukkit.getPluginManager().registerEvents(this, plugin);
    PacketEvents.getAPI().getEventManager().registerListener(this.interactionListener);
    this.load();
  }

  public void respawnAll() {
    this.npcs.forEach(ManagedNpc::despawnAll);
    this.npcs.clear();
    this.load();
    Bukkit.getOnlinePlayers().forEach(this::spawnFor);
  }

  public void shutdown() {
    if (this.rotationTask != null) {
      this.rotationTask.cancel();
      this.rotationTask = null;
    }
    this.npcs.forEach(ManagedNpc::despawnAll);
    this.npcs.clear();
    HandlerList.unregisterAll(this);
  }

  @EventHandler
  public void handle(PlayerJoinEvent event) {
    Bukkit.getScheduler().runTaskLater(GamePlugin.getProvidingPlugin(GamePlugin.class), () -> this.spawnFor(event.getPlayer()), 10L);
  }

  @EventHandler
  public void handle(PlayerQuitEvent event) {
    Object channel = this.channel(event.getPlayer());
    if (channel == null) {
      return;
    }
    this.npcs.forEach(npc -> {
      npc.hologram().destroy(event.getPlayer());
      npc.despawn(channel);
    });
  }

  private void load() {
    this.register("queue-npc", "QueueNpc", "&aQueue", "&7Suche nach einem Gegner.", Material.COMPASS, player -> {
      if (player.environment().equals(de.rexlmanu.mlgrush.plugin.game.Environment.ARENA)) {
        player.sendMessage("Du bist bereits in einem Spiel.");
        return;
      }
      de.rexlmanu.mlgrush.plugin.game.GameManager gameManager = de.rexlmanu.mlgrush.plugin.game.GameManager.instance();
      if (gameManager.queueCooldown().currently(player.uniqueId())) {
        return;
      }
      gameManager.queueCooldown().add(player.uniqueId());
      if (gameManager.queueController().inQueue(player)) {
        gameManager.queueController().playerQueue().remove(player);
        player.sendMessage("Du hast die &aWarteschlange &7verlassen.");
        gameManager.scoreboardHandler().updateAll(de.rexlmanu.mlgrush.plugin.game.Environment.LOBBY);
        player.sound(org.bukkit.Sound.BLOCK_PISTON_CONTRACT, 2f);
        return;
      }
      gameManager.queueController().playerQueue().offer(player);
      player.sendMessage("Du hast die &aWarteschlange &7betreten.");
      gameManager.scoreboardHandler().updateAll(de.rexlmanu.mlgrush.plugin.game.Environment.LOBBY);
      player.sound(org.bukkit.Sound.BLOCK_PISTON_EXTEND, 2f);
    });
    this.register("stick-change-npc", "StickNpc", "&aSticks", "&7Aendere deinen Stick.", new ItemStack(StickEquipment.values()[0].material()), player ->
      new de.rexlmanu.mlgrush.plugin.inventory.ShopInventory(player, "&aStick", StickEquipment.values()));
    this.register("block-change-npc", "BlockNpc", "&aBloecke", "&7Aendere den Typ deiner Bloecke.", new ItemStack(BlockEquipment.values()[0].material()), player ->
      new de.rexlmanu.mlgrush.plugin.inventory.ShopInventory(player, "&aBlöcke", BlockEquipment.values()));

    if (this.rotationTask != null) {
      this.rotationTask.cancel();
    }
    this.rotationTask = Bukkit.getScheduler().runTaskTimer(GamePlugin.getProvidingPlugin(GamePlugin.class), this::updateRotations, 1L, 10L);
  }

  private void register(String locationKey, String profileName, String title, String subtitle, Material mainHand, InteractionHandler handler) {
    this.register(locationKey, profileName, title, subtitle, new ItemStack(mainHand), handler);
  }

  private void register(String locationKey, String profileName, String title, String subtitle, ItemStack mainHand, InteractionHandler handler) {
    Optional<Location> optionalLocation = de.rexlmanu.mlgrush.plugin.game.GameManager.instance().locationProvider().get(locationKey);
    if (optionalLocation.isEmpty()) {
      return;
    }

    Location location = optionalLocation.get().clone();
    UserProfile profile = new UserProfile(UUID.nameUUIDFromBytes(("mlgrush:npc:" + profileName).getBytes()), profileName);
    NPC npc = new NPC(profile, ENTITY_IDS.getAndIncrement(), Component.empty());
    npc.setLocation(SpigotConversionUtil.fromBukkitLocation(location));
    npc.setMainHand(SpigotConversionUtil.fromBukkitItemStack(mainHand));
    npc.updateEquipment();
    npc.updateRotation(location.getYaw(), location.getPitch());
    Location hologramLocation = location.clone().add(0, 1.35, 0);
    VirtualHologram hologram = new VirtualHologram(hologramLocation, title, subtitle);
    hologram.setDistance_above(-0.32D);
    this.npcs.add(new ManagedNpc(location, npc, hologram, handler));
  }

  private void spawnFor(Player player) {
    Object channel = this.channel(player);
    if (channel == null) {
      return;
    }
    this.npcs.forEach(npc -> {
      npc.spawn(channel);
      npc.hologram().send(player);
      PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerPlayerInfoRemove(npc.npc().getProfile().getUUID()));
    });
  }

  private Object channel(Player player) {
    return PacketEvents.getAPI().getPlayerManager().getChannel(player);
  }

  private void updateRotations() {
    this.npcs.forEach(managedNpc -> {
      Player nearest = Bukkit.getOnlinePlayers().stream()
        .filter(player -> player.getWorld().equals(managedNpc.location().getWorld()))
        .filter(player -> player.getLocation().distanceSquared(managedNpc.location()) <= 25)
        .min(java.util.Comparator.comparingDouble(player -> player.getLocation().distanceSquared(managedNpc.location())))
        .orElse(null);
      if (nearest == null) {
        return;
      }
      Location facing = managedNpc.location().clone();
      facing.setDirection(nearest.getEyeLocation().toVector().subtract(facing.toVector()));
      managedNpc.npc().updateRotation(facing.getYaw(), managedNpc.location().getPitch());
    });
  }

  private final class PacketNpcInteractionListener extends PacketListenerAbstract {

    private PacketNpcInteractionListener() {
      super(PacketListenerPriority.NORMAL);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) {
        return;
      }
      Object source = event.getPlayer();
      if (!(source instanceof Player player)) {
        return;
      }

      WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
      if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
        return;
      }

      ManagedNpc managedNpc = LobbyNpcManager.this.npcs.stream()
        .filter(npc -> npc.npc().getId() == wrapper.getEntityId())
        .findFirst()
        .orElse(null);
      if (managedNpc == null) {
        return;
      }

      Bukkit.getScheduler().runTask(GamePlugin.getProvidingPlugin(GamePlugin.class), () ->
        PlayerProvider.find(player.getUniqueId()).ifPresent(gamePlayer -> managedNpc.handler().handle(gamePlayer)));
    }
  }

  private record ManagedNpc(Location location, NPC npc, VirtualHologram hologram, InteractionHandler handler) {

    private void spawn(Object channel) {
      this.npc.spawn(channel);
      PacketEvents.getAPI().getProtocolManager().sendPacket(channel, new WrapperPlayServerTeams(
        "nh" + this.npc.getId(),
        WrapperPlayServerTeams.TeamMode.CREATE,
        new WrapperPlayServerTeams.ScoreBoardTeamInfo(
          Component.text("nh" + this.npc.getId()),
          Component.empty(),
          Component.empty(),
          WrapperPlayServerTeams.NameTagVisibility.NEVER,
          WrapperPlayServerTeams.CollisionRule.ALWAYS,
          null,
          WrapperPlayServerTeams.OptionData.NONE
        ),
        this.npc.getProfile().getName()
      ));
    }

    private void despawn(Object channel) {
      this.npc.despawn(channel);
      PacketEvents.getAPI().getProtocolManager().sendPacket(channel, new WrapperPlayServerTeams(
        "nh" + this.npc.getId(),
        WrapperPlayServerTeams.TeamMode.REMOVE,
        java.util.Optional.empty()
      ));
    }

    private void despawnAll() {
      this.npc.despawnAll();
      this.hologram.destroy();
    }
  }
}
