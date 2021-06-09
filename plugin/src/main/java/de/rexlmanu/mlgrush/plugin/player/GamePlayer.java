package de.rexlmanu.mlgrush.plugin.player;

import de.rexlmanu.mlgrush.plugin.Constants;
import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.Arena;
import de.rexlmanu.mlgrush.plugin.arena.ArenaStatistics;
import de.rexlmanu.mlgrush.plugin.game.GameEnvironment;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import eu.miopowered.repository.Key;
import fr.mrmicky.fastboard.FastBoard;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

@Data
@Getter
@Accessors(fluent = true)
public class GamePlayer {

    @Setter
    private GameEnvironment environment;
    @Setter
    @Nullable
    private Arena arena;
    @Setter
    @Nullable
    private ArenaStatistics arenaStatistics;


    private UUID uniqueId;
    private GamePlayerData data;
    private FastBoard fastBoard;

    public GamePlayer(UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.data = GamePlugin.getPlugin(GamePlugin.class).repository().find(Key.wrap(uniqueId)).orElse(new GamePlayerData(this.uniqueId));
        GameManager.instance().updateTablist(this);
        this.fastBoard = new FastBoard(this.player());
    }

    public void save() {
        GamePlugin.getPlugin(GamePlugin.class).repository().update(this.data);
    }

    public Player player() {
        return Bukkit.getPlayer(this.uniqueId);
    }

    public void sendMessage(String text) {
        this.player().sendMessage(MessageFormat.replaceColors(Constants.PREFIX + text));
    }

    public boolean isIngame() {
        return Objects.nonNull(this.arena);
    }

    public boolean isInLobby() {
        return Objects.isNull(this.arena);
    }

    public void sound(Sound sound, float pitch) {
        this.player().playSound(this.player().getLocation(), sound, 1f, pitch);
    }
}
