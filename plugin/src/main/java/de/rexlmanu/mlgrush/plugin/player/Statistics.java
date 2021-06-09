package de.rexlmanu.mlgrush.plugin.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true, chain = true)
@Getter
@Setter
public class Statistics {
    private int kills, deaths, wins, games, blocks;

    public Statistics() {
        this.kills = 0;
        this.deaths = 0;
        this.wins = 0;
        this.games = 0;
        this.blocks = 0;
    }

    public Statistics addKills(int kills) {
        this.kills += kills;
        return this;
    }

    public Statistics addDeaths(int deaths) {
        this.deaths += deaths;
        return this;
    }

    public Statistics addBlocks(int blocks) {
        this.blocks += blocks;
        return this;
    }

    public Statistics addGame() {
        this.games++;
        return this;
    }

    public Statistics addWin() {
        this.wins++;
        return this;
    }
}
