package de.rexlmanu.mlgrush.plugin.command;

import de.rexlmanu.mlgrush.plugin.inventory.EditLayoutHandler;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InventoryCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        Player player = (Player) commandSender;
        PlayerProvider.find(player.getUniqueId()).filter(gamePlayer -> {
            if (gamePlayer.isIngame()) {
                gamePlayer.sendMessage("Du kannst dein Inventar nur in der Lobby anpassen.");
                return false;
            }
            return true;
        }).ifPresent(EditLayoutHandler::new);
        return true;
    }
}
