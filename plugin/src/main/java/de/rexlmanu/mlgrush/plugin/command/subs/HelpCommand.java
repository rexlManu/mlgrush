package de.rexlmanu.mlgrush.plugin.command.subs;

import de.rexlmanu.mlgrush.plugin.command.SubCommand;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AllArgsConstructor
public class HelpCommand implements SubCommand {

    private List<SubCommand> commands;

    @Override
    public @NotNull String description() {
        return "Zeigt dir alle Commands an";
    }

    @Override
    public @NotNull String name() {
        return "help";
    }

    @Override
    public void execute(GamePlayer gamePlayer, String[] arguments) {
        this.commands.forEach(subCommand ->
                gamePlayer.sendMessage(MessageFormat.of("%s - %s", subCommand.name(), subCommand.description())));
    }
}
