package de.rexlmanu.mlgrush.plugin.task.arena;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockBreakAnimation;
import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ArenaBlockRemoveTask implements Runnable {
  private final Map<Block, Float> blockAnimationMap = new HashMap<>();

  public ArenaBlockRemoveTask() {
    Bukkit.getScheduler().runTaskTimer(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 6);
  }

  @Override
  public void run() {
    Iterator<Map.Entry<Block, Float>> iterator = this.blockAnimationMap.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<Block, Float> entry = iterator.next();
      if (entry.getKey().getType().isAir()) {
        iterator.remove();
      }
    }

    GameManager.instance().arenaManager().arenaContainer().activeArenas().stream().filter(arena -> arena.configuration().autoBlockBreak()).forEach(arena -> {
      arena.placedBlocks().forEach(block -> {
        float progress = Math.min(1.0f, this.blockAnimationMap.getOrDefault(block, 0.0f) + 0.1f);
        this.blockAnimationMap.put(block, progress);

        arena.players().stream().map(GamePlayer::player).forEach(player -> this.sendBlockAnimation(player, block, progress));

        if (progress >= 1.0f) {
          arena.placedBlocks().remove(block);
          block.setType(Material.AIR, false);
          this.blockAnimationMap.remove(block);
        }
      });
    });
  }

  private void sendBlockAnimation(Player player, Block block, float progress) {
    if (player == null) {
      return;
    }
    byte stage = (byte) Math.max(0, Math.min(9, Math.round(progress * 9.0f)));
    WrapperPlayServerBlockBreakAnimation packet = new WrapperPlayServerBlockBreakAnimation(
      block.hashCode(),
      new Vector3i(block.getX(), block.getY(), block.getZ()),
      stage
    );
    PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
  }
}
