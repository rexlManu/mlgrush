package de.rexlmanu.mlgrush.arenacreator.command;

import de.rexlmanu.mlgrush.arenacreator.ArenaCreatorPlugin;
import de.rexlmanu.mlgrush.arenacreator.Constants;
import de.rexlmanu.mlgrush.arenacreator.process.ArenaCreationProcess;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class ArenaCreatorCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] arguments) {
        if (!(commandSender instanceof Player)) return true;
        Player player = (Player) commandSender;
        if (arguments.length == 0) {
            player.sendMessage(Constants.PREFIX + "/arenacreator start - Start den Erstellungsprozess");
            player.sendMessage(Constants.PREFIX + "/arenacreator abort - Breche den Prozess ab");
            return true;
        }
        Optional<ArenaCreationProcess> optionalProcess = ArenaCreatorPlugin.ARENA_CREATION_PROCESSES
                .stream()
                .filter(arenaCreationProcess -> arenaCreationProcess.player().equals(player))
                .findAny();
        if ("start".equals(arguments[0].toLowerCase())) {
            if (optionalProcess.isPresent()) {
                player.sendMessage(Constants.PREFIX + "Du hast bereits einen Prozess am laufen.");
                return true;
            }
            player.sendMessage(Constants.PREFIX + "Es wurde ein Prozess erstellt.");
            ArenaCreatorPlugin.ARENA_CREATION_PROCESSES.add(new ArenaCreationProcess(player));
        }
        if ("abort".equals(arguments[0].toLowerCase())) {
            if (optionalProcess.isPresent()) {
                player.sendMessage(Constants.PREFIX + "Du hast den Prozess abgebrochen.");
                optionalProcess.get().abort();
            } else {
                player.sendMessage(Constants.PREFIX + "Du hast aktuell keinen Prozess am laufen.");
            }
        }
        return true;
    }
}
