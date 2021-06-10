package de.rexlmanu.mlgrush.plugin.command;

import de.rexlmanu.mlgrush.plugin.command.exception.CommandParameterMissingException;

public interface CommandParameter {

  static void require(String name, int position, String[] arguments) throws CommandParameterMissingException {
    if (arguments.length < (position + 1)) throw new CommandParameterMissingException(
      String.format("Der Parameter &8'&e%s&8' &7fehlt.", name)
    );
  }

}
