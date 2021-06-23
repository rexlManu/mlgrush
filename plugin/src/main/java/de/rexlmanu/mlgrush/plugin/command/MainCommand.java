package de.rexlmanu.mlgrush.plugin.command;

import de.rexlmanu.mlgrush.plugin.Constants;
import de.rexlmanu.mlgrush.plugin.command.exception.CommandParameterMissingException;
import de.rexlmanu.mlgrush.plugin.command.subs.*;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainCommand implements CommandExecutor, TabExecutor {

  private List<SubCommand> commands;

  public MainCommand() {
    this.commands = new ArrayList<>();
    this.commands.add(new SetLocationCommand());
    this.commands.add(new HelpCommand(this.commands));
    this.commands.add(new DebugCommand());
    this.commands.add(new BuildCommand());
    this.commands.add(new SetRankingCommand());
    this.commands.add(new InspectionCommand());
    this.commands.add(new UpdateStatsWallCommand());
    this.commands.add(new RespawnMobCommand());
    this.commands.add(new ForceStatsWallCommand());
    this.commands.add(new NickCommand());
    this.commands.add(new UnnickCommand());
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) return true;
    if (args.length < 1) return false;
    Player player = (Player) sender;
    String targetName = args[0];
    Optional<SubCommand> optional = this.commands
      .stream()
      .filter(s -> s.name().equals(targetName))
      .findAny();
    optional.ifPresent(subCommand ->
      PlayerProvider.find(player.getUniqueId()).ifPresent(gamePlayer ->
      {
        try {
          subCommand.execute(gamePlayer, Arrays.copyOfRange(args, 1, args.length));
        } catch (Exception e) {
          this.handleException(gamePlayer, e, subCommand);
        }
      }));
    if (!optional.isPresent()) {
      sender.sendMessage(MessageFormat.of(Constants.PREFIX
        + "Es konnte kein Command mit &8'&a%s&8' &7gefunden werden.", targetName));
    }
    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    if (!(sender instanceof Player) || args.length == 0) return new ArrayList<>();
    if (args.length == 1) {
      return this.commands.stream().map(SubCommand::name).filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
    }
    String targetName = args[0];
    Optional<GamePlayer> gamePlayer = PlayerProvider.find(((Player) sender).getUniqueId());
    return gamePlayer.map(player -> this.commands
      .stream()
      .filter(subCommand -> subCommand.name().equals(targetName))
      .map(subCommand -> subCommand.suggestions(player, Arrays.copyOfRange(args, 1, args.length)))
      .findAny()
      .orElseGet(ArrayList::new)).orElseGet(ArrayList::new);

  }

  private void handleException(GamePlayer player, Exception exception, SubCommand command) {
    if (exception instanceof CommandParameterMissingException) {
      player.sendMessage(exception.getMessage());
    }

  }
}
