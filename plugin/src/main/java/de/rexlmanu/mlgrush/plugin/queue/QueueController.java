package de.rexlmanu.mlgrush.plugin.queue;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;

@Accessors(fluent = true)
@Getter
public class QueueController implements Runnable {

    private Queue<GamePlayer> playerQueue;

    public QueueController() {
        this.playerQueue = new PlayerQueue();

        Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 20 * 3);
    }

    public boolean inQueue(GamePlayer player) {
        return this.playerQueue.contains(player);
    }

    @Override
    public void run() {
        // Not enough players in queue
        if (this.playerQueue.size() < 2) return;

        GameManager.instance().arenaContainer().create(Arrays.asList(
                this.playerQueue.poll(),
                this.playerQueue.poll()
        ));
    }
}
