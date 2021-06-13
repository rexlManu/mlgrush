package de.rexlmanu.mlgrush.plugin.task.arena;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import eu.miopowered.packetlistener.reflection.PacketReflection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ArenaBlockRemoveTask implements Runnable {
  private Map<Block, BlockAnimation> blockAnimationMap;

  public ArenaBlockRemoveTask() {
    this.blockAnimationMap = new HashMap<>();
    Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 6);
  }

  @Override
  public void run() {
    this.blockAnimationMap.keySet().stream().filter(block -> block.getType().equals(Material.AIR)).forEach(block -> this.blockAnimationMap.remove(block));

    GameManager.instance().arenaManager().arenaContainer().activeArenas().stream().filter(arena -> arena.configuration().autoBlockBreak()).forEach(arena -> {
      arena.placedBlocks().forEach(block -> {
        if (!this.blockAnimationMap.containsKey(block)) {
          this.blockAnimationMap.put(block, new BlockAnimation(0));
        }
        BlockAnimation animation = this.blockAnimationMap.get(block);
        if (animation.state >= 9) {
          arena.placedBlocks().remove(block);
          Bukkit.getScheduler().runTask(GamePlugin.getProvidingPlugin(GamePlugin.class), () -> block.setType(Material.AIR));
          return;
        }
        animation.state++;
        arena
          .players()
          .stream()
          .map(GamePlayer::player)
          .forEach(player -> this.sendBlockAnimation(player, block, animation));
      });
    });
  }

  private void sendBlockAnimation(Player player, Block block, BlockAnimation blockAnimation) {
    try {
      Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
      Object playerConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
      Method sendPacket = playerConnection.getClass().getMethod("sendPacket", PacketReflection.nmsClass("Packet"));
      Class<?> blockPosition = PacketReflection.nmsClass("BlockPosition");
      Location location = block.getLocation();
      Object packetPlayOutBlockBreakAnimation = PacketReflection.nmsClass("PacketPlayOutBlockBreakAnimation");
      blockPosition
        .getConstructor(int.class, blockPosition, int.class)
        .newInstance(blockAnimation.entityId, blockPosition.getConstructor(int.class, int.class, int.class)
          .newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ()), blockAnimation.state);
      sendPacket.invoke(playerConnection, packetPlayOutBlockBreakAnimation);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private class BlockAnimation {
    private int entityId;
    private int state;

    public BlockAnimation(int state) {
      this.entityId = ThreadLocalRandom.current().nextInt(1000000, 9000000);
      this.state = state;
    }
  }
}
