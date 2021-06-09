package de.rexlmanu.mlgrush.plugin.arena;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@Accessors(fluent = true, chain = true)
@Data
public class ArenaStatistics {
    private int kills, deaths, blocks;

    @Nullable
    private Player lastHitter;

    public void addBlock() {
        this.blocks++;
    }

    public void addKill() {
        this.kills++;
    }

    public void addDeath() {
        this.deaths++;
    }
}
