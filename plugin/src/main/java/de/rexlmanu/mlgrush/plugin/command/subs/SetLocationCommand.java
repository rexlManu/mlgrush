package de.rexlmanu.mlgrush.plugin.command.subs;

import de.rexlmanu.mlgrush.plugin.command.CommandParameter;
import de.rexlmanu.mlgrush.plugin.command.SubCommand;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class SetLocationCommand implements SubCommand {

    @Override
    public @NotNull String name() {
        return "setlocation";
    }

    @Override
    public @NotNull String description() {
        return "Setze eine Location";
    }

    @Override
    public void execute(GamePlayer gamePlayer, String[] arguments) throws Exception {
        CommandParameter.require("name", 0, arguments);
        Location location = gamePlayer.player().getLocation().clone();
        location.setX(location.getBlockX() + 0.5);
        location.setZ(location.getBlockZ() + 0.5);
        GameManager.instance().locationProvider().set(arguments[0], location);
        gamePlayer.sendMessage(String.format("Die Location &e%s &7wurde gesetzt.", arguments[0]));
    }
}
